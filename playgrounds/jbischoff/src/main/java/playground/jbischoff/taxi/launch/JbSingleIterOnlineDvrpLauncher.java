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

package playground.jbischoff.taxi.launch;

import java.io.*;
import java.util.*;

import org.matsim.analysis.LegHistogram;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.otfvis.OTFVis;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.algorithms.*;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.vis.otfvis.*;

import pl.poznan.put.util.jfreechart.ChartUtils;
import pl.poznan.put.vrp.dynamic.data.model.*;
import pl.poznan.put.vrp.dynamic.data.model.Request.ReqStatus;
import pl.poznan.put.vrp.dynamic.optimizer.taxi.*;
import playground.jbischoff.taxi.evaluation.ScheduleChartUtils;
import playground.jbischoff.taxi.optimizer.rank.NOSRankTaxiOptimizer;
import playground.michalm.util.gis.Schedules2GIS;
import playground.michalm.vrp.RunningVehicleRegister;
import playground.michalm.vrp.data.MatsimVrpData;
import playground.michalm.vrp.otfvis.OTFLiveUtils;
/**
 * 
 * 
 * 
 * @author jbischoff
 *
 */

/*package*/class JbSingleIterOnlineDvrpLauncher
{
    /*package*/final String dirName;
    /*package*/final String netFileName;
    /*package*/final String plansFileName;
    /*package*/final String taxiCustomersFileName;
    /*package*/final String depotsFileName;

    /*package*/final boolean vrpOutFiles;
    /*package*/final String vrpOutDirName;

    /*package*/final boolean outHistogram;
    /*package*/final String histogramOutDirName;

    /*package*/final boolean otfVis;

    /*package*/final boolean writeSimEvents;
    /*package*/final String eventsFileName;

    /*package*/final Scenario scenario;
    

    /*package*/MatsimVrpData data;
    /*package*/AlgorithmConfig algorithmConfig;
    /*package*/LegHistogram legHistogram;

    /*package*/TaxiDelaySpeedupStats delaySpeedupStats;
    private String electricStatsFilename;
    private String electricStatsDir;


    /*package*/JbSingleIterOnlineDvrpLauncher()
    {
//    	dirName = "Z:\\WinHome\\Docs\\maciejewski\\jbtest\\";
//    	dirName = "Z:\\WinHome\\Docs\\svn-checkouts\\jbischoff\\jbmielec\\";
    	dirName = "C:\\local_jb\\Dropbox\\MasterOfDesaster\\jbischoff\\jbmielec\\";
        netFileName = dirName + "network.xml";
        electricStatsFilename = dirName + "elstats.txt";
        electricStatsDir = dirName +"electric_depots\\";
        plansFileName = dirName + "20.plans.xml.gz";

        taxiCustomersFileName = dirName + "taxiCustomers_05_pc.txt";
        // taxiCustomersFileName = dirName + "taxiCustomers_10_pc.txt";

        depotsFileName = dirName + "depots-5_taxis-50.xml";
//         depotsFileName = dirName + "depots-5_taxis-100.xml";

        // reqIdToVehIdFileName = dirName + "reqIdToVehId";

        eventsFileName = dirName + "20.events.xml.gz";

        // algorithmConfig = AlgorithmConfig.NOS_STRAIGHT_LINE;
//         algorithmConfig = AlgorithmConfig.NOS_TRAVEL_DISTANCE;
        algorithmConfig = AlgorithmConfig.NOS_FREE_FLOW;
        // algorithmConfig = AlgorithmConfig.NOS_24_H;
        // algorithmConfig = AlgorithmConfig.NOS_15_MIN;
        // algorithmConfig = AlgorithmConfig.OTS_REQ_FREE_FLOW;
        // algorithmConfig = AlgorithmConfig.OTS_REQ_24_H;
        // algorithmConfig = AlgorithmConfig.OTS_REQ_15_MIN;
        // algorithmConfig = AlgorithmConfig.OTS_DRV_FREE_FLOW;
        // algorithmConfig = AlgorithmConfig.OTS_DRV_24_H;
        // algorithmConfig = AlgorithmConfig.OTS_DRV_15_MIN;
        // algorithmConfig = AlgorithmConfig.RES_REQ_FREE_FLOW;
        // algorithmConfig = AlgorithmConfig.RES_REQ_24_H;
        // algorithmConfig = AlgorithmConfig.RES_REQ_15_MIN;
        // algorithmConfig = AlgorithmConfig.RES_DRV_FREE_FLOW;
        // algorithmConfig = AlgorithmConfig.RES_DRV_24_H;
        // algorithmConfig = AlgorithmConfig.RES_DRV_15_MIN;

        otfVis = !true;

        vrpOutFiles = !true;
        vrpOutDirName = dirName + "vrp_output";

        outHistogram = false;
        histogramOutDirName = dirName + "histograms";

        writeSimEvents = true;

        scenario = ElectroCabLaunchUtils.initMatsimData(netFileName, plansFileName,
                taxiCustomersFileName);
    }


    /*package*/JbSingleIterOnlineDvrpLauncher(String paramFile)
    {
        Scanner scanner;
        try {
            scanner = new Scanner(new BufferedReader(new FileReader(paramFile)));
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Map<String, String> params = new HashMap<String, String>();

        while (scanner.hasNext()) {
            String key = scanner.next();
            String value = scanner.next();
            params.put(key, value);
        }

        dirName = params.get("dirName") + '\\';
        
        
        netFileName = dirName + params.get("netFileName");

        plansFileName = dirName + params.get("plansFileName");

        taxiCustomersFileName = dirName + params.get("taxiCustomersFileName");

        depotsFileName = dirName + params.get("depotsFileName");

        eventsFileName = dirName + params.get("eventsFileName");

        algorithmConfig = AlgorithmConfig.ALL[Integer.valueOf(params.get("algorithmConfig"))];

        otfVis = Boolean.valueOf(params.get("otfVis"));

        vrpOutFiles = Boolean.valueOf(params.get("vrpOutFiles"));
        vrpOutDirName = dirName + params.get("vrpOutDirName");

        outHistogram = Boolean.valueOf(params.get("outHistogram"));
        histogramOutDirName = dirName + params.get("histogramOutDirName");

        writeSimEvents = Boolean.valueOf(params.get("writeSimEvents"));

        scenario = ElectroCabLaunchUtils.initMatsimData(netFileName, plansFileName,
                taxiCustomersFileName);
    }


    /**
     * Can be called several times (1 call == 1 simulation)
     */
    /*package*/void go()
    {

    	File f = new File(electricStatsDir);
    	f.mkdirs();
    	
    	ElectroCabLaunchUtils olutils = new ElectroCabLaunchUtils();
    	if (scenario == null) System.out.println("scen");
    	if (algorithmConfig.ttimeSource == null) System.out.println("ttsource");
    	if (algorithmConfig.tcostSource == null) System.out.println("tcostSource");
    	if (eventsFileName == null) System.out.println("eventsFileName");
    	if (depotsFileName == null) System.out.println("depotsFileName");
        data = olutils.initMatsimVrpData(scenario, algorithmConfig.ttimeSource,
                algorithmConfig.tcostSource, eventsFileName, depotsFileName);

        NOSRankTaxiOptimizer optimizer = algorithmConfig.createTaxiOptimizer(data
                .getVrpData(),true);
//        optimizer.setDelaySpeedupStats(delaySpeedupStats);
        

        QSim qSim = olutils.initQSim(data, optimizer);
        
        EventsManager events = qSim.getEventsManager();

        EventWriter eventWriter = null;
        if (writeSimEvents) {
            eventWriter = new EventWriterXML(electricStatsDir + "events.xml.gz");
            events.addHandler(eventWriter);
        }

        RunningVehicleRegister rvr = new RunningVehicleRegister();
        events.addHandler(rvr);

        if (otfVis) { // OFTVis visualization
            OTFLiveUtils.initQueryHandler(qSim, data.getVrpData());
            OnTheFlyServer server = OTFVis.startServerAndRegisterWithQSim(scenario.getConfig(),
                    scenario, qSim.getEventsManager(), qSim);
            OTFClientLive.run(scenario.getConfig(), server);
        }

        if (outHistogram) {
            events.addHandler(legHistogram = new LegHistogram(300));
        }

        qSim.run();

        events.finishProcessing();

        if (writeSimEvents) {
            eventWriter.closeFile();
        }
//        olutils.printStatisticsToConsole();
        olutils.writeStatisticsToFiles(electricStatsDir);
        
        // check if all reqs have been served
        for (Request r : data.getVrpData().getRequests()) {
            if (r.getStatus() != ReqStatus.PERFORMED) {
                throw new IllegalStateException();
            }
        }
    }


    /*package*/void generateOutput()
    {
        PrintWriter pw = new PrintWriter(System.out);
        new TaxiEvaluator().evaluateVrp(data.getVrpData()).print(pw);
        pw.flush();

        if (vrpOutFiles) {
            new Schedules2GIS(data.getVrpData().getVehicles(), data).write(vrpOutDirName);
        }

        // ChartUtils.showFrame(RouteChartUtils.chartRoutesByStatus(data.getVrpData()));
        ChartUtils.showFrame(ScheduleChartUtils.chartSchedule(data.getVrpData()));

        if (outHistogram) {
            ElectroCabLaunchUtils.writeHistograms(legHistogram, histogramOutDirName);
        }
    }


    public static void main(String... args)
    {
        JbSingleIterOnlineDvrpLauncher launcher;
        if (args.length == 0) {
            launcher = new JbSingleIterOnlineDvrpLauncher();
        }
        else if (args.length == 1) {
            launcher = new JbSingleIterOnlineDvrpLauncher(args[0]);
        }
        else {
            throw new RuntimeException();
        }

        launcher.go();
        launcher.generateOutput();
    }
}
