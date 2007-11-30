/* *********************************************************************** *
 * project: org.matsim.*
 * BarChartUtil.java
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

/**
 * 
 */
package playground.yu.graphUtils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

/**
 * @author yu
 * 
 */
public class BarChartUtil extends ChartUtil {
	public BarChartUtil(String title, String categoryAxisLabel,
			String valueAxisLabel) {
		super(title, categoryAxisLabel, valueAxisLabel);
	}

	protected JFreeChart createChart(String title, String xAxisLabel,
			String yAxisLabel) {
		chart_ = ChartFactory.createBarChart(title, xAxisLabel, yAxisLabel,
				dataset0, PlotOrientation.VERTICAL, true, // legend?
				true, // tooltips?
				false // URLs?
				);
		return chart_;
	}
}
