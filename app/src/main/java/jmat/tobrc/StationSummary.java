package jmat.tobrc;


public record StationSummary(String station, double min, double avg, double max) implements Comparable<StationSummary> {
	public int compareTo(final StationSummary other) {
		return this.station.compareTo(other.station);
	}
}

