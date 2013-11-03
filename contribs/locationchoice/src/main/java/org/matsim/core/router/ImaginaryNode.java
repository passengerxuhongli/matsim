/* *********************************************************************** *
 * project: org.matsim.*
 * ImaginaryNode.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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

package org.matsim.core.router;

import java.util.Collection;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.utils.geometry.CoordImpl;

public class ImaginaryNode implements Node {

	/*package*/ final Collection<InitialNode> initialNodes;
	/*package*/ final Coord coord;
	
	public ImaginaryNode(Collection<InitialNode> initialNodes, Coord coord) {
		this.initialNodes = initialNodes;
		this.coord = coord;
	}
	
	public ImaginaryNode(Collection<InitialNode> initialNodes) {
		this.initialNodes = initialNodes;
		
		double sumX = 0.0;
		double sumY = 0.0;
		
		for (InitialNode initialNode : initialNodes) {
			sumX += initialNode.node.getCoord().getX();
			sumY += initialNode.node.getCoord().getY();
		}
		
		sumX /= initialNodes.size();
		sumY /= initialNodes.size();
		
		this.coord = new CoordImpl(sumX, sumY);
	}
	
	@Override
	public Coord getCoord() {
		return this.coord;
	}

	@Override
	public Id getId() {
		return null;
	}

	@Override
	public boolean addInLink(Link link) {
		return false;
	}

	@Override
	public boolean addOutLink(Link link) {
		return false;
	}

	@Override
	public Map<Id, ? extends Link> getInLinks() {
		return null;
	}

	@Override
	public Map<Id, ? extends Link> getOutLinks() {
		return null;
	}
}