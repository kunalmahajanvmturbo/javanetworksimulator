/**
 * @author Kunal
 */
package com.vmturbo.NS;

import java.util.ArrayList;
import java.util.Collection;

public class LinkMetric {

    private double avgLinkUtilHosttoToR;
    private double maxLinkUtilHosttoToR;
    private double stdevLinkUtilHosttoToR;
    private double avgLinkUtilToRtoSpine;
    private double maxLinkUtilToRtoSpine;
    private double stdevLinkUtilToRtoSpine;

    //aggregated metrics: HT means HosttoTor; TS means TortoSpine
    private int counter = 0;
    private double aggr_avgHT;
    private double aggr_maxHT;
    private double aggr_stdevHT;
    private double aggr_avgTS;
    private double aggr_maxTS;
    private double aggr_stdevTS;



    /**
     * Calculate average, max and std dev of link utilization
     * @param linkList
     */
    public void calculateLinkUtil(ArrayList<Link> linkList) {

        //counter keeps track of how many times metrics are calculated
        counter++;

        double sumUtilHostTor = 0;
        double sumUtilTorSpine = 0;
        double maxHostTor = 0;
        double maxTorSpine = 0;
        double tempUtil = 0;
        double tempStdevHostTor = 0;
        double tempStdevTorSpine = 0;

        // calculate max and average link utilization
        int numHT = 0;
        int numTS = 0;
        for (Link link : linkList) {
            switch (link.getLinkType()) {
                case HOSTTOTOR:
                    numHT++;
                    tempUtil = link.getUtilization();
                    sumUtilHostTor += tempUtil;
                    if (maxHostTor < tempUtil) {
                        maxHostTor = tempUtil;
                    }
                    break;
                case TORTOSPINE:
                    numTS++;
                    tempUtil = link.getUtilization();
                    sumUtilTorSpine += tempUtil;
                    if (maxTorSpine < tempUtil) {
                        maxTorSpine = tempUtil;
                    }
                    break;
            }
        }

        avgLinkUtilHosttoToR = sumUtilHostTor / numHT;
        avgLinkUtilToRtoSpine = sumUtilTorSpine / numTS;
        maxLinkUtilHosttoToR = maxHostTor;
        maxLinkUtilToRtoSpine = maxTorSpine;

        aggr_avgHT += avgLinkUtilHosttoToR;
        aggr_avgTS += avgLinkUtilToRtoSpine;
        aggr_maxHT = Math.max(aggr_maxHT, maxLinkUtilHosttoToR);
        aggr_maxTS = Math.max(aggr_maxTS, maxLinkUtilToRtoSpine);

        // calculate variance
        for (Link link : linkList) {
            switch (link.getLinkType()) {
                case HOSTTOTOR:
                    tempUtil = link.getUtilization();
                    tempStdevHostTor += Math.pow(tempUtil - avgLinkUtilHosttoToR, 2);
                    break;
                case TORTOSPINE:
                    tempUtil = link.getUtilization();
                    tempStdevTorSpine += Math.pow(tempUtil - avgLinkUtilToRtoSpine, 2);
                    break;
            }
        }

        // calculate std dev
        stdevLinkUtilHosttoToR = Math.sqrt(tempStdevHostTor / numHT);
        stdevLinkUtilToRtoSpine = Math.sqrt(tempStdevTorSpine / numTS);

        aggr_stdevHT += stdevLinkUtilHosttoToR;
        aggr_stdevTS += stdevLinkUtilToRtoSpine;
    }

    public double getAvgLinkUtilHostTor() {
        return avgLinkUtilHosttoToR;
    }

    public double getMaxLinkUtilHostTor() {
        return maxLinkUtilHosttoToR;
    }

    public double getStdevLinkUtilHostTor() {
        return stdevLinkUtilHosttoToR;
    }

    public double getAvgLinkUtilTorSpine() {
        return avgLinkUtilToRtoSpine;
    }

    public double getMaxLinkUtilTorSpine() {
        return maxLinkUtilToRtoSpine;
    }

    public double getStdevLinkUtilTorSpine() {
        return stdevLinkUtilToRtoSpine;
    }


    public void printMetrics(ArrayList<Link> linkList) {

        /**
        System.out.println("total num of links: " + linkList.size());
        for (Link link : linkList) {
            System.out.println("Link: " + link.getSrcNode().getName()
                               + " -> "
                               + link.getDestNode().getName()
                               + " ,util: "
                               + link.getUtilization());
        }
        */

        //alternative way to print all links
        Utility.printLinkUsage(linkList, 0.001);



        System.out.println("Avg H to ToR: " + getAvgLinkUtilHostTor()
                           + "\tMax H to ToR: " + getMaxLinkUtilHostTor()
                           + "\tStdev H to ToR: " + getStdevLinkUtilHostTor());
        System.out.println("Avg ToR to Spine: " + getAvgLinkUtilTorSpine()
                           + "\tMax ToR to Spine: " + getMaxLinkUtilTorSpine()
                           + "\tStdev ToR to Spine: " + getStdevLinkUtilTorSpine());

        //PrintPercentageMetrics(linkList);


    }

    //shangshang 
    public void PrintPercentageMetrics(Collection<Link> links) {

        ArrayList<Double> HTPerc = new ArrayList<>(); //util percentages for host-tor links
        ArrayList<Double> TSPerc = new ArrayList<>(); //util percentages for tor-spine links

        for (Link link : links) {
            switch (link.getLinkType()) {
                case HOSTTOTOR:
                    HTPerc.add(link.getPercentage());
                    break;
                case TORTOSPINE:
                    TSPerc.add(link.getPercentage());
                    break;
            }
        }
        System.out.println("---------------Link Util Percentage----------------");
        System.out.println("Avg H to ToR: " + Utility.average(HTPerc)
                           + "\tMax H to ToR: " + Utility.max(HTPerc)
                           + "\tVar H to ToR: " + Utility.variance(HTPerc));
        System.out.println("Avg ToR to Spine: " + Utility.average(TSPerc)
                           + "\tMax ToR to Spine: " + Utility.max(TSPerc)
                           + "\tVar ToR to Spine: " + Utility.variance(TSPerc));



    }

    public void printAggrMetrics() {
        System.out.println("\n=================Aggregated metrics====================\n");

        counter = counter - 2; //-2 to discount the initial and final calculations 
        int decimal = 3;
        System.out.println("Avg H to ToR: "
                           + Utility.formatDouble(aggr_avgHT / counter, decimal)
                           + "\tMax H to ToR: "
                           + Utility.formatDouble(aggr_maxHT, decimal)
                           + "\tStdev H to ToR: "
                           + Utility.formatDouble(aggr_stdevHT / counter, decimal));
        System.out.println("Avg ToR to Spine: "
                           + Utility.formatDouble(aggr_avgTS / counter, decimal)
                           + "\tMax ToR to Spine: "
                           + Utility.formatDouble(aggr_maxTS, decimal)
                           + "\tStdev ToR to Spine: "
                           + Utility.formatDouble(aggr_stdevTS / counter, decimal));
    }



}
