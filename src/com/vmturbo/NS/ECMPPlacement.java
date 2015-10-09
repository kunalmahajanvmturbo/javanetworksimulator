/**
 * @author shangshangchen
 */
package com.vmturbo.NS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;


public class ECMPPlacement {

    private Flow flow;
    private Host source;
    private Host dest;

    private ArrayList<SpineSwitch> spines;
    private ArrayList<ToRSwitch> tors;
    private ArrayList<Host> hosts;
    private ArrayList<Link> links;

    //"distances" stores shortest distance from every switch to every host
    //it  will be used to choose the best next-hops
    private Map<Node, Map<Host, Integer>> distances;
    private Map<Node, Map<Host, ArrayList<Link>>> bests;
    //used for roundRobin to keep track of least recently used link
    private Map<Link, Integer> timeStamp;
    private int t = 0;

    //constructor: stores topology info
    public ECMPPlacement(ArrayList<SpineSwitch> spines, ArrayList<ToRSwitch> tors,
                    ArrayList<Host> hosts, ArrayList<Link> links) {

        this.spines = spines;
        this.tors = tors;
        this.hosts = hosts;
        this.links = links;

        this.distances = new TreeMap<>();
        this.bests = new TreeMap<>();
        this.timeStamp = new HashMap<>();
        fillDistiances();
        populateBests();
        initializeTimeStamp();


    }

    //suggest a path for a particular flow
    //return null if destination can't be reached from source
    public Path recommendPath(Flow flow) {

        this.flow = flow;
        this.source = flow.getSource();
        this.dest = flow.getDest();

        if (source.equals(dest)) {
            System.out.println("ECMP.recommendPath: flow has same source and destination. " + flow);
            return null;
        }


        //===========compute a path hop by hop=================

        //initialize
        ArrayList<Link> pathLinks = new ArrayList<>();
        Path path = new Path(this.source, this.dest, pathLinks);
        Node current = flow.getSource();

        //choose a best next-hop        
        while (!current.equals(this.dest)) {
            ArrayList<Link> bestLinks = bests.get(current).get(this.dest);

            if (bestLinks == null) { //"null" means that the entry hasn't been cached.
                ArrayList<Node> nextSwitches = new ArrayList<>();
                if (current instanceof Host) {
                    //i.e. current == source, because host can't be intermediary nodes
                    //next-hops are the ToR switches it's connected to
                    nextSwitches.addAll(((Host)current).getToRSwitch());
                    //get the best next-hops
                    bestLinks = getBestLinks(current, nextSwitches);
                }
                else if (current instanceof ToRSwitch) {
                    //two cases here:
                    //if current tor is directly connected to destination host,
                    //then the best next-hop IS the destination
                    if (((ToRSwitch)current).getHostList().contains(dest)) {
                        bestLinks = Utility.getMultiLinks(current, dest, this.links);
                    }
                    else { //otherwise, go through spines
                        nextSwitches.addAll(((ToRSwitch)current).getSpineList());
                        bestLinks = getBestLinks(current, nextSwitches);
                    }
                }
                else if (current instanceof SpineSwitch) {
                    nextSwitches.addAll(((SpineSwitch)current).getToRList());
                    bestLinks = getBestLinks(current, nextSwitches);
                }
                //bestLinks may be empty, but can't be null, due to the methods used to get them:
                //i.e this.getBestLinks(); Utility.getMultiLinks()
                //now we can cache it in "bests"
                bests.get(current).put(this.dest, bestLinks);
            }


            //Link linkSelected = getRandomLink(bestLinks);
            Link linkSelected = roundRobin(bestLinks);
            if (linkSelected == null) {
                System.out.println("ECMP.recommendPath: can't find next-hop after " + path);
                return null;
            }
            pathLinks.add(linkSelected);
            current = linkSelected.getDestNode();
        }


        return path;
    }

