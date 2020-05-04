package UserExamples;

import COMSETsystem.*;
import DataParsing.GeoProjector;
import DataParsing.MapWithData;
import MapCreation.MapCreator;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Random destination search algorithm:
 * After dropping off a resource, the agent chooses a random intersection on the map as the destination,
 * and follows the shortest travel time path to go to the destination. When the destination is reached,
 * the agent chooses another random intersection to go to. This procedure is repeated until the agent
 * is assigned to a resource.
 */
public class AgentRandomDestination extends BaseAgent {

	// search route stored as a list of intersections.
	LinkedList<Intersection> route = new LinkedList<Intersection>();

	// random number generator
	Random rnd;

	// a static singleton object of a data model, shared by all agents
	static DummyDataModel dataModel = null;

	/**
	 * AgentRandomWalk constructor.
	 *
	 * @param id An id that is unique among all agents and resources
	 * @param map The map
	 */
	public AgentRandomDestination(long id, CityMap map) {
		super(id, map);
		rnd = new Random(id);
		if (dataModel == null) {
			dataModel = new DummyDataModel(map);
		}
	}

	/**
	 * Choose a random intersection of the map as the destination and set the
	 * shortest travel time path as the search route.
	 *
	 * IMPORTANT: The first intersection on the resulted search route must not be the
	 * end intersection of the current road, i.e., it must not be that
	 * route.get(0) == currentLocation.road.to.
	 */

	//@Override
	public void planSearchRoute(LocationOnRoad currentLocation, long currentTime) {

		String pattern = dataModel.foo(); // Pretend we are using some data model for routing.

		route.clear();
		//System.out.println("Source location type:"+ currentLocation.getClass());
		Intersection sourceIntersection = currentLocation.road.to;
		int destinationIndex = rnd.nextInt(map.intersections().size());
		Intersection[] intersectionArray = map.intersections().values().toArray(new Intersection[map.intersections().size()]);
		//System.out.println(intersectionArray);
		//Intersection destinationIntersection = intersectionArray[destinationIndex];

		List<List<Double>> result = getHubLatLong();
		Random rand = new Random();
		int rand_int = rand.nextInt(result.get(0).size()-1);
		double longitude = result.get(0).get(rand_int);
		double latitude = result.get(1).get(rand_int);

		//Hub destination
		LocationOnRoad destination2 = mapMatch(longitude, latitude);
		Intersection destinationIntersection2 = destination2.road.to;


		/*double latitude1 = 0.0;
		double longitude1 = 0.0;

		//List<List<Double>> result = getHubLatLong();
		ArrayList<Double> longitude_Array = new ArrayList<Double>(
				Arrays.asList(0.0, -73.873, -73.994, -73.782, -73.991, -73.871, -73.992,
						-73.863, -73.864, -73.863, -73.99, -73.978, -73.991, -73.991, -73.982, -73.99, -73.79,
						-73.992, -73.995, -73.979, -74.006, -73.79, -74.004, -73.995, -73.962, -73.966, -73.982,
						-73.977, -73.777, -73.974, -73.973, -73.988, -73.989, -73.985, -73.976, -73.975, -73.982, -73.968,
						-73.987, -73.994, -74.002, -73.954, -73.972, -74.007, -73.984, -73.997, -73.989, -74.005, -73.973,
						-73.989, -74.008, -74.016
				));
		ArrayList<Double> latitude_Array = new ArrayList<Double>(
				Arrays.asList(0.0, 40.774, 40.751, 40.645, 40.75, 40.774,
						40.75, 40.769, 40.77, 40.77, 40.757, 40.752, 40.756, 40.751,
						40.768, 40.756, 40.647, 40.749, 40.75, 40.762, 40.74, 40.644, 40.742,
						40.74, 40.779, 40.762, 40.769, 40.752, 40.645, 40.763, 40.764, 40.738, 40.737,
						40.748, 40.752, 40.752, 40.763, 40.763, 40.761, 40.746, 40.74, 40.779, 40.757, 40.744,
						40.744, 40.737, 40.748, 40.741, 40.756, 40.758, 40.741, 40.715
		));

		LocationOnRoad hubDestinationpoint = null;
		double mintimetoHub = 10000000.0;

		for (int i=0; i<longitude_Array.size(); i++)
		{
			longitude1 = longitude_Array.get(i);
			latitude1 = latitude_Array.get(i);

			LocationOnRoad hubpoint = mapMatch(longitude1, latitude1);
			double timetohub = simulator.getMap().travelTimeBetween(currentLocation, hubpoint);
			if (timetohub < mintimetoHub)
			{
				mintimetoHub = timetohub;
				hubDestinationpoint = hubpoint;
			}
		}

		///System.out.println("Latitude:"+latitude1);
		///System.out.println("Latitude:"+longitude1);
		///System.out.println("Minimum Time:" +mintimetoHub);
		///System.out.println("Hub Destination point:"+hubDestinationpoint);

		//LocationOnRoad dropoffMatch = mapMatch(resource.getDropoffLon(), resource.getDropoffLat());
		//LocationOnRoad destination2 = mapMatch(longitude1, latitude1);
		//System.out.println("Destination location type:"+ hubDestinationpoint.getClass());
		Intersection destinationIntersection2 = hubDestinationpoint.road.to;*/







		//Values in program descriptions
		///System.out.println("Source Intersection:"+ sourceIntersection);
		///System.out.println("Destination Intersection:"+ destinationIntersection2);
		///System.out.println("Source intersection id:"+ sourceIntersection.id);
		///System.out.println("Destination intersection id:"+ destinationIntersection2.id);

		if (destinationIntersection2.id == sourceIntersection.id) {
			//System.out.println("Source and destination are same");
			// destination cannot be the source
			// if destination is the source, choose a neighbor to be the destination
			Road[] roadsFrom = sourceIntersection.roadsMapFrom.values().toArray(new Road[sourceIntersection.roadsMapFrom.values().size()]);
			destinationIntersection2 = roadsFrom[0].to;
		}
		route = map.shortestTravelTimePath(sourceIntersection, destinationIntersection2);
		///System.out.println("Moved to destination");
		route.poll(); // Ensure that route.get(0) != currentLocation.road.to.
		///System.out.println("Route polled");
		///System.out.println("**********************************************");
	}

