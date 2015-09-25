/**
 * @author shangshangchen
 */
package com.vmturbo.NS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.vmturbo.NS.Link.LinkType;


public class Utility {


    public static double formatDouble(double d, int decimalPlaces) {
        double n = Math.pow(10, decimalPlaces);
        return Math.round(d * n) / n;
    }

    /**
     * 
     * @param n1: source node
     * @param n2: destination node
     * @return an ArrayList of all parallel links from n1 to n2, empty ArrayList if no such link exists
     */
    public static ArrayList<Link> getMultiLinks(Node n1, Node n2, Collection<Link> links) {
        ArrayList<Link> multiLinks = new ArrayList<>();
        if (links == null)
            return multiLinks;

        for (Link link : links) {
            if (link != null &&
                link.getSrcNode().equals(n1) &&
                link.getDestNode().equals(n2)) {
                multiLinks.add(link);
            }
        }
        return multiLinks;
    }

    /**
     * skip links with 0 utilization, can print links alphabetically
     * @param links
     * @param threshold: because comparing double with 0 is problematic, we use a threshold
     */
    public static void printLinkUsage(ArrayList<Link> links, double threshold) {
        if (links == null)
            return;
        ArrayList<Link> localCopy = new ArrayList<>(links);
        Collections.sort(localCopy);
        for (Link link : localCopy) {
            if (link.getUtilization() > threshold) {
                System.out.println(link.toString() + ": " + link.getUtilization());
            }
        }

    }

    /**
     * it takes care of everything, including:
     * - create bidirectional links, and add them to the linkList
     * - configure neighbors for end nodes
     * @param n1
     * @param n2
     * @param capacities: {"10|10", 
     * @param links
     * @return
     */
    public static int connectNodes(Node n1, Node n2, String[] capacities, ArrayList<Link> links) {
        boolean connect = false;
        if ((n1 instanceof Host) && (n2 instanceof ToRSwitch)) {
            Host host = (Host)n1;
            ToRSwitch tor = (ToRSwitch)n2;
            host.addtorSwitch(tor);
            tor.addHost(host);
            connect = true;
        }
        if ((n2 instanceof Host) && (n1 instanceof ToRSwitch)) {
            Host host = (Host)n2;
            ToRSwitch tor = (ToRSwitch)n1;
            host.addtorSwitch(tor);
            tor.addHost(host);
            connect = true;
        }
        if ((n1 instanceof ToRSwitch) && (n2 instanceof SpineSwitch)) {
            ToRSwitch tor = (ToRSwitch)n1;
            SpineSwitch spine = (SpineSwitch)n2;
            tor.addSpine(spine);
            spine.addtorSwitch(tor);
            connect = true;

        }
        if ((n2 instanceof ToRSwitch) && (n1 instanceof SpineSwitch)) {
            ToRSwitch tor = (ToRSwitch)n2;
            SpineSwitch spine = (SpineSwitch)n1;
            tor.addSpine(spine);
            spine.addtorSwitch(tor);
            connect = true;

        }
        if (connect) {
            for (int i = 0; i < capacities.length; i++) {
                String s1 = "0/" + capacities[i].split("\\|")[0];
                String s2 = "0/" + capacities[i].split("\\|")[1];
                addDuplexLink(n1, n2, s1, s2, links);
            }
            return 0;
        }
        else {
            System.out.println("Utility.connectNodes: can't connect " + n1 + " and " + n2);
            return -1;
        }

    }

