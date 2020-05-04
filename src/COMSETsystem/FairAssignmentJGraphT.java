package COMSETsystem;

import java.util.ArrayList;
import java.util.Collections;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleWeightedGraph;


public class FairAssignmentJGraphT {
	
	SimpleWeightedGraph<String, DefaultEdge> graph;
	int numberOfResources;
	
	public FairAssignmentJGraphT() {
		
	}
	
	
	 public HashMap<String,String> getFairMatch(SimpleWeightedGraph<String, DefaultEdge> graph) {
		 
		// System.out.println("Fair Assignment:");
		 HashMap<String,String> fairmatch = new HashMap<String,String>();
		 
		 while(graph.edgeSet().size() != 0) {
			 
			 Set<DefaultEdge> graphEdges = graph.edgeSet();
			 ArrayList<DefaultEdge> graphEdgesList = new ArrayList<DefaultEdge>();
			 graphEdgesList.addAll(graphEdges);
			 
			 Collections.sort(graphEdgesList, new Comparator<DefaultEdge>(){
	      	    @Override
	              public int compare(DefaultEdge e1,DefaultEdge e2){
	                  return (int)(graph.getEdgeWeight(e1) - graph.getEdgeWeight(e2));
	              }
	          });
			 
			 String resourceNode = graph.getEdgeSource(graphEdgesList.get(0));
		     String agentNode = graph.getEdgeTarget(graphEdgesList.get(0));
		     String edgeweight = Integer.toString((int)(graph.getEdgeWeight(graphEdgesList.get(0))));
			 
	         //System.out.println("Minimum Edge weight with source vertex: "+ resourceNode + "and target vertex: "+ agentNode +" with weight " +  edgeweight);
	         
	         fairmatch.put(resourceNode, agentNode +':'+ edgeweight);
	         graph.removeVertex(resourceNode);
		     graph.removeVertex(agentNode); 
			 
		 }
	        
	     return fairmatch;
	     
	    }
	 

	 public void addFairMatchToDB(HashMap<String,String> fairmatch) {
		    
		//Connection connection = null;
	        Connection conn = null;

	        try {
				String databaseURL = "jdbc:mysql://localhost:3306/av_schema";
				Class.forName("com.mysql.cj.jdbc.Driver");
				 conn = DriverManager.getConnection(databaseURL, "root", "tiger123");
	            if (conn == null) {
	                System.out.println("Not Connected to the database");
	            }

	            String eachRow =  "INSERT INTO assignmentTable (ResourceId, AgentId, Weight) VALUES (?, ?, ?)";
	            PreparedStatement statement = conn.prepareStatement(eachRow);


	            for ( Entry<String, String> match : fairmatch.entrySet()) {
	                String key = match.getKey();
	                String value = match.getValue();

	                int ResourceId = Integer.parseInt(key.replace("R", ""));

	                String[] data = value.split(":");
	                int AgentId = Integer.parseInt(data[0].replace("A", ""));
	                int Weight = Integer.parseInt(data[1]);

	                statement.setInt(1, ResourceId);
	                statement.setInt(2, AgentId);
	                statement.setInt(3, Weight);

	                statement.addBatch();

	            }
	            statement.executeBatch();

	            //System.out.println("Updated Assignment in DB successfully!");

	            conn.close();

	        } catch (SQLException | ClassNotFoundException ex) {
	            ex.printStackTrace();

	            try {
	                conn.rollback();

	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	        
	        }
    }