	/**
	 * This method polls the first intersection in the current route and returns this intersection.
	 *
	 * This method is a callback method which is called when the agent reaches an intersection. The Simulator
	 * will move the agent to the returned intersection and then call this method again, and so on.
	 * This is how a planned route (in this case randomly planned) is executed by the Simulator.
	 *
	 * @return Intersection that the Agent is going to move to.
	 */

	@Override
	public Intersection nextIntersection(LocationOnRoad currentLocation, long currentTime) {
		if (route.size() != 0) {
			// Route is not empty, take the next intersection.
			///System.out.println("Getting Route");
			Intersection nextIntersection = route.poll();
			return nextIntersection;
		} else {
			// Finished the planned route. Plan a new route.
			///System.out.println("Planning New Route");
			planSearchRoute(currentLocation, currentTime);
			return route.poll();
		}
	}

	/**
	 * A dummy implementation of the assignedTo callback function which does nothing but clearing the current route.
	 * assignedTo is called when the agent is assigned to a resource.
	 */

	@Override
	public void assignedTo(LocationOnRoad currentLocation, long currentTime, long resourceId, LocationOnRoad resourcePikcupLocation, LocationOnRoad resourceDropoffLocation) {
		// Clear the current route.
		route.clear();

		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Agent " + this.id + " assigned to resource " + resourceId);
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "currentLocation = " + currentLocation);
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "currentTime = " + currentTime);
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "resourcePickupLocation = " + resourcePikcupLocation);
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "resourceDropoffLocation = " + resourceDropoffLocation);
	}

	public LocationOnRoad mapMatch(double longitude, double latitude) {
		Link link = map.getNearestLink(longitude, latitude);
		double xy[] = map.projector().fromLatLon(latitude, longitude);
		double [] snapResult = snap(link.from.getX(), link.from.getY(), link.to.getX(), link.to.getY(), xy[0], xy[1]);
		double distanceFromStartVertex = this.distance(snapResult[0], snapResult[1], link.from.getX(), link.from.getY());
		long travelTimeFromStartVertex = Math.round(distanceFromStartVertex / link.length * link.travelTime);
		long travelTimeFromStartIntersection = link.beginTime + travelTimeFromStartVertex;
		return new LocationOnRoad(link.road, travelTimeFromStartIntersection);
	}

	public double[] snap(double x1, double y1, double x2, double y2, double x, double y) {
		double[] snapResult = new double[3];
		double dist;
		double length = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);

		if (length == 0.0) {
			dist = this.distance(x1, y1, x, y);
			snapResult[0] = x1;
			snapResult[1] = y1;
			snapResult[2] = dist;
		} else {
			double t = ((x - x1) * (x2 - x1) + (y - y1) * (y2 - y1)) / length;
			if (t < 0.0) {
				dist = distance(x1, y1, x, y);
				snapResult[0] = x1;
				snapResult[1] = y1;
				snapResult[2] = dist;
			} else if (t > 1.0) {
				dist = distance(x2, y2, x, y);
				snapResult[0] = x2;
				snapResult[1] = y2;
				snapResult[2] = dist;
			} else {
				double proj_x = x1 + t * (x2 - x1);
				double proj_y = y1 + t * (y2 - y1);
				dist = distance(proj_x, proj_y, x, y);
				snapResult[0] = proj_x;
				snapResult[1] = proj_y;
				snapResult[2] = dist;
			}
		}
		return snapResult;
	}

	public double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}




	//changed
	public List<List<Double>> getHubLatLong() {

		String configFile = "etc/config.properties";
		Properties prop = new Properties();

		try{
			prop.load(new FileInputStream(configFile));
		}


		catch (Exception ex) {
			ex.printStackTrace();
		}

		String csvFile = prop.getProperty("comset.hubs_file").trim();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		ArrayList<Double> longitude_Array = new ArrayList<Double>();
		ArrayList<Double> latitude_Array = new ArrayList<Double>();
		List<List<Double>> result = new ArrayList<List<Double>>();

		try {

			br = new BufferedReader(new FileReader(csvFile));
			int count = 0;
			while ((line = br.readLine()) != null) {

				if (count == 0)
				{
					//System.out.println("First Row Coulumn Values");
					count = count + 1;
				}
				else {
					// use comma as separator
					String[] hubs = line.split(cvsSplitBy);
					String longitudeh = hubs[0];
					String latitudeh = hubs[1];
					double longitude_hub_d = Double.valueOf(longitudeh);
					double latitude_hub_d = Double.valueOf(latitudeh);

					//System.out.println("Longitude:" + longitude_hub_d);
					//System.out.println("Latitude:" + latitude_hub_d);

					longitude_Array.add(longitude_hub_d);
					latitude_Array.add(latitude_hub_d);
					count = count + 1;
				}
			}
			//System.out.println(longitude_Array);
			//System.out.println(latitude_Array);
			result.add(longitude_Array);
			result.add(latitude_Array);
			//System.out.println(longitude_Array.size());
			//System.out.println(latitude_Array.size());
			//System.out.println(longitude_Array.get(0));
			//System.out.println(latitude_Array.get(0));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return result;

	}





}