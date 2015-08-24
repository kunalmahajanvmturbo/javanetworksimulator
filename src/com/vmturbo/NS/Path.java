/**
 * @author shangshangchen
 */
package com.vmturbo.NS;

import java.util.ArrayList;

public class Path {

    private Host source;
    private ArrayList<Link> links;
    private Host dest;

    //constructor
    public Path(Host source, Host dest, ArrayList<Link> links) {
        this.links = links;
        this.source = source;
        this.dest = dest;
    }

    public Host getSource() {
        return source;
    }

    public Host getDest() {
        return dest;
    }

    public ArrayList<Link> getLinks() {
        return links;
    }

    @Override
    public String toString() {
        if (links == null) {
            return ("path: " + source.getName() + " -> " + dest.getName());
        }
        else {
            String s = "path: " + source.getName();
            for (Link link : links) {
                s += " -> " + link.getDestNode().getName();
            }
            return s;
        }
    }



    /**
     * used for economic placement
     * assuming we are not doing multi-threading, no need for locks
     * @param flow
     * @return -1 if path can't satisfy flow requirement, an integer quote otherwise
     */
    public int getQuote(Flow flow) {
        if (!flow.getSource().equals(source) || !flow.getDest().equals(dest)) {
            return -1;
        }
        //"demand" is the bandwidth demand of the flow
        double demand = flow.getBandwidth();

        //quote of the path is the sum of link prices
        //link price is calculated as 1/(1-U)^2, U is percentage utilized of the link
        int quote = 0;
        for (Link link : links) {
            double bandwidthLeft = link.getCapacity() - link.getUtilization();
            if (demand >= bandwidthLeft) {
                return -1;
            }
            double percentage = (demand + link.getUtilization()) / link.getCapacity();
            double linkPrice = 1 / Math.pow(1 - percentage, 2);
            quote += linkPrice;
        }
        return quote;
    }


    /**
     * used for economic placement
     * @param flow
     * @return 0 if successful, -1 unsuccessful
     */
    public int placeFlow(Flow flow) {
        if (!flow.getSource().equals(source) || !flow.getDest().equals(dest)) {
            return -1;
        }
        double demand = flow.getBandwidth();
        for (Link link : links) {
            /**
             * For now, just push everything into the link.
             * Ideally, we should set utilization to be min(link.getCapacity(), link.getUtilization() + demand)
             * but this approach might cause unequal addition and removal of bandwidths in 
             * placeFlow() and removeFlow() .
             * The current approach lets you set utilization above link capacity.
             * So when polling link capacity, interpret accordingly
             */
            link.setUtilization(link.getUtilization() + demand);
        }
        return 0;
    }

    /**
     * used for economic placement
     * @param flow
     * @return 0 if successful, -1 unsuccessful
     */
    public int removeFlow(Flow flow) {
        if (!flow.getSource().equals(source) || !flow.getDest().equals(dest)) {
            return -1;
        }
        double demand = flow.getBandwidth();
        for (Link link : links) {
            link.setUtilization(Math.max(0, link.getUtilization() - demand));
        }
        return 0;
    }

    /** testing
    public static void main(String[] args) {

        //set up topology
        Host a = new Host("a");
        Host b = new Host("b");
        Host c = new Host("c");
        ToRSwitch tor1 = new ToRSwitch("tor1");
        ToRSwitch tor2 = new ToRSwitch("tor2");
        tor1.addHost(a);
        tor1.addHost(b);
        tor2.addHost(c);
        SpineSwitch spine = new SpineSwitch("spine");
        spine.addtorSwitch(tor1);
        spine.addtorSwitch(tor2);
        Link l1 = new Link(a, tor1, 1, 0.5);
        Link l2 = new Link(tor1, b, 1, 0.5);
        Link l3 = new Link(tor1, spine, 10, 0);
        Link l4 = new Link(spine, tor2, 10, 0);
        Link l5 = new Link(tor2, c, 1, 0.5);



        ArrayList<Link> links1 = new ArrayList<Link>();
        links1.add(l1);
        links1.add(l2);

        ArrayList<Link> links2 = new ArrayList<Link>();
        links2.add(l1);
        links2.add(l3);
        links2.add(l4);
        links2.add(l5);


        Path p1 = new Path(a, b, links1);
        Path p2 = new Path(a, c, links2);
        ArrayList<Path> paths = new ArrayList<Path>();
        paths.add(p1);
        paths.add(p2);

        //test toString() 
        System.out.println(paths);

        //test getQuote()
        Flow f1 = new Flow(a, b, 0, 10, 0.1);
        System.out.println(p1.getQuote(f1));  //should print 12
        Flow f2 = new Flow(a, b, 0, 10, 0.4);
        System.out.println(p1.getQuote(f2));  //should print 200
        Flow f3 = new Flow(a, b, 0, 10, 0.7);
        System.out.println(p1.getQuote(f3));  //should print -1
        Flow f4 = new Flow(a, c, 0, 10, 0.1);
        System.out.println(p2.getQuote(f4));  //should print 14


        //test placeFlow() and removeFlow()
        System.out.println(l1.getUtilization());  //"0.5"
        p1.placeFlow(f1);
        System.out.println(l1.getUtilization());  //"0.6"
        p1.removeFlow(f1);
        System.out.println(l1.getUtilization());  //"0.5"

        System.out.println(l1.getUtilization());  //"0.5"
        p1.placeFlow(f3);
        System.out.println(l1.getUtilization());  //"1.2"
        p1.removeFlow(f3);
        System.out.println(l1.getUtilization());  //"0.5"

        System.out.println(l3.getUtilization());  //"0.0"
        p2.placeFlow(f4);
        System.out.println(l3.getUtilization());  //"0.1"
        p2.removeFlow(f4);
        System.out.println(l3.getUtilization());  //"0.0"



        System.out.println(p1.getQuote(f4)); //"-1", since p1 doesn't match f4
        System.out.println(p1.placeFlow(f4)); //"-1"
        System.out.println(p1.removeFlow(f4)); //"-1"



    }
    */

}
