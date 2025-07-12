package jmat.tobrc;


import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;


public class TOBRC02 extends AbstractTOBRC {
	public static void main(final String args[]) throws Exception {
		new TOBRC02().run(args);
	}

	protected Collection<? extends StationSummary> calculate(final File inputFile) throws Exception {
		final var lookupStation = new Station();
		final var stationToMeasurementSummary = new HashMap<Station, MeasurementSummary>();

		try(var in = new FileInputStream(inputFile)) {
			final var bytes = new byte[1024 * 4];

			var offset = 0;
			var stationStart = 0;
			var stationFinish = -1;
			var stationHash = 0;
			var calculatingHash = true;
			var sign = 1;
			var measurement = 0;
			while(in.available() > 0) {
				var bytesRead = in.read(bytes, offset, bytes.length - offset);
				var bytesEnd = offset + bytesRead;

				for(var i = offset; i < bytesEnd; i++) {
					if(calculatingHash) {
						if(bytes[i] == ';') {
							stationFinish = i;
							calculatingHash = false;
						} else {
							stationHash = stationHash * 31 + bytes[i];
						}
					} else {
						if(bytes[i] == '\n') {
							lookupStation.update(bytes, stationStart, stationFinish, stationHash);
							var measurementSummary = stationToMeasurementSummary.get(lookupStation);
							if(measurementSummary == null) {
								var copyOfBytes = Arrays.copyOfRange(bytes, stationStart, stationFinish);
								stationToMeasurementSummary.put(
									new Station(copyOfBytes, 0, copyOfBytes.length, stationHash),
									new MeasurementSummary(new String(copyOfBytes, StandardCharsets.UTF_8), measurement * sign)
								);
							} else {
								measurementSummary.add(measurement * sign);
							}

							// Reset state for the next measurement.
							stationStart = i + 1;
							stationFinish = -1;
							stationHash = 0;
							calculatingHash = true;
							sign = 1;
							measurement = 0;
						} else if(bytes[i] == '-') {
							sign = -1;
						} else if(bytes[i] == '.') {
							// Ignore.
							// Per the spec/rules the measurement will always have 1 decimal digit.
							// So we will keep track of the measurements as integers and divide by 10 when we are finished.
						} else {
							measurement = (measurement * 10) + (bytes[i] - '0');
						}
					}
				}

				// At this point the current buffer is consumed. We have most likely partially consumed a measurement.
				// Copy the partially consumed measurement to the beginning of the buffer and adjust the state appropriately.
				if(stationFinish != -1) {
					// We have completely consumed the station name.
					stationFinish = stationFinish - stationStart;
				}
				System.arraycopy(bytes, stationStart, bytes, 0, bytes.length - stationStart);
				offset = bytes.length - stationStart;
				stationStart = 0;
			}
		}

		return stationToMeasurementSummary.values();
	}

	class Station {
		byte[] bytes;
		int offset;
		int end;
		int hashCode;

		public Station() {
		}

		public Station(final byte[] bytes, final int offset, final int end, final int hashCode) {
			this.bytes = bytes;
			this.offset = offset;
			this.end = end;
			this.hashCode = hashCode;
		}

		public void update(final byte[] bytes, final int offset, final int end, final int hashCode) {
			this.bytes = bytes;
			this.offset = offset;
			this.end = end;
			this.hashCode = hashCode;
		}

		@Override
		public boolean equals(Object other) {
			var otherStation = (Station)other;

			var myLength = this.end - this.offset;
			var otherLength = otherStation.end - otherStation.offset;
			if(myLength != otherLength) return false;

			return Arrays.equals(this.bytes, this.offset, this.end, otherStation.bytes, otherStation.offset, otherStation.end);
		}

		@Override
		public int hashCode() {
			return this.hashCode;
		}
	}

	class MeasurementSummary extends StationSummary {
		private int min;
		private int max;
		private long sum;
		private int count;

		public MeasurementSummary(final String station, final int measurement) {
			super(station);
			this.min = measurement;
			this.max = measurement;
			this.sum = measurement;
			this.count = 1;
		}

		public void add(final int measurement) {
			if(measurement < this.min) this.min = measurement;
			if(this.max < measurement) this.max = measurement;
			this.sum += measurement;
			this.count++;
		}

		public double getMin() {
			return this.min / 10.0;
		}

		public double getMax() {
			return this.max / 10.0;
		}

		public double getAvg() {
			return (this.sum / 10.0) / this.count;
		}
	}
}

