/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package playground.michalm.vrp.data.network;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;

import pl.poznan.put.vrp.dynamic.data.network.Vertex;


public interface MatsimVertex
    extends Vertex
{
    //usually getCoord() == getLink().getCoord(); however not always!
    Coord getCoord();


    Link getLink();
}
