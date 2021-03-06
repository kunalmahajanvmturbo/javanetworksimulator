/**
 * @author shangshangchen 
 * We assume static topology for now, i.e. no method for updating paths when a link is down.
 */

package com.vmturbo.NS;

import java.util.ArrayList;
import java.util.HashMap;



public class ComputePaths {

    //matrix is a HashMap of HashMaps
    //matrix stores a list of paths between all pairs of hosts
    private HashMap<Host, HashMap<Host, ArrayList<Path>>> matrix;
    private ArrayList<SpineSwitch> spines;
    private ArrayList<ToRSwitch> tors;
    private ArrayList<Host> hosts;
    private ArrayList<Link> links;

    /**
     * Constructor: just takes in params. For actual computation, use findPaths()
     * All nodes are passed in as "ArrayLists", maybe we will switch to "Set" later
     * @param spines
     * @param tors
     * @param hosts
     * @param links
     */
    public ComputePaths(ArrayList<SpineSwitch> spines, ArrayList<ToRSwitch> tors,
                    ArrayList<Host> hosts, ArrayList<Link> links) {
        matrix = new HashMap<Host, HashMap<Host, ArrayList<Path>>>();
        this.spines = spines;
        this.tors = tors;
        this.hosts = hosts;
        this.links = links;
    }

    /**
     * Main method for computing paths for all pairs
     * This method only works for leaf-spine architecture.
     * For general graphs, we need something like BFS.
     * For now, it only considers paths that contain 0 or 1 spine.
     * Later, we will consider paths that bounce off multiple spines.
     * @param spines
     * @param tors
     * @param hosts
     * @param links
     */
    public void findPaths() {

        //create an empty |hosts| × |hosts| matrix
        for (Host source : hosts) {
            HashMap<Host, ArrayList<Path>> destinations = new HashMap<>();
            for (Host dest : hosts) {
                destinations.put(dest, new ArrayList<Path>());
            }
            matrix.put(source, destinations);
        }

        //populate the matrix
        for (Host source : hosts) {
            for (Host dest : hosts) {
                findPaths(source, dest);
            }
        }

    }

