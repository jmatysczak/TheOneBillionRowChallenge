package jmat.tobrc;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;


public class TOBRC01 extends AbstractTOBRC {
	public static void main(final String args[]) throws Exception {
		new TOBRC01().run(args);
	}

	protected Collection<StationSummary> calculate(final File inputFile) throws Exception {
		final var stationToMeasurementSummary = new HashMap<String, MeasurementSummary>();

		try(var in = new BufferedReader(new FileReader(inputFile))) {
			String line = null;
			while((line = in.readLine()) != null) {
				final String[] stationAndMeasurement = line.split(";");
				final String station = stationAndMeasurement[0];
				final double measurement = Double.parseDouble(stationAndMeasurement[1]);

				final var measurementSummary = stationToMeasurementSummary.get(station);
				if(measurementSummary == null) {
					stationToMeasurementSummary.put(station, new MeasurementSummary(station, measurement));
				} else {
					measurementSummary.add(measurement);
				}
			}
		}

		return stationToMeasurementSummary.values().stream().map(ms -> new StationSummary(ms.station, ms.min, ms.getAvg(), ms.max)).toList();
	}

	class MeasurementSummary {
		public final String station;
		public double min;
		public double max;
		public double sum;
		public int count;

		public MeasurementSummary(final String station, final double measurement) {
			this.station = station;
			this.min = measurement;
			this.max = measurement;
			this.sum = measurement;
			this.count = 1;
		}

		public void add(final double measurement) {
			if(measurement < this.min) this.min = measurement;
			if(this.max < measurement) this.max = measurement;
			this.sum += measurement;
			this.count++;
		}

		public double getAvg() {
			// Rounding logic copied from:
			// https://github.com/gunnarmorling/1brc/blob/main/src/main/java/dev/morling/onebrc/CalculateAverage_baseline.java
			return (Math.round(this.sum * 10.0) / 10.0) / this.count;
		}
	}
}

