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

	protected Collection<? extends StationSummary> calculate(final File inputFile) throws Exception {
		var measurementSummaries = new MeasurementSummary[1024];
		var numberOfMeasurementSummaries = 0;
		var measurementSummariesTheshold = measurementSummaries.length * 0.75;

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
							final var hashCode = Math.abs(stationHash);
							final var startIndex = hashCode % measurementSummaries.length;
							for(var j = startIndex; true; j = ((j + 1) % measurementSummaries.length)) {
								final var measurementSummary = measurementSummaries[j];
								if(measurementSummary == null) {
									var stationAsBytes = Arrays.copyOfRange(bytes, stationStart, stationFinish);
									var stationAsString = new String(stationAsBytes, StandardCharsets.UTF_8);
									measurementSummaries[j] = new MeasurementSummary(hashCode, stationAsBytes, stationAsString, measurement * sign);
									numberOfMeasurementSummaries++;

									if(numberOfMeasurementSummaries > measurementSummariesTheshold) {
										var newMeasurementSummaries = new MeasurementSummary[measurementSummaries.length * 2];
										for(var k = 0; k < measurementSummaries.length; k++) {
											final var measurementSummaryToMove = measurementSummaries[k];
											if(measurementSummaryToMove != null) {
												final var newStartIndex = measurementSummaryToMove.hashCode % newMeasurementSummaries.length;
												for(var l = newStartIndex; true; l = ((l + 1) % newMeasurementSummaries.length)) {
													if(newMeasurementSummaries[l] == null) {
														newMeasurementSummaries[l] = measurementSummaryToMove;
														break;
													}
												}
											}
										}
										measurementSummaries = newMeasurementSummaries;
										measurementSummariesTheshold = measurementSummaries.length * 0.75;
									}

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
			.toList();
	}

	class MeasurementSummary extends StationSummary {
		public final int hashCode;
		public final byte[] stationAsBytes;
		private int min;
		private int max;
		private long sum;
		private int count;

		public MeasurementSummary(final int hashCode, final byte[] stationAsBytes, final String station, final int measurement) {
			super(station);
			this.hashCode = hashCode;
			this.stationAsBytes = stationAsBytes;
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

