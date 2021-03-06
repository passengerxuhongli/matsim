/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package org.matsim.contrib.accessibility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.accessibility.interfaces.FacilityDataExchangeInterface;
import org.matsim.contrib.matrixbasedptrouter.PtMatrix;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * @author dziemke
 */
public final class AccessibilityShutdownListenerV4 implements ShutdownListener {
	private static final Logger LOG = Logger.getLogger(AccessibilityShutdownListenerV4.class);

	private final AccessibilityCalculator accessibilityCalculator;
	private String outputDirectory;
	private AccessibilityConfigGroup acg;
	
	private String outputSubdirectory; // For consideration of different activity types subdirectories are required in order not to confuse the output
	private AccessibilityAggregator accessibilityAggregator;
	private PtMatrix ptMatrix;
	private ActivityFacilities opportunities;
	
	private ActivityFacilities measurePoints;
	private Map<Id<ActivityFacility>, Geometry> measurePointGeometryMap;

	
	public AccessibilityShutdownListenerV4(AccessibilityCalculator accessibilityCalculator, ActivityFacilities opportunities, 
			PtMatrix ptMatrix, String outputDirectory, AccessibilityConfigGroup acg, Map<Id<ActivityFacility>, Geometry> measurePointGeometryMap, ActivityFacilities measurePoints) {
		this.ptMatrix = ptMatrix;
		this.opportunities = opportunities;
		this.accessibilityCalculator = accessibilityCalculator;
		this.outputDirectory = outputDirectory;
		this.acg = acg;
		this.measurePointGeometryMap = measurePointGeometryMap;
		this.measurePoints = measurePoints;
	}

	private List<ActivityFacilities> additionalFacilityData = new ArrayList<>() ;
	private boolean lockedForAdditionalFacilityData = false;

	@Override
	public void notifyShutdown(ShutdownEvent event) {
		if (event.isUnexpected()) {
			return;
		}
		LOG.info("Initializing accessibility computation...");
		accessibilityAggregator = new AccessibilityAggregator();
		accessibilityCalculator.addFacilityDataExchangeListener(accessibilityAggregator);

		lockedForAdditionalFacilityData = true;

		if (outputSubdirectory != null) {
			File file = new File(outputDirectory + "/" + outputSubdirectory);
			file.mkdirs();
		}

		assignAdditionalFacilitiesDataToMeasurePoint();

		LOG.info("Start computing accessibilities.");
		accessibilityCalculator.computeAccessibilities(acg.getTimeOfDay(), opportunities);
		LOG.info("Finished computing accessibilities.");

		// In case multiple accessibility listeners are used, subdirectories are required in order not to confuse the output
		if (outputSubdirectory == null) {
			writeOutputFile(outputDirectory);
		} else {
			writeOutputFile(outputDirectory + "/" + outputSubdirectory);
		}
	}

	private void assignAdditionalFacilitiesDataToMeasurePoint() {
		LOG.info("Start assigning additional facilities data to measure point.");
		GeometryFactory geometryFactory = new GeometryFactory();
		
		for (ActivityFacilities additionalDataFacilities : this.additionalFacilityData) { // Iterate over all additional data collections
			String additionalDataName = additionalDataFacilities.getName();
			int additionalDataFacilitiesToAssign = additionalDataFacilities.getFacilities().size();
			
			for (Id<ActivityFacility> measurePointId : measurePoints.getFacilities().keySet()) { // Iterate over all measure points
				ActivityFacility measurePoint = measurePoints.getFacilities().get(measurePointId);
				measurePoint.getAttributes().putAttribute(additionalDataName, 0);
				Geometry geometry = measurePointGeometryMap.get(measurePointId);
				
				for (ActivityFacility facility : additionalDataFacilities.getFacilities().values()) { // Iterate over additional-data facilities
					Point point = geometryFactory.createPoint(new Coordinate(facility.getCoord().getX(), facility.getCoord().getY()));
					if (geometry.contains(point)) {
						measurePoint.getAttributes().putAttribute(additionalDataName, (int) measurePoint.getAttributes().getAttribute(additionalDataName) + 1);
						additionalDataFacilitiesToAssign--;
					}
				}
			}
			LOG.warn(additionalDataFacilitiesToAssign + " have not been assigned to a measure point geometry.");
		}
		LOG.info("Finished assigning additional facilities data to measure point.");
	}