    /**
     * find all links leading to the best next-hops, i.e. "best links"
     * @param current: can be a host (i.e. source), or a switch (i.e. intermediary node) 
     * @param nextSwitches: a collection of next-hop switches that "current" is connected to
     * @return: empty ArrayList if destination is unreachable from "current"
     */
    private ArrayList<Link> getBestLinks(Node current, Collection<Node> nextSwitches) {
        ArrayList<Link> bestLinks = new ArrayList<>();
        if (nextSwitches == null || nextSwitches.isEmpty()) {
            System.out.println("ECMP.getBestLinks: no switch is connected to " + current);
            return bestLinks;
        }

        //find minDistance from nextSwitches to destination host
        int minDistance = Integer.MAX_VALUE;
        for (Node sw : nextSwitches) {
            int distance = distances.get(sw).get(dest);
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        //if destination host is unreachable from "current" node, return empty ArrayList
        if (minDistance == Integer.MAX_VALUE) {
            System.out.println("ECMP.getBestLinks: no switch connected to " + current
                               + " leads to "
                               + this.dest);
            return bestLinks;
        }
        //else, find all links leading to switches with minDistance
        for (Node sw : nextSwitches) {
            int distance = distances.get(sw).get(dest);
            if (distance == minDistance) {
                bestLinks.addAll(Utility.getMultiLinks(current, sw, this.links));
            }
        }
        return bestLinks;
    }


    /**
     * Helper function, declared public so that other classes might use it
     * @param myLinks: shouldn't contain any null link
     * @return a random link among myLinks. Can return "null" if myLinks has no element.
     */
    public static Link getRandomLink(ArrayList<Link> myLinks) {

        if (myLinks == null || myLinks.isEmpty()) {
            System.out.println("ECMP.getRandomLink: ArrayList passed is null or empty");
            return null;
        }
        if (myLinks.contains(null)) {
            System.out.println("ECMP.getRandomLink: ArrayList passed contains null link");
            return null;
        }
        Random rand = new Random();
        int n = rand.nextInt(myLinks.size());
        return myLinks.get(n);
    }

    private Link roundRobin(ArrayList<Link> myLinks) {
        if (myLinks == null || myLinks.isEmpty()) {
            System.out.println("ECMP.roundRobin: ArrayList passed is null or empty");
            return null;
        }
        if (myLinks.contains(null)) {
            System.out.println("ECMP.roundRobin: ArrayList passed contains null link");
            return null;
        }

        Link next = myLinks.get(0);
        int earliest = Integer.MAX_VALUE;
        for (Link link : myLinks) {
            if (timeStamp.get(link) < earliest) {
                earliest = timeStamp.get(link);
                next = link;
            }
        }

        timeStamp.put(next, t);
        t++;
        return next;

    }

    /**
     * BFS only computes distance between switches,
     * we need distance between switches and hosts,
     * but we can use BFS to compute this.
     */
    private void fillDistiances() {

        //run BFS
        ArrayList<Node> switches = new ArrayList<>();
        switches.addAll(this.spines);
        switches.addAll(this.tors);
        BFS bfs = new BFS(switches);
        bfs.run();

        //use BFS results to populate our matrix
        for (Node sw : switches) {
            Map<Host, Integer> row = new TreeMap<>();

            for (Host host : this.hosts) { //find (shortest) distance between sw and host                
                int distance;
                ArrayList<ToRSwitch> destToRs = host.getToRSwitch();


                if (destToRs.contains(sw)) { //if sw is a ToR connected to host, 
                    distance = 1;            //then distance must be 1 (can't be shorter than that)
                }
                else { //i.e. sw is not directly connected to host
                       //find the closest ToR among all ToRs the host is connected to
                       //distance(sw, host) = distance(sw, closest ToR) + 1
                    distance = Integer.MAX_VALUE;
                    for (Node destToR : destToRs) {
                        if (bfs.getDistance(sw, destToR) == Integer.MAX_VALUE) {
                            continue;
                        }
                        int newDistance = bfs.getDistance(sw, destToR) + 1;
                        if (newDistance < distance) {
                            distance = newDistance;
                        }
                    }
                }
                row.put(host, distance);
            }
            distances.put(sw, row);
        }
    }

    private void populateBests() {
        ArrayList<Node> allNodes = new ArrayList<>();
        allNodes.addAll(this.hosts);
        allNodes.addAll(this.spines);
        allNodes.addAll(this.tors);
        for (Node node : allNodes) {
            Map<Host, ArrayList<Link>> row = new TreeMap<>();
            for (Host host : this.hosts) {
                row.put(host, null);
            }
            bests.put(node, row);
        }

    }

    private void initializeTimeStamp() {
        for (Link link : this.links) {
            timeStamp.put(link, -1);
        }
    }

    public void printDistances() {
        //print hosts
        Set<Node> sources = distances.keySet();
        if (sources.iterator().hasNext()) {
            System.out.println("  " + distances.get(sources.iterator().next()).keySet());
        }

        for (Node source : distances.keySet()) {
            String s = source.getName() + ": ";
            for (Node dest : distances.get(source).keySet()) {
                int distance = distances.get(source).get(dest);
                if (distance < Integer.MAX_VALUE) {
                    s += distance + "  ";
                }
                else {
                    s += "-" + "  ";
                }

            }
            System.out.println(s);
        }
    }

    public ArrayList<Link> getBests(Node node, Host host) {
        return bests.get(node).get(host);
    }

    //for testing
    public static void main(String[] args) {

    }

}