    /**
     * Helper method for findPaths()
     * Again, it only works for spine-leaf architecture
     * @param h1: source host
     * @param h2: destination host
     */
    private void findPaths(Host h1, Host h2) {
        if (h1.equals(h2))
            return;

        ArrayList<ToRSwitch> tors1 = h1.getToRSwitch();
        ArrayList<ToRSwitch> tors2 = h2.getToRSwitch();
        if (tors1 == null || tors2 == null)
            return;

        for (ToRSwitch tor1 : tors1) {
            for (ToRSwitch tor2 : tors2) {
                if (tor1.equals(tor2)) {
                    //add path: h1->tor1->h2 to the cell (h1,h2)  
                    ArrayList<Link> l1s = Utility.getMultiLinks(h1, tor1, this.links);
                    ArrayList<Link> l2s = Utility.getMultiLinks(tor1, h2, this.links);
                    for (Link l1 : l1s) {
                        for (Link l2 : l2s) {
                            ArrayList<Link> pathLinks = new ArrayList<>();
                            pathLinks.add(l1);
                            pathLinks.add(l2);
                            Path newPath = new Path(h1, h2, pathLinks);
                            getPaths(h1, h2).add(newPath);
                        }
                    }


                }
                else { // find common spines connected to both tor1 and tor2
                    ArrayList<SpineSwitch> spines1 = tor1.getSpineList();
                    ArrayList<SpineSwitch> spines2 = tor2.getSpineList();
                    if (spines1 == null || spines2 == null)
                        continue;
                    for (SpineSwitch spine : spines1) {
                        if (spines2.contains(spine)) {
                            //add path: h1->tor1->spine->tor2->h2
                            ArrayList<Link> l1s = Utility.getMultiLinks(h1, tor1, this.links);
                            ArrayList<Link> l2s = Utility.getMultiLinks(tor1, spine, this.links);
                            ArrayList<Link> l3s = Utility.getMultiLinks(spine, tor2, this.links);
                            ArrayList<Link> l4s = Utility.getMultiLinks(tor2, h2, this.links);
                            for (Link l1 : l1s) {
                                for (Link l2 : l2s) {
                                    for (Link l3 : l3s) {
                                        for (Link l4 : l4s) {
                                            ArrayList<Link> pathLinks = new ArrayList<>();
                                            pathLinks.add(l1);
                                            pathLinks.add(l2);
                                            pathLinks.add(l3);
                                            pathLinks.add(l4);
                                            Path newPath = new Path(h1, h2, pathLinks);
                                            getPaths(h1, h2).add(newPath);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @param source
     * @param dest
     * @return: pointer to the list of paths between source and dest
     */
    public ArrayList<Path> getPaths(Host source, Host dest) {
        return matrix.get(source).get(dest);
    }


    ///**for testing 
    public static void main(String[] args) {

        //set up topology

        Host a = new Host("a");
        Host b = new Host("b");
        Host c = new Host("c");

        ToRSwitch tor1 = new ToRSwitch("tor1");
        ToRSwitch tor2 = new ToRSwitch("tor2");
        tor1.addHost(a);
        tor1.addHost(b);
        tor2.addHost(b);
        tor2.addHost(c);
        a.addtorSwitch(tor1);
        b.addtorSwitch(tor1);
        b.addtorSwitch(tor2);
        c.addtorSwitch(tor2);

        SpineSwitch spine1 = new SpineSwitch("spine1");
        SpineSwitch spine2 = new SpineSwitch("spine2");
        spine1.addtorSwitch(tor1);
        spine1.addtorSwitch(tor2);
        spine2.addtorSwitch(tor1);
        spine2.addtorSwitch(tor2);
        tor1.addSpine(spine1);
        tor1.addSpine(spine2);
        tor2.addSpine(spine1);
        tor2.addSpine(spine2);


        Link l1 = new Link(a, tor1, 1, 0, null);
        Link l1r = new Link(tor1, a, 1, 0, null);

        Link l2 = new Link(b, tor1, 1, 0, null);
        Link l2r = new Link(tor1, b, 1, 0, null);

        Link l3 = new Link(tor1, spine1, 10, 1.2, null);
        Link l3r = new Link(spine1, tor1, 10, 0, null);

        //Link l3a = new Link(tor1, spine1, 10, 0, null);
        //Link l3ar = new Link(spine1, tor1, 10, 0, null);

        Link l4 = new Link(tor2, spine1, 10, 0, null);
        Link l4r = new Link(spine1, tor2, 10, 0, null);

        //Link l4a = new Link(tor2, spine1, 10, 0, null);
        //Link l4ar = new Link(spine1, tor2, 10, 0, null);

        //Link l4b = new Link(tor2, spine1, 10, 0, null);
        //Link l4br = new Link(spine1, tor2, 10, 0, null);

        Link l5 = new Link(c, tor2, 1, 0, null);
        Link l5r = new Link(tor2, c, 1, 0, null);

        Link l6 = new Link(b, tor2, 1, 0, null);
        Link l6r = new Link(tor2, b, 1, 0, null);

        Link l7 = new Link(tor1, spine2, 10, 1.25, null);
        Link l7r = new Link(spine2, tor1, 10, 0, null);

        Link l8 = new Link(tor2, spine2, 10, 0, null);
        Link l8r = new Link(spine2, tor2, 10, 0, null);


        ArrayList<Host> hosts = new ArrayList<>();
        ArrayList<Link> links = new ArrayList<>();
        ArrayList<ToRSwitch> tors = new ArrayList<>();
        ArrayList<SpineSwitch> spines = new ArrayList<>();
        hosts.add(a);
        hosts.add(b);
        hosts.add(c);
        tors.add(tor1);
        tors.add(tor2);
        spines.add(spine1);
        spines.add(spine2);
        links.add(l1);
        links.add(l1r);
        links.add(l2);
        links.add(l2r);
        links.add(l3);
        links.add(l3r);
        links.add(l4);
        links.add(l4r);
        links.add(l5);
        links.add(l5r);
        links.add(l6);
        links.add(l6r);
        links.add(l7);
        links.add(l7r);
        links.add(l8);
        links.add(l8r);

        //links.add(l3a);
        //links.add(l3ar);
        //links.add(l4a);
        //links.add(l4ar);
        //links.add(l4b);
        //links.add(l4br);


        //=========testing findPaths()========================
        ComputePaths pathsComputer = new ComputePaths(spines, tors, hosts, links);
        pathsComputer.findPaths();
        System.out.println(pathsComputer.getPaths(a, a));//[]
        System.out.println(pathsComputer.getPaths(a, b));//3 paths
        System.out.println(pathsComputer.getPaths(a, c));//2 paths

        //=========testing different placement algorithms==========
        //To see how economic placement adjusts to link utilization,
        //change link utilization above. 

        ArrayList<Path> paths;
        Path path;
        Flow flow;

        ECMPPlacement ecmp = new ECMPPlacement(spines, tors, hosts, links);

        paths = pathsComputer.getPaths(a, b);
        flow = new Flow(a, b, 0, 10, 0.5);
        System.out.println("\nflow a -> b");
        path = RandomPlacement.randomPlacement(flow, paths);
        System.out.println("randomPlacement: " + path);
        path = ecmp.recommendPath(flow);
        System.out.println("ecmpPlacement:   " + path);
        path = EconomicPlacement.econPlacement(flow, paths);
        System.out.println("econPlacment:    " + path);

        paths = pathsComputer.getPaths(a, c);
        flow = new Flow(a, c, 0, 10, 0.5);
        System.out.println("\nflow a -> c");
        path = RandomPlacement.randomPlacement(flow, paths);
        System.out.println("randomPlacement: " + path);
        path = ecmp.recommendPath(flow);
        System.out.println("ecmpPlacement:   " + path);
        path = EconomicPlacement.econPlacement(flow, paths);
        System.out.println("econPlacment:    " + path);

        paths = pathsComputer.getPaths(b, a);
        flow = new Flow(b, a, 0, 10, 0.5);
        System.out.println("\nflow b -> a");
        path = RandomPlacement.randomPlacement(flow, paths);
        System.out.println("randomPlacement: " + path);
        path = ecmp.recommendPath(flow);
        System.out.println("ecmpPlacement:   " + path);
        path = EconomicPlacement.econPlacement(flow, paths);
        System.out.println("econPlacment:    " + path);


    }
    //*/
}
