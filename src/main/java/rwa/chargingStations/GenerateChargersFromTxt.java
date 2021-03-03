/* *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package rwa.chargingStations;/*
 * created by jbischoff, 12.10.2018
 * edited by mattjweist, 19.02.2021
 * has not been tested in RWA package, could possibly require updating pom.xml
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.ParseException;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.ev.EvUnits;
import org.matsim.contrib.ev.infrastructure.Charger;
import org.matsim.contrib.ev.infrastructure.ChargerSpecification;
import org.matsim.contrib.ev.infrastructure.ChargerWriter;
import org.matsim.contrib.ev.infrastructure.ImmutableChargerSpecification;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.filter.NetworkFilterManager;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

public class GenerateChargersFromTxt {

    public static void main(String[] args) throws IOException, ParseException {
        String folder = "C:/Users/Weist/Desktop/github/matsim-rwa/scenarios/rwa/";

        CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, TransformationFactory.WGS84_UTM33N);
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(folder + "bigroadsnetwork.xml");
        NetworkFilterManager nfm = new NetworkFilterManager(network);
        nfm.addLinkFilter(l -> {
            if (l.getAllowedModes().contains(TransportMode.car)) return true;
            else return false;
        });
        Network filteredNet = nfm.applyFilters();
        
        
        Map<Id<Charger>, ChargerSpecification> chargers = new HashMap<>();
        
        // read in the .txt file created from open charge maps API
        // parse the .txt file for charging station properties
        BufferedReader br = new BufferedReader(new FileReader("C:/Users/Weist/Desktop/RWA/Charging_stations/ChgStations.txt"));  
        String line = null;  
        while ((line = br.readLine()) != null) {
        	String[] chargerProperties = line.split(",");
        	String idNumber = chargerProperties[0];
        	double lon = Double.parseDouble(chargerProperties[1]);
        	double lat = Double.parseDouble(chargerProperties[2]);
        	double power = Double.parseDouble(chargerProperties[3]);
        	int quantity = Integer.parseInt(chargerProperties[4]);
            Coord c = ct.transform(new Coord(lon, lat));
            Link l = NetworkUtils.getNearestLink(filteredNet, c);
            // Id<Charger> carCharger = Id.create(l.getId().toString() + "fast", Charger.class);
            // use original charger ID instead
            Id<Charger> carCharger = Id.create(idNumber, Charger.class);
			ChargerSpecification fastCharger = ImmutableChargerSpecification.newBuilder()
                    .id(carCharger)
					.plugPower(power * EvUnits.W_PER_kW)
					.plugCount(quantity)
					.linkId(l.getId()) 
					.chargerType("fast")
					// "RunEvExample" from ev contrib will fail if chargerType is included in chargers.xml
					.build();
            chargers.put(carCharger, fastCharger);
        }
        new ChargerWriter(chargers.values().stream()).write(folder + "chargers.xml");
    }
}
