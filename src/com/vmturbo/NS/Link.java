/**
 *
 */
package com.vmturbo.NS;

/**
 * @author kunal
 *
 */
public class Link implements Comparable<Link> {

    private Node srcNode;
    private Node destNode;
    private double capacity;
    private double utilization;
    private LinkType type;
    private String name = "";

    public enum LinkType {
        HOSTTOTOR, TORTOSPINE
    }

    public Link(Node srcNode, Node destNode, double capacity, double utilization, LinkType type) {
        this.srcNode = srcNode;
        this.destNode = destNode;
        this.setCapacity(capacity);
        this.setUtilization(utilization);
        this.type = type;
    }

    //another constructor
    public Link(Node srcNode, Node destNode, double capacity, LinkType type) {
        this.srcNode = srcNode;
        this.destNode = destNode;
        this.setCapacity(capacity);
        this.utilization = 0;
        this.type = type;
    }

    public Node getSrcNode() {
        return srcNode;
    }

    public Node getDestNode() {
        return destNode;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getUtilization() {
        return utilization;
    }

    public double getPercentage() {
        if (capacity == 0)
            return 0;
        return utilization / capacity;
    }

    public LinkType getLinkType() {
        return type;
    }

    public void setSrcNode(Node src) {
        if (src == null) {
            return;
        }
        this.srcNode = src;

    }

    public void setDestNode(Node dest) {
        if (dest == null) {
            return;
        }
        this.destNode = dest;
    }

    public void setCapacity(double capac) {
        if (capac < 0) {
            return;
        }
        this.capacity = Utility.formatDouble(capac, 4);
    }

    public double getCapacityLeft() {
        if (utilization >= capacity) {
            return 0;
        }
        else {
            return Utility.formatDouble(capacity - utilization, 4);
        }
    }

    public void setUtilization(double util) {
        if (util < 0) {
            return;
        }
        this.utilization = Utility.formatDouble(util, 4);
    }


    //optional name of the link,
    //so that we can distinguish parallel links between the same two nodes
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        if (this.name.trim().equals("")) {
            return srcNode + " -> " + destNode;
        }
        else {
            return this.name;
        }

    }

    @Override
    public int compareTo(Link other) {
        return this.toString().compareTo(other.toString());
    }

    public boolean exists(String src, String dest) {
        if (src != null && this.srcNode.name.equals(src) && this.destNode.name.equals(dest)) {
            return true;
        }
        return false;
    }


}
