/**
 * @author shangshangchen
 */
package com.vmturbo.NS;

import java.util.ArrayList;

public class EconomicPlacement {

    /**
     * All placement algorithms only give recommendations without actual act of placement
     * @param flow: the flow that needs placement
     * @param allPaths: a list of all paths in the network
     * @return: a recommended path based on price
     */
    public static Path econPlacement(Flow flow, ArrayList<Path> allPaths) {

        //find only paths that match the source-destination of the flow
        ArrayList<Path> paths = new ArrayList<>();
        for (Path path : allPaths) {
            if (path.getSource().equals(flow.getSource()) &&
                path.getDest().equals(flow.getDest())) {
                paths.add(path);
            }
        }

        //if no matching path exists
        if (paths.isEmpty()) {
            System.out.println("no matching path exists for " + flow.toString());
            return null;
        }

        //find cheapest path
        double minQuote = 200001.0;//to approximate infinity, because Double.MAX_VALUE is problematic
        int pathNo = 0;
        for (int i = 0; i < paths.size(); i++) {
            double quote = paths.get(i).getQuote(flow);
            //System.out.println(paths.get(i).toString() + quote + "\n");
            if (quote >= 0 && quote < minQuote) {//quote can be -1 if path can't accommodate flow
                minQuote = quote;
                pathNo = i;

            }
        }


        //check if flow has enough budget for the cheapest path
        if (flow.getBudget() >= minQuote) {
            return paths.get(pathNo);

        }
        else {
            System.out.println("insufficient budget: \n" +
                               flow.toString() + "\n" +
                               "minimum quote is " + minQuote + " $");
            return null;
        }

    }

}
