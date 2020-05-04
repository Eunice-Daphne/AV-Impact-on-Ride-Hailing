package DataParsing;

/**
 *
 * @author TijanaKlimovic
 */
public class Resource extends TimestampAbstract {

	private double dropffLat; // drop-off latitude of resource
	private double dropffLon; // drop-off longitude of resource
	private double tripDistance;
	private double fairAmount;

	//constructor initiating each field of the Resource record type
	public Resource(double pickupLat, double pickupLon, double dropffLat, double dropffLon, long time, double tripDistance, double fairAmount) {
		super(pickupLat, pickupLon, time);
		this.dropffLat = dropffLat;
		this.dropffLon = dropffLon;
		this.tripDistance = tripDistance;
		this.fairAmount = fairAmount;
	}

	/**
	 * returns the dropff latitude of the resource
	 * 
	 * @return {@code this.dropffLat}
	 */
	public double getDropoffLat() {
		return dropffLat;
	}

	/**
	 * returns the dropff longitude of the resource
	 * 
	 * @return {@code this.dropffon}
	 */
	public double getDropoffLon() {
		return dropffLon;
	}

	public double getTripDistance() {
		return tripDistance;
	}

	public double getFairAmount() {
		return fairAmount;
	}

}
