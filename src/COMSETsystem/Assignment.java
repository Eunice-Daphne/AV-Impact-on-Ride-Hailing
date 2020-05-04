package COMSETsystem;

import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Assignment {

    public static void makeAssignment(Simulator simulator) throws SQLException, ClassNotFoundException {

        // finds the agent with least travel time between itself and this resource

        //System.out.println("Make Assignment");

        final int MOVING_AGENT_TO_INTERSECTION = 1;
        final int RESOURCE_EXPIRED = 3;

        //System.out.println("Start of the Assignment");

        //Printing queue, empty agent and waiting resources at the start of the assignment
        //simulator.peekEventsQueue(simulator.events);
        //simulator.peekEmptyAgents(simulator.emptyAgents);
        //simulator.peekWaitingResouces(simulator.waitingResources);

        if (! simulator.emptyAgents.isEmpty() && ! simulator.waitingResources.isEmpty()) {
            //System.out.println("Computing the edges based on travel time");
            for(ResourceEvent resource: simulator.waitingResources){
                //GraphVertex  vertex0 = new GraphVertex("R"+resource.id);
                simulator.graph.addVertex("R"+resource.id);
                for(AgentEvent agent: simulator.emptyAgents){
                    //GraphVertex  vertex1 = new GraphVertex("A"+agent.id);
                    simulator.graph.addVertex("A"+agent.id);
                    long travelTime = simulator.map.travelTimeBetween(agent.loc, resource.pickupLoc);
                    simulator.graph.addEdge("R"+resource.id, "A"+agent.id);
                    simulator.graph.setEdgeWeight(simulator.graph.getEdge("R"+resource.id, "A"+agent.id), travelTime);

                }
            }

            FairAssignmentJGraphT match = new FairAssignmentJGraphT();
            HashMap<String,String> fairmatch = match.getFairMatch(simulator.graph);

            match.addFairMatchToDB(fairmatch);

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
                    //System.out.println("Iterating the Table");
                    int Resource = rs.getInt("ResourceId");
                    int Agent = rs.getInt("AgentId");
                    int TravelTime = rs.getInt("Weight");
                    resource = findResourceEvent(simulator, Resource);
                    ag = findAgentEvent(simulator, Agent);
                    arriveTime = TravelTime + resource.availableTime;
                    bestAgent = ag;
                    if (arriveTime > resource.availableTime + simulator.ResourceMaximumLifeTime) {
                        resource.time += simulator.ResourceMaximumLifeTime;
                        resource.eventCause = RESOURCE_EXPIRED;
                        Logger.getLogger(resource.getClass().getName()).log(Level.INFO, "Setup expiration event at time " + resource.time, resource);
                        simulator.events.add(resource);
                        simulator.waitingResources.remove(resource);
                    }
                    else{

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
                String deletRow = "DELETE FROM av_schema.assignmentTable";
                st.executeUpdate(deletRow);
                //System.out.println("**** DB deleted Successfully ***** ");
                conn.close();

            }
            catch (Exception e)
            {
                System.err.println("Got an exception! ");
                e.printStackTrace();
            }
        }

        for(AgentEvent agt : simulator.emptyAgents){
            System.out.println("\n");
            if(simulator.polledAgentEvents.contains(agt.id)){

                LocationOnRoad locAgentCopy = simulator.agentCopy(agt.loc);
                agt.agent.planSearchRoute(locAgentCopy, agt.time);

                // no resources have been assigned to the agent
                // so if the agent was not empty, make it empty for other resources

                if (!simulator.emptyAgents.contains(agt)) {
                    // "Label" the agent as empty.
                    simulator.emptyAgents.add(agt);
                }

                long nextEventTime = agt.time +  agt.loc.road.travelTime - agt.loc.travelTimeFromStartIntersection +100;
                LocationOnRoad nextLoc = new LocationOnRoad(agt.loc.road, agt.loc.road.travelTime);
                agt.setEvent(nextEventTime, nextLoc, AgentEvent.MOVING_AGENT_TO_INTERSECTION);
                simulator.events.add(agt);

                //modify existing agent in the empty agent list
                agt.time = nextEventTime;
                agt.loc = nextLoc;
                agt.eventCause = MOVING_AGENT_TO_INTERSECTION;

            }
        }


        //conn.close();
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
