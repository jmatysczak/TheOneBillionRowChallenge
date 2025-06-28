package jmat.tobrc;


import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;


public class TOBRC03 extends AbstractTOBRC {
	public static void main(final String args[]) throws Exception {
		new TOBRC03().run(args);
	}

	protected Collection<StationSummary> calculate(final File inputFile) throws Exception {
		final var measurementSummaries = new MeasurementSummary[Integer.parseInt(System.getProperty("TOBRC_Hash_Size", "20480"))];

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
							final var startIndex = Math.abs(stationHash) % measurementSummaries.length;
							for(int j = startIndex, count = measurementSummaries.length; count > 0; j = ((j + 1) % measurementSummaries.length), count--) {
								final var measurementSummary = measurementSummaries[j];
								if(measurementSummary == null) {
									var copyOfBytes = Arrays.copyOfRange(bytes, stationStart, stationFinish);
									measurementSummaries[j] = new MeasurementSummary(
										copyOfBytes, new String(copyOfBytes, StandardCharsets.UTF_8), measurement * sign
									);
									break;
								} else {
									var currentLength = stationFinish - stationStart;
									if(currentLength == measurementSummary.stationAsBytes.length) {
										final var match = Arrays.equals(
											bytes, stationStart, stationFinish,
											measurementSummary.stationAsBytes, 0, measurementSummary.stationAsBytes.length
										);
										if(match) {
											measurementSummary.add(measurement * sign);
											break;
										}
									}
								}
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

		return Arrays.stream(measurementSummaries)
			.filter(ms -> ms != null)
			.map(ms -> new StationSummary(ms.station, ms.getMin(), ms.getAvg(), ms.getMax()))
			.toList();
	}

	class MeasurementSummary {
		public final byte[] stationAsBytes;
		public final String station;
		private int min;
		private int max;
		private long sum;
		private int count;

		public MeasurementSummary(final byte[] stationAsBytes, final String station, final int measurement) {
			this.stationAsBytes = stationAsBytes;
			this.station = station;
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