	private void writeOutputFile(String adaptedOutputDirectory) {
		LOG.info("Start writing accessibility output to " + adaptedOutputDirectory + ".");

		Map<Tuple<ActivityFacility, Double>, Map<String,Double>> accessibilitiesMap = accessibilityAggregator.getAccessibilitiesMap();
		final CSVWriter writer = new CSVWriter(adaptedOutputDirectory + "/" + CSVWriter.FILE_NAME ) ;

		// Write header
		writer.writeField(Labels.X_COORDINATE);
		writer.writeField(Labels.Y_COORDINATE);
		writer.writeField(Labels.TIME);
		for (String mode : accessibilityCalculator.getModes() ) {
			writer.writeField(mode + "_accessibility");
		}
		for (ActivityFacilities additionalDataFacilities : this.additionalFacilityData) { // Iterate over all additional data collections
			String additionalDataName = additionalDataFacilities.getName();
			writer.writeField(additionalDataName);
			writer.writeField(additionalDataName); // TODO Only here for backwards comparability
		}
		writer.writeNewLine();

		// Write data
		for (Tuple<ActivityFacility, Double> tuple : accessibilitiesMap.keySet()) {
			ActivityFacility facility = tuple.getFirst();
			writer.writeField(facility.getCoord().getX());
			writer.writeField(facility.getCoord().getY());
			writer.writeField(tuple.getSecond());
			
			for (String mode : accessibilityCalculator.getModes() ) {
				final double value = accessibilitiesMap.get(tuple).get(mode);
				if (!Double.isNaN(value)) { 
					writer.writeField(value) ;
				} else {
					writer.writeField(Double.NaN) ;
				}
			}
			for (ActivityFacilities additionalDataFacilities : this.additionalFacilityData) { // Again: Iterate over all additional data collections
				String additionalDataName = additionalDataFacilities.getName();
				int value = (int) facility.getAttributes().getAttribute(additionalDataName);
				writer.writeField(value);
				writer.writeField(value); // TODO Only here for backwards comparability
			}
			writer.writeNewLine();
		}
		writer.close() ;
		LOG.info("Finished writing accessibility output to " + adaptedOutputDirectory + ".");
	}
	
	public void addAdditionalFacilityData(ActivityFacilities facilities) {
		if (this.lockedForAdditionalFacilityData) {
			throw new RuntimeException("too late for adding additional facility data; spatial grids have already been generated.  Needs"
					+ " to be called before generating the spatial grids.  (This design should be improved ..)") ;
		}
		if (facilities.getName() == null || facilities.getName().equals("")) {
			throw new RuntimeException("Cannot add unnamed facility containers here. A key is required to identify them.") ;
		}
		for (ActivityFacilities existingFacilities : this.additionalFacilityData) {
			if (existingFacilities.getName().equals(facilities.getName())) {
				throw new RuntimeException("Additional facilities under the name of + " + facilities.getName() + 
						" already exist. Cannot add additional facilities under the same name twice.") ;
			}
		}
		this.additionalFacilityData.add( facilities ) ;
	}

	/**
	 * Use this method to change the folder structure of the output. The output will be written into the subfolder, which is needed if
	 * more than one listener is added since otherwise the output would be overwritten and not be available for analyses anymore.
	 */
	public void writeToSubdirectoryWithName(String subdirectory) {
		this.outputSubdirectory = subdirectory;
	}
	
	public void addFacilityDataExchangeListener( FacilityDataExchangeInterface facilityDataExchangeListener ) {
		this.accessibilityCalculator.addFacilityDataExchangeListener(facilityDataExchangeListener);
	}
}