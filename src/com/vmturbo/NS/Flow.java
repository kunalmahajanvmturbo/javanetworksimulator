/**
 * @author shangshangchen
 */
package com.vmturbo.NS;

public class Flow {

    Host source, dest;
    int start, duration; //in seconds
    int size; //total bytes of the flow
    int budget; // the budge the flow has, in virtual dollars

    public Flow(Host source, Host dest, int start, int duration, int size, int budget) {
        this.source = source;
        this.dest = dest;
        this.start = start;
        this.duration = duration;
        this.size = size;
        this.budget = budget;
    }

    public Host getSource() {
        return source;
    }

    public Host getDest() {
        return dest;
    }

    public int getStart() {
        return start;
    }

    public int getDuration() {
        return duration;
    }

    public int getEnd() {
        return start + duration;
    }

    public int getSize() {
        return size;
    }

    public int getBudget() {
        return budget;
    }

    public String getAllInfo() {
        return ("flow: "
                + source.getName()
                + " -> "
                + dest.getName()
                + "; "
                + getStart()
                + "s -> "
                + getEnd()
                + "s; "
                + size
                + " bytes; "
                + budget
                + " $\n");
    }

    /** for testing purpose
    public static void main(String[] args) {
        Host h1 = new Host("h1", null);
        Host h2 = new Host("h2", null);
        Flow f = new Flow(h1, h2, 0, 20, 1000, 100);
        
    }
    */
}
