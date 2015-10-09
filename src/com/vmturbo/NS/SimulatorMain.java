/**
 *
 */
package com.vmturbo.NS;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author kunal, shangshang, pamela
 *
 */
public class SimulatorMain {

    ArrayList<SpineSwitch> spineList;
    ArrayList<ToRSwitch> torList;
    ArrayList<Host> hostList;
    ArrayList<Link> linkList;
    ArrayList<FlowEvent> flowQueue;


    private static String TOPOFILE = "input/topology";
    private static String QUEUEFILE = "input/overFlow";



    public static void main(String[] args) {
        SimulatorMain simulator = new SimulatorMain();
        LinkMetric metric = new LinkMetric();
        Map<Flow, Double> throughput = new LinkedHashMap<>();

        // discover the topology
        TopologySetup topo = TopologySetup.getInstance();
        topo.setTopologyFileName(TOPOFILE);
        topo.parseFile();

        // populate the FlowQueue
        FlowQueueSetup queue = new FlowQueueSetup(QUEUEFILE);
        queue.populateQueue();
        simulator.flowQueue = queue.getFlowQueue();
        System.out.println("Size of flow event queue: " + simulator.flowQueue.size());

        // compute all the paths and store in matrix containing list of all possible paths for each src, dest pair
        ComputePaths comPaths = new ComputePaths(topo.spineList, topo.torList, topo.hostList,
                                                 topo.linkList);
        comPaths.findPaths();
        ArrayList<Path> allPaths;

        // Initial link utilization
        metric.calculateLinkUtil(topo.linkList);
        System.out.println("Link Utilization initially:");
        metric.printMetrics(topo.linkList);

        ECMPPlacement ecmp = new ECMPPlacement(topo.spineList, topo.torList,
                                               topo.hostList, topo.linkList);

        // find random placement
        // find random placement
        for (FlowEvent flowEvent : simulator.flowQueue) {
            Flow flow = flowEvent.getFlow();
            switch (flowEvent.getFlowEventType()) {
                case END:
                    if (flow.getPath() != null)
                        flow.getPath().removeFlow(flow);
                    System.err.println("Flow event end " + flow.getSource()
                                       + "\tdest:"
                                       + flow.getDest()
                                       + "\t Start: "
                                       + flow.getStart() + "\t Bandwidth: "
                                       + flow.getBandwidth());
                    metric.calculateLinkUtil(topo.linkList);
                    metric.printMetrics(topo.linkList);
                    break;
                case START:
                    //list of all possible paths for thsi src and dest
                    allPaths = comPaths.getPaths(flow.getSource(), flow.getDest());
                    if (allPaths.isEmpty()) {
                        System.err.println("No path found for flow: source: " + flow.getSource()
                                           + "\tdest:"
                                           + flow.getDest()
                                           + "\t Start: "
                                           + flow.getStart() + "\t Bandwidth: "
                                           + flow.getBandwidth());
                    }



                    //Path pathSelected = ecmp.recommendPath(flow);
                    // Path pathSelected = RandomPlacement.randomPlacement(flow, allPaths);
                    Path pathSelected = EconomicPlacement.econPlacement(flow, allPaths);
                    System.out.println("path select" + pathSelected);
                    if (pathSelected == null) {
                        throughput.put(new Flow(flow), 0.0);
                        flow.setBandwith(0);
                    }
                    else {
                        double capacityLeft = Utility.formatDouble(pathSelected.capacityLeft(), 4);
                        if (flow.getBandwidth() <= capacityLeft) {
                            pathSelected.placeFlow(flow);
                            throughput.put(new Flow(flow), flow.getBandwidth());
                        }
                        else { //pathSelected can only serve part of the flow                        
                            throughput.put(new Flow(flow), capacityLeft);
                            flow.setBandwith(capacityLeft);
                            pathSelected.placeFlow(flow);
                        }
                    }


                    metric.calculateLinkUtil(topo.linkList);
                    System.err.println("Link Utilization after placing Flow: "
                                       + flow.getSource().getName()
                                       + " -> "
                                       + flow.getDest().getName()
                                       + " Flow bandwidth: "
                                       + flow.getBandwidth());
                    metric.printMetrics(topo.linkList);
                    break;
                default:
                    break;
            }

        }
        metric.printAggrMetrics();
        System.out.println(throughput);

    }

}
