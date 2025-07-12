package jmat.tobrc;


public abstract class StationSummary implements Comparable<StationSummary> {
	public final String station;

	public StationSummary(final String station) {
		this.station = station;
	}

	public String getStation() {
		return this.station;
	}

	public abstract double getMin();
	public abstract double getAvg();
	public abstract double getMax();

	public int compareTo(final StationSummary other) {
		return this.getStation().compareTo(other.getStation());
	}
}

