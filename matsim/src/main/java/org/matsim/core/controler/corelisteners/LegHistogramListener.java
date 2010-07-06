/* *********************************************************************** *
 * project: org.matsim.*
 * LegHistogramListener.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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
 * *********************************************************************** */

package org.matsim.core.controler.corelisteners;

import org.apache.log4j.Logger;
import org.matsim.analysis.LegHistogram;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;

/**
 * Integrates the {@link org.matsim.analysis.LegHistogram} into the
 * {@link org.matsim.core.controler.Controler}, so the leg histogram is
 * automatically created every iteration.
 *
 * @author mrieser
 */
public class LegHistogramListener implements IterationEndsListener, IterationStartsListener {

	private final EventsManager events;
	private final LegHistogram histogram;
	private final boolean outputGraph;

	static private final Logger log = Logger.getLogger(LegHistogramListener.class);

	public LegHistogramListener(final EventsManager events, final boolean outputGraph) {
		this.events = events;
		this.histogram = new LegHistogram(300);
		this.outputGraph = outputGraph;
		this.events.addHandler(this.histogram);
	}

	@Override
	public void notifyIterationStarts(final IterationStartsEvent event) {
		this.histogram.reset(event.getIteration());
	}

	@Override
	public void notifyIterationEnds(final IterationEndsEvent event) {
		this.histogram.write(event.getControler().getControlerIO().getIterationFilename(event.getIteration(), "legHistogram.txt"));
		this.printStats();
		if (this.outputGraph) {
			this.histogram.writeGraphic(event.getControler().getControlerIO().getIterationFilename(event.getIteration(), "legHistogram_all.png"));
			for (String legMode : this.histogram.getLegModes()) {
				this.histogram.writeGraphic(event.getControler().getControlerIO().getIterationFilename(event.getIteration(), "legHistogram_" + legMode + ".png"), legMode);
			}
		}

	}

	public void printStats() {
		int nofLegs = 0;
		for (int nofDepartures : this.histogram.getDepartures()) {
			nofLegs += nofDepartures;
		}
		log.info("number of legs:\t"  + nofLegs + "\t100%");
		for (String legMode : this.histogram.getLegModes()) {
			int nofModeLegs = 0;
			for (int nofDepartures : this.histogram.getDepartures(legMode)) {
				nofModeLegs += nofDepartures;
			}
			if (nofModeLegs != 0) {
				log.info("number of " + legMode + " legs:\t"  + nofModeLegs + "\t" + (nofModeLegs * 100.0 / nofLegs) + "%");
			}
		}
	}

}
