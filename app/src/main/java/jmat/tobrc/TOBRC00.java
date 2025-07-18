package jmat.tobrc;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;


public class TOBRC00 extends AbstractTOBRC {
	public static void main(final String args[]) throws Exception {
		new TOBRC00().run(args);
	}

	protected Collection<? extends StationSummary> calculate(final File inputFile) throws Exception {
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

		return stationToMeasurementSummary.values();
	}

	class MeasurementSummary extends StationSummary {
		public double min;
		public double max;
		public BigDecimal sum;
		public int count;

		public MeasurementSummary(final String station, final double measurement) {
			super(station);
			this.min = measurement;
			this.max = measurement;
			this.sum = BigDecimal.valueOf(measurement);
			this.count = 1;
		}

		public void add(final double measurement) {
			if(measurement < this.min) this.min = measurement;
			if(this.max < measurement) this.max = measurement;
			this.sum = this.sum.add(BigDecimal.valueOf(measurement));
			this.count++;
		}

		public double getMin() {
			return this.min;
		}

		public double getAvg() {
			return this.sum.divide(BigDecimal.valueOf(this.count), 1, RoundingMode.HALF_UP).doubleValue();
		}

		public double getMax() {
			return this.max;
		}
	}
}

