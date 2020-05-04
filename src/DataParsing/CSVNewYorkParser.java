package DataParsing;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import MapCreation.MapCreator;

/**       
 * The CSVNewYorkParser class parses a New York TLC data file for a month before July of 2016.
 * The following columns are extracted from each row to create a Resource object.
 * 
 * 1. "tpep_pickup_datetime": This time stamp is treated as the time at which the resource (passenger) 
 *    is introduced to the system.
 * 2. "pickup_longitude", "pickup_latitude": The location at which the resource (passenger) is introduced.
 * 3. "dropoff_longitude", "dropoff_latitude": The location at which the resource (passenger) is dropped off. 
 *   
 * @author TijanaKlimovic
 */
public class CSVNewYorkParser {

	// absolute path to csv file to be parsed
	private String path;

	// list of all resources
	private ArrayList<Resource> resources = new ArrayList<>();    

	DateTimeFormatter dtf;

	ZoneId zoneId;

	/**
	 * Constructor of the CSVNewYorkParser class
	 * @param path full path to the resource dataset file
	 * @param zoneId the time zone id of the studied area
	 */
	// resource specified in csv file located at path
	public CSVNewYorkParser(String path, ZoneId zoneId) {
	//public CSVNewYorkParser(ZoneId zoneId) {
		this.path = path;
		dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		// TLC Trip Record data uses local time. So the zone ID is America/New_York
		this.zoneId = zoneId;
	}

	/**
	 * Converts the date+time (timestamp) string into the Linux epoch.
	 *
	 * @param timestamp string containing formatted date and time data to be
	 * converted
	 * @return long value of the timestamp string
	 */
	public Long dateConversion(String timestamp) {
		long l = 0L;
		LocalDateTime ldt = LocalDateTime.parse(timestamp, dtf);
		ZonedDateTime zdt = ZonedDateTime.of(ldt, zoneId);
		l = zdt.toEpochSecond(); //Returns Linux epoch, i.e., the number of seconds since January 1, 1970, 00:00:00 GMT until time specified in zdt

		return l;
	}

	/**
	 * Parse the csv file.
	 * 
	 * @return ArrayList<Resource>
	 */
	/*public ArrayList<Resource> parse() {

		try{
			String databaseURL = "jdbc:mysql://localhost:3306/av_schema";
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection conn = DriverManager.getConnection(databaseURL, "root", "admin");
			if (conn == null) {
				System.out.println("Not Connected to the database");
			}

			// our SQL SELECT query.
			// if you only need a few columns, specify them by name instead of using "*"
			String query = "SELECT * FROM av_schema.yellow_trip_data_test";

			// create the java statement
			Statement st = conn.createStatement();

			// execute the query, and get a java resultset
			ResultSet rs = st.executeQuery(query);

			while (rs.next())
			{
				//int id = rs.getInt("VendorID");
				long time = dateConversion(rs.getString("tpep_pickup_datetime"));
				double pickupLon = Double.parseDouble(rs.getString("pickup_longitude"));
				double pickupLat = Double.parseDouble(rs.getString("pickup_latitude"));
				double dropoffLon = Double.parseDouble(rs.getString("dropoff_longitude"));
				double dropoffLat = Double.parseDouble(rs.getString("dropoff_latitude"));

				System.out.println("time pickupLon pickupLat dropoffLon dropoffLat" + time + " " +pickupLat +" "+ pickupLat + " "+ dropoffLon + " " +dropoffLat );
				if (!(MapCreator.insidePolygon(pickupLon, pickupLat) && MapCreator.insidePolygon(dropoffLon, dropoffLat))) {
					continue;
				}
				resources.add(new Resource(pickupLat, pickupLon, dropoffLat, dropoffLon, time)); //create new resource with the above fields
			}
			st.close();

		}
		catch (Exception e) {

			e.printStackTrace();
		}
		return resources;
	} */

	public ArrayList<Resource> parse() {
		try {
			Scanner sc = new Scanner(new File(path));   //scanner will scan the file specified by path
			sc.useDelimiter(",|\n");    //scanner will skip over "," and "\n" found in file
			sc.nextLine(); // skip the header

			//while there are tokens in the file the scanner will scan the input
			//each line in input file will contain 4 tokens for the scanner and will be in the format : latitude longitude time type
			//per line of input file we will create a new TimestampAgRe object
			// and save the 4 tokens of each line in the corresponding field of the TimestampAgRe object
			while (sc.hasNext()) {
				sc.next();// skip first VendorID
				long time = dateConversion(sc.next()); //tpep_pickup_datetime
				sc.next();// skip these fields tpep_dropoff_datetime
				sc.next(); //passenger_count
				double tripDistance = Double.parseDouble(sc.next());
				double pickupLon = Double.parseDouble(sc.next());
				double pickupLat = Double.parseDouble(sc.next());
				sc.next();// RatecodeID
				sc.next();//store_and_fwd_flag
				double dropoffLon = Double.parseDouble(sc.next());
				double dropoffLat = Double.parseDouble(sc.next());
				sc.next(); //payment_type
				double fare_amount = Double.parseDouble(sc.next()); //payment_type
				sc.nextLine(); //skip rest of fileds in this line
				// Only keep the resources such that both pickup location and dropoff location are within the bounding polygon.
				if (!(MapCreator.insidePolygon(pickupLon, pickupLat) && MapCreator.insidePolygon(dropoffLon, dropoffLat))) {
					continue;
				}
				resources.add(new Resource(pickupLat, pickupLon, dropoffLat, dropoffLon, time,tripDistance,fare_amount)); //create new resource with the above fields
			}
			System.out.println(" Number of resources actually taken : " +resources.size());
			sc.close();
		} catch (Exception e) {

			e.printStackTrace();
		}
		return resources;
	}

}
