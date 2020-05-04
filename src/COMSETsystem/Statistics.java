package COMSETsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class Statistics {

    public static void agent_statistics(Simulator simulator, AgentEvent ag, ResourceEvent resource, long arriveTime) {

        long cruiseTime = ag.time - ag.startSearchTime;
        simulator.totalAgentCruiseTime += cruiseTime;

        long approachTime = arriveTime - ag.time;
        long searchTime = cruiseTime + approachTime;
        simulator.totalAgentSearchTime += searchTime;

        long waitTime = arriveTime - resource.availableTime;
        simulator.totalResourceWaitTime += waitTime;

        simulator.totalAssignments++;

        System.out.println("\n");
    }


    public static void store_statistics (long assignmentType,long totalAgents,long totalResources,long numberOfHubs,long  windowSize,long totalAssignments,long averageResourceWaitTime,long averageAgentSearchTime,long expiredResourcesPercent, long totalSimulationTime){
        //Connection connection = null;
        Connection conn = null;
        try {
            String databaseURL = "jdbc:mysql://localhost:3306/av_schema";
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(databaseURL, "root", "tiger123");
            if (conn == null) {
                System.out.println("Not Connected to the database");
            }
             if (assignmentType == 1)
                windowSize = windowSize*30;
            String eachRow =  "INSERT INTO fullstatistics (assignmentType,totalAgents,totalResources,numberOfHubs, windowSize,totalAssignments,averageResourceWaitTime,averageAgentSearchTime,expiredResourcesPercent, totalSimulationTime ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(eachRow);
            statement.setLong(1,assignmentType);
            statement.setLong(2, totalAgents);
            statement.setLong(3, totalResources);
            statement.setLong(4, numberOfHubs);
            statement.setLong(5, windowSize);
            statement.setLong(6, totalAssignments);
            statement.setLong(7, averageResourceWaitTime);
            statement.setLong(8, averageAgentSearchTime);
            statement.setLong(9, expiredResourcesPercent);
            statement.setLong(10, totalSimulationTime);

            statement.addBatch();
            statement.executeBatch();

            // conn.commit();
            conn.close();

        }
        catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }

    }

}