    /**
     * take care of all link-related matters, including
     * - add the new links to the list of all links
     * - take care "outGoingLinks" field of nodes 
     * - give names to links
     * @param n1 
     * @param n2
     * @param s1 "utilization/capacity" for the direction n1->n2, e.g. "3.0/10.0"
     * @param s2 "utilization/capacity" for the direction n2->n1
     * @param 
     * @return array of size 2: duplex[0] is the forward link, duplex[1] is the reverse link
     * can return null if input params are bad
     */
    public static Link[] addDuplexLink(Node n1, Node n2, String s1, String s2,
                                       ArrayList<Link> links) {

        Link[] duplex = new Link[2];
        Link link, rlink;
        int num = Utility.getMultiLinks(n1, n2, links).size();

        //create links and set link type
        if (((n1 instanceof Host) && (n2 instanceof ToRSwitch)) ||
            ((n2 instanceof Host) && (n1 instanceof ToRSwitch))) {
            link = new Link(n1, n2, 0, 0, LinkType.HOSTTOTOR);
            rlink = new Link(n2, n1, 0, 0, LinkType.HOSTTOTOR);
        }
        else if (((n1 instanceof ToRSwitch) && (n2 instanceof SpineSwitch)) ||
                 ((n2 instanceof ToRSwitch) && (n1 instanceof SpineSwitch))) {
            link = new Link(n1, n2, 0, 0, LinkType.TORTOSPINE);
            rlink = new Link(n2, n1, 0, 0, LinkType.TORTOSPINE);
        }
        else {
            System.out.println("Utility.addDuplexLink: can't add link between " + n1 + " and " + n2);
            return null;
        }

        //set utilization and capacity        
        double utilization, capacity;
        if ((s1.split("/").length != 2) ||
            (s2.split("/").length != 2)) {
            System.out.println("Utility.addDuplexLink: bad format in params");
            return null;
        }

        utilization = Double.parseDouble(s1.split("/")[0]);
        capacity = Double.parseDouble(s1.split("/")[1]);
        link.setUtilization(utilization);
        link.setCapacity(capacity);
        duplex[0] = link;
        if (links != null)
            links.add(link);
        n1.addOutgoingLink(link);

        utilization = Double.parseDouble(s2.split("/")[0]);
        capacity = Double.parseDouble(s2.split("/")[1]);
        rlink.setUtilization(utilization);
        rlink.setCapacity(capacity);
        duplex[1] = rlink;
        if (links != null)
            links.add(rlink);
        n2.addOutgoingLink(rlink);


        //set optional names
        if (num == 0) {
            link.setName(n1.getName() + "-" + n2.getName());
            rlink.setName(n2.getName() + "-" + n1.getName());
        }
        else {
            link.setName(n1.getName() + "-" + n2.getName() + "(" + num + ")");
            rlink.setName(n2.getName() + "-" + n1.getName() + "(" + num + ")");
        }

        return duplex;

    }

    //for Pamela
    public static void printPrePaths(ArrayList<ArrayList<Link>> prePathsList) {
        for (ArrayList<Link> prePath : prePathsList) {
            System.out.println("prePath: ");
            for (Link link : prePath) {
                System.out.println("    " + link);
            }
        }
    }

    public static void main(String[] args) {
        //testing formatDouble
        System.out.println(formatDouble(3.4995, 3));

        //testing printPrePaths()
        Node n1 = new Host("n1");
        Node n2 = new Host("n2");
        Link l1 = new Link(n1, n2, 0, 0, null);
        Link l2 = new Link(n2, n1, 0, 0, null);
        ArrayList<Link> pp1 = new ArrayList<>();
        pp1.add(l1);
        pp1.add(l2);
        ArrayList<Link> pp2 = new ArrayList<>();
        pp2.add(l2);
        pp2.add(l1);
        ArrayList<ArrayList<Link>> prePathsList = new ArrayList<>();
        prePathsList.add(pp1);
        prePathsList.add(pp2);
        printPrePaths(prePathsList);


        Node tor1 = new ToRSwitch("tor1");
        connectNodes(null, tor1, new String[] {"0|0"}, pp1);
        addDuplexLink(n1, tor1, "0/1", "0/1", null);
        printLinkUsage(null, 0.001);
        pp1.add(null);
        getMultiLinks(n1, n2, pp1);

    }
}
