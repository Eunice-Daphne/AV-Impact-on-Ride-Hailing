package COMSETsystem;

import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultEdge;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OptimalAssignmentJGraphT {

    public static void makeAssignment(Simulator simulator) throws SQLException, ClassNotFoundException {

        // finds the agent with least travel time between itself and this resource

        // System.out.println("Make Assignment");

        final int MOVING_AGENT_TO_INTERSECTION = 1;
        final int RESOURCE_EXPIRED = 3;

        // System.out.println("Start of the Assignment");

        //Printing queue, empty agent and waiting resources at the start of the assignment
        //simulator.peekEventsQueue(simulator.events);
        //simulator.peekEmptyAgents(simulator.emptyAgents);
        //simulator.peekWaitingResouces(simulator.waitingResources);

        //Optimal Assignment
        Set<String> partition1 = new HashSet<>();
        Set<String> partition2 = new HashSet<>();

        if (! simulator.emptyAgents.isEmpty() && ! simulator.waitingResources.isEmpty()) {
            // System.out.println("Computing the edges based on travel time");
            for(ResourceEvent resource: simulator.waitingResources){
                //GraphVertex  vertex0 = new GraphVertex("R"+resource.id);
                simulator.graph.addVertex("R"+resource.id);
                partition1.add("R"+resource.id);
                int i=0;
                for(AgentEvent agent: simulator.emptyAgents){
                    //GraphVertex  vertex1 = new GraphVertex("A"+agent.id);
                    simulator.graph.addVertex("A"+agent.id);
                    partition2.add("A"+agent.id);
                    //long travelTime = simulator.map.travelTimeBetween(agent.loc, resource.pickupLoc);
                    double timeToPickUp = simulator.map.travelTimeBetween(agent.loc, resource.pickupLoc);

                    double benefit =  Math.round((resource.fairAmount/timeToPickUp)*100000);
                    //System.out.println("R"+resource.id+" -> "+ "A"+agent.id+" - "+benefit);

                    simulator.graph.addEdge("R"+resource.id, "A"+agent.id);
                    simulator.graph.setEdgeWeight(simulator.graph.getEdge("R"+resource.id, "A"+agent.id), benefit);

                }
            }
            MaximumWeightBipartiteMatching<String, DefaultEdge> maxweight = new MaximumWeightBipartiteMatching<>(simulator.graph, partition1, partition2);
            MatchingAlgorithm.Matching<String, DefaultEdge> matchingWeight = maxweight.getMatching();

            HashMap<String, String> mapMatch = new HashMap<>();
            Set<DefaultEdge> assignmentSet2 = matchingWeight.getEdges();

            for (DefaultEdge edg : assignmentSet2) {
                String s = simulator.graph.getEdgeSource(edg);
                int i = 0;//(int)simulator.graph.getEdgeWeight(edg);
                String t = simulator.graph.getEdgeTarget(edg)+":"+i++;
                mapMatch.put(s, t);
            }
            FairAssignmentJGraphT match = new FairAssignmentJGraphT();
            //System.out.println(mapMatch);
            match.addFairMatchToDB(mapMatch);

            //System.out.println("Hash Map:"+mapMatch);

            //picking data from DB

            try{
                // create our mysql database connection
                String databaseURL = "jdbc:mysql://localhost:3306/av_schema";
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DriverManager.getConnection(databaseURL, "root", "tiger123");
                if (conn == null) {
                    System.out.println("Not Connected to the database");
                }
                //System.out.println("waiting resource size " + simulator.waitingResources.size());
                String eachRow = "SELECT * FROM av_schema.assignmentTable";
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(eachRow);
                long arriveTime = 0;
                AgentEvent ag = null;
                ResourceEvent resource = null;
                AgentEvent bestAgent = null;
                while (rs.next() && rs!=null) {
                    // System.out.println("Iterating the Table");
                    int Resource = rs.getInt("ResourceId");
                    int Agent = rs.getInt("AgentId");
                    // int TravelTime = rs.getInt("Weight");
                    //System.out.println("Resource + Agent + TravelTime " + Resource + " " + Agent + " " + TravelTime);
                    resource = findResourceEvent(simulator, Resource);
                    ag = findAgentEvent(simulator, Agent);
                    long TravelTime = simulator.map.travelTimeBetween(ag.loc, resource.pickupLoc);
                    arriveTime = TravelTime + resource.availableTime;
                    bestAgent = ag;
                    //  System.out.println("bestAgent " + bestAgent);
                    if (arriveTime > resource.availableTime + simulator.ResourceMaximumLifeTime) {
                        // System.out.println("Inside Expired" );
                        //simulator.waitingResources.add(resource);
                        resource.time += simulator.ResourceMaximumLifeTime;
                        resource.eventCause = RESOURCE_EXPIRED;
                        Logger.getLogger(resource.getClass().getName()).log(Level.INFO, "Setup expiration event at time " + resource.time, resource);
                        simulator.events.add(resource);
                        simulator.waitingResources.remove(resource);
                    }
                    else{

                        //   System.out.println("Inside Assigned" );
                        //Statistics.agent_statistics(simulator,ag,resource,arriveTime);

                        bestAgent.assignedTo(ag.loc, resource.time, resource.id, resource.pickupLoc, resource.dropoffLoc);
                        long tripTime = simulator.map.travelTimeBetween(resource.pickupLoc, resource.dropoffLoc);

                        //removing the best agent from the empty agent list and assigned to drop off location
                        simulator.emptyAgents.remove(bestAgent);
                        simulator.events.remove(bestAgent);
                        simulator.events.remove(resource);
                        simulator.polledAgentEvents.remove(bestAgent.id);
                        Logger.getLogger(bestAgent.getClass().getName()).log(Level.INFO, "Assigned to agent id = " + bestAgent.id + " currently at " + bestAgent.loc, bestAgent);
                        bestAgent.setEvent(arriveTime + tripTime, resource.dropoffLoc, AgentEvent.AGENT_AVAILABLE_FOR_PICKUP);
                        simulator.events.add(bestAgent);

                        //removing the assigned resources from the waiting resource list
                        simulator.waitingResources.remove(resource);
                        simulator.polledResourceEvents.remove(resource.id);

                        //estimating the statistics
                        Statistics.agent_statistics(simulator, bestAgent, resource, arriveTime);
                    }
                }
                String deletRow = "DELETE FROM av_schema.assignmenttable";
                st.executeUpdate(deletRow);
                //  System.out.println("**** DB deleted Successfully ***** ");
                conn.close();

            }
            catch (Exception e)
            {
                System.err.println("Got an exception! ");
                e.printStackTrace();
            }
        }

        //   System.out.println("**** Sending waiting agents to the events ***** ");
        //   System.out.println("No of Waiting agents" + simulator.emptyAgents.size());
        for(AgentEvent agt : simulator.emptyAgents){
            //  System.out.println("\n");
            if(simulator.polledAgentEvents.contains(agt.id)){
                //  System.out.println("----- Empty Agent id " + agt.id + "----");
                //System.out.println("Agent Current Time" + agt.time);
                //System.out.println("Agent Current loc" + agt.loc);
                //System.out.println("Agent Current Event " + agt.eventCause);

                LocationOnRoad locAgentCopy = simulator.agentCopy(agt.loc);
                agt.agent.planSearchRoute(locAgentCopy, agt.time);

                // no resources have been assigned to the agent
                // so if the agent was not empty, make it empty for other resources

                if (!simulator.emptyAgents.contains(agt)) {
                    // "Label" the agent as empty.
                    //  System.out.println("Before Adding empty agents back to Events Queue " + simulator.emptyAgents.size());
                    simulator.emptyAgents.add(agt);
                    // System.out.println("After Adding empty agents back to Events Queue " + simulator.emptyAgents.size());
                }

                long nextEventTime = agt.time +  agt.loc.road.travelTime - agt.loc.travelTimeFromStartIntersection +100;
                // System.out.println("nextEventTime "+ nextEventTime);
                // System.out.println("agt.loc.road.travelTime " + agt.loc.road.travelTime);
                //  System.out.println("agt.loc.travelTimeFromStartIntersection " + agt.loc.travelTimeFromStartIntersection);
                // System.out.println("nextEventTime " + nextEventTime);
                LocationOnRoad nextLoc = new LocationOnRoad(agt.loc.road, agt.loc.road.travelTime);
                agt.setEvent(nextEventTime, nextLoc, AgentEvent.MOVING_AGENT_TO_INTERSECTION);
                simulator.events.add(agt);

                //modify existing agent in the empty agent list
                agt.time = nextEventTime;
                agt.loc = nextLoc;
                agt.eventCause = MOVING_AGENT_TO_INTERSECTION;

                //System.out.println("Agent Changed Time" + agt.time);
                //System.out.println("Agent Changed loc" + agt.loc);
                //System.out.println("Agent Changed Event " + agt.eventCause);

            }
        }
    }

    static AgentEvent findAgentEvent(Simulator simulator, int agent_id) {

        for (AgentEvent ag : simulator.emptyAgents) {
            if (ag.id == agent_id) {
                return ag;
            }
        }
        return null;
    }

    static ResourceEvent findResourceEvent(Simulator simulator, int resourceID) {

        for (ResourceEvent rs : simulator.waitingResources) {
            if (rs.id == resourceID) {
                return rs;
            }
        }
        return null;
    }
}
