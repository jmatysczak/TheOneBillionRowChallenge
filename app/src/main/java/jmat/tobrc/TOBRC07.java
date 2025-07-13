package jmat.tobrc;


import java.io.File;
import java.lang.foreign.Arena;
import java.lang.foreign.ValueLayout;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;


public class TOBRC07 extends AbstractTOBRC {
	public static void main(final String args[]) throws Exception {
		new TOBRC07().run(args);
	}

	protected Collection<? extends StationSummary> calculate(final File inputFile) throws Exception {
		var measurementSummaries = new MeasurementSummary[1024];
		var numberOfMeasurementSummaries = 0;
		var measurementSummariesTheshold = measurementSummaries.length * 0.75;

		try(var fileChannel = FileChannel.open(inputFile.toPath(), StandardOpenOption.READ)) {
			final var fileSize = fileChannel.size();
			final var memorySegment = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize, Arena.global());

			// Assume no station name is great than 512 bytes.
			final var tempStationName = new byte[512];

			var i = 0L;
			while(i < fileSize) {
				var stationLength = 0;
				var stationHashCode = 0;
				var sign = 1;
				var measurement = 0;

				// Read the station name.
				var b = memorySegment.get(ValueLayout.JAVA_BYTE, i++);
				while(b != ';') {
					tempStationName[stationLength++] = b;
					stationHashCode = stationHashCode * 31 + b;
					b = memorySegment.get(ValueLayout.JAVA_BYTE, i++);
				}

				// Read the measurement.
				b = memorySegment.get(ValueLayout.JAVA_BYTE, i++);
				if(b == '-') {
					sign = -1;
					b = memorySegment.get(ValueLayout.JAVA_BYTE, i++);
				}
				while(b != '\n') {
					if(b != '.') {
						// Ignore periods.
						// Per the spec/rules the measurement will always have 1 decimal digit.
						// So we will keep track of the measurements as integers and divide by 10 when we are finished.
						measurement = (measurement * 10) + (b - '0');
					}
					b = memorySegment.get(ValueLayout.JAVA_BYTE, i++);
				}

				final var hashCode = Math.abs(stationHashCode);
				for(var j = hashCode % measurementSummaries.length; true; j = ((j + 1) % measurementSummaries.length)) {
					final var measurementSummary = measurementSummaries[j];
					if(measurementSummary == null) {
						var stationAsBytes = Arrays.copyOfRange(tempStationName, 0, stationLength);
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
						var otherStationAsBytes = measurementSummary.stationAsBytes;
						if(stationLength == otherStationAsBytes.length) {
							var k = stationLength;
							while(--k >= 0 && tempStationName[k] == otherStationAsBytes[k]);
							if(k == -1) {
								measurementSummary.add(measurement * sign);
								break;
							}
						}
					}
				}
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

