/**
 * @author shangshangchen
 */
package com.vmturbo.NS;



public class LinkEvent extends Event {

    public enum LinkEventType {
        UP, DOWN
    }

    private int eventTime;
    private LinkEventType leType;
    private Link link;

    public LinkEvent(int eventTime, LinkEventType leType, Link link) {
        this.eventTime = eventTime;
        this.leType = leType;
        this.link = link;
    }

    @Override
    public double getEventTime() {
        return eventTime;
    }

    public LinkEventType getEventType() {
        return leType;
    }

    public Link getLink() {
        return link;
    }

    public void setEventTime(int eventTime) {
        this.eventTime = eventTime;
    }

    public void setLinkEventType(LinkEventType leType) {
        this.leType = leType;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    @Override
    public int compareTo(Event other) {
        if (other instanceof LinkEvent) {

            if (this.eventTime > other.getEventTime()) {
                return 1;
            }
            else if (this.eventTime < other.getEventTime()) {
                return -1;
            }
            else {

            }


        }
        return 0;
    }


}
