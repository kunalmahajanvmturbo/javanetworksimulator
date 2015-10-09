/**
 * @author shangshangchen
 */
package com.vmturbo.NS;

public class Flow {

    private Host source, dest;
    private double bandwidth; //bandwidth of the flow
    private double budget; // the budget the flow has, in virtual dollars

    private Path assignedPath; //an optional field for flow to remember its assignedPath

    private int start, duration; //these will be reset when flow stops/restarts
    private int begin, end; //these won't change once set
    private boolean stopped = false;

    //constructor: assuming infinite budget for now
    public Flow(Host source, Host dest, int start, int duration, double bandwidth /*, int budget*/) {
        this.source = source;
        this.dest = dest;
        this.start = start;
        this.duration = duration;
        this.bandwidth = bandwidth;
        this.budget = 200000.0; //Double.MAX_VALUE is problematic, so we use 200k to approximate infinity

        begin = start;
    }

    public Flow(Flow f) {
        this.source = f.getSource();
        this.dest = f.getDest();
        this.start = f.getStart();
        this.duration = f.getDuration();
        this.bandwidth = f.getBandwidth();
        this.budget = f.getBandwidth();

        begin = start;

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

    public void setBandwith(double bw) {
        this.bandwidth = bw;
    }

    //stop at time t
    //duration will be recalculated
    public void stop(int t) {
        //if t < stop, do nothing
        //if flow is already stopped, don't stop it again 
        //if duration == 0, then the flow is finished already.
        if (t < start || stopped || duration == 0)
            return;
        if (start + duration <= t) {
            end = start + duration;
            duration = 0;
        }
        else {
            duration = duration - (t - start);
        }
        stopped = true;
    }

    //reset start time 
    public void reset(int t) {
        if (duration == 0)
            return;
        start = t;
        stopped = false;
    }



    public int getDelay() {
        if (end == 0) {
            System.out.println("the flow hasn't terminated");
            return -1;
        }
        return end - begin;
    }

    public double getBandwidth() {
        return bandwidth;
    }

    public double getBudget() {
        return budget;
    }


    @Override
    public String toString() {
        return ("flow: "
                + source.getName() + " -> " + dest.getName() + "; "
                + "starts: " + getStart() + "s; "
                + "duration: " + getDuration() + "s; "
                + bandwidth + " Gb/s; "
                + budget + " $;");
    }

    //This method allows flow to remember its path
    //It does NOT place the flow to path, which is done by placeFlow() in Path.java.
    public void setPath(Path path) {
        this.assignedPath = path;
    }

    public Path getPath() {
        return this.assignedPath;
    }



    // for testing purpose
    public static void main(String[] args) {
        Host h1 = new Host("h1");
        Host h2 = new Host("h2");
        Flow f = new Flow(h1, h2, 0, 4, 1);
        f.reset(3);
        System.out.println(f.getStart() + "," + f.getDuration());
        f.stop(2);
        System.out.println(f.getStart() + "," + f.getDuration());
        f.stop(3);
        System.out.println(f.getStart() + "," + f.getDuration());
        f.reset(4);
        System.out.println(f.getStart() + "," + f.getDuration());
        f.stop(5);
        System.out.println(f.getStart() + "," + f.getDuration());
        f.reset(6);
        System.out.println(f.getStart() + "," + f.getDuration());
        f.stop(9);
        System.out.println(f.getStart() + "," + f.getDuration());
        f.reset(10);
        System.out.println(f.getStart() + "," + f.getDuration());
        f.stop(11);
        System.out.println(f.getStart() + "," + f.getDuration());
        System.out.println(f.getDelay());

    }

}
