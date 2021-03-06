/*
 * *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** *
 */

package org.matsim.contrib.taxi.run;

import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.Fleet;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizer;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.passenger.PassengerRequestCreator;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.contrib.dvrp.run.AbstractMultiModeQSimModule;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.ModalProviders;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic.DynActionCreator;
import org.matsim.contrib.taxi.optimizer.DefaultTaxiOptimizerProvider;
import org.matsim.contrib.taxi.optimizer.TaxiOptimizer;
import org.matsim.contrib.taxi.passenger.SubmittedTaxiRequestsCollector;
import org.matsim.contrib.taxi.passenger.TaxiRequestCreator;
import org.matsim.contrib.taxi.scheduler.TaxiScheduler;
import org.matsim.contrib.taxi.vrpagent.TaxiActionCreator;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * @author michalm
 */
public class MultiModeTaxiQSimModule extends AbstractMultiModeQSimModule {
	private final TaxiConfigGroup taxiCfg;

	public MultiModeTaxiQSimModule(TaxiConfigGroup taxiCfg) {
		super(taxiCfg.getMode());
		this.taxiCfg = taxiCfg;
	}

	@Override
	protected void configureQSim() {
		bindModal(TaxiOptimizer.class).toProvider(
				new ModalProviders.AbstractProvider<TaxiOptimizer>(taxiCfg.getMode()) {
					@Inject
					@Named(DvrpRoutingNetworkProvider.DVRP_ROUTING)
					private Network network;

					@Inject
					private MobsimTimer timer;

					@Inject
					@Named(DvrpTravelTimeModule.DVRP_ESTIMATED)
					private TravelTime travelTime;

					@Inject
					@Taxi
					private TravelDisutility travelDisutility;

					@Inject
					private EventsManager events;

					@Override
					public TaxiOptimizer get() {
						Fleet fleet = getModalInstance(Fleet.class);
						TaxiScheduler taxiScheduler = getModalInstance(TaxiScheduler.class);
						return new DefaultTaxiOptimizerProvider(taxiCfg, fleet, network, timer, travelTime,
								travelDisutility, taxiScheduler).get();
					}
				}).asEagerSingleton();

		bindModal(TaxiScheduler.class).toProvider(
				new ModalProviders.AbstractProvider<TaxiScheduler>(taxiCfg.getMode()) {
					@Inject
					@Named(DvrpRoutingNetworkProvider.DVRP_ROUTING)
					private Network network;

					@Inject
					private MobsimTimer timer;

					@Inject
					@Named(DvrpTravelTimeModule.DVRP_ESTIMATED)
					private TravelTime travelTime;

					@Inject
					@Taxi
					private TravelDisutility travelDisutility;

					@Override
					public TaxiScheduler get() {
						Fleet fleet = getModalInstance(Fleet.class);
						return new TaxiScheduler(taxiCfg, fleet, network, timer, travelTime, travelDisutility);
					}
				}).asEagerSingleton();

		bindModal(DynActionCreator.class).toProvider(
				new ModalProviders.AbstractProvider<TaxiActionCreator>(taxiCfg.getMode()) {
					@Inject
					private MobsimTimer timer;

					@Inject
					private DvrpConfigGroup dvrpCfg;

					@Override
					public TaxiActionCreator get() {
						PassengerEngine passengerEngine = getModalInstance(PassengerEngine.class);
						TaxiOptimizer optimizer = getModalInstance(TaxiOptimizer.class);
						return new TaxiActionCreator(passengerEngine, taxiCfg, optimizer, timer, dvrpCfg);
					}
				}).asEagerSingleton();

		bindModal(PassengerRequestCreator.class).toProvider(
				modalProvider(getter -> new TaxiRequestCreator(getter.getModal(SubmittedTaxiRequestsCollector.class))))
				.asEagerSingleton();

		bindModal(VrpOptimizer.class).to(modalKey(TaxiOptimizer.class));
	}
}
