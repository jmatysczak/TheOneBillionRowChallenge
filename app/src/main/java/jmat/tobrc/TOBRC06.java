package jmat.tobrc;


import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;


public class TOBRC06 extends AbstractTOBRC {
	public static void main(final String args[]) throws Exception {
		new TOBRC06().run(args);
	}

	protected Collection<? extends StationSummary> calculate(final File inputFile) throws Exception {
		var measurementSummaries = new MeasurementSummary[1024];
		var numberOfMeasurementSummaries = 0;
		var measurementSummariesTheshold = measurementSummaries.length * 0.75;

		try(var fileChannel = FileChannel.open(inputFile.toPath(), StandardOpenOption.READ)) {
			final var fileSize = fileChannel.size();
			final var numberOfProcessors = Runtime.getRuntime().availableProcessors();
			// Assume no measurement (line of the file) is great than 512 bytes.
			// This allows the code below to start at the end of a line and assume
			// it can always read a full measurement before the end of the buffer.
			final var bufferSize = 1024;

			var numberOfChunks = numberOfProcessors;
			while((fileSize / numberOfChunks + bufferSize) > Integer.MAX_VALUE) numberOfChunks += numberOfProcessors;
			var chunkSize = fileSize / numberOfChunks;
			var mappedSize = chunkSize + bufferSize;

			if(fileSize < Integer.MAX_VALUE) {
				chunkSize = fileSize;
				mappedSize = fileSize;
				numberOfChunks = 1;
			}

			// Assume no station name is great than 512 bytes.
			final var tempStationName = new byte[512];
			for(var i = 0; i < numberOfChunks; i++) {
				final var remainder = fileSize - (i * chunkSize);
				if(remainder < mappedSize) mappedSize = remainder;
				final var mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, i * chunkSize, mappedSize);

				if(i > 0) {
					// Chunks after the first chunk will most likely start in the middle of a measurement.
					// The previous chunk will have processed that measurement.
					// So look for the start of the next measurement.
					while(mappedByteBuffer.get() != '\n');
				}

				while(mappedByteBuffer.position() < chunkSize) {
					var stationLength = 0;
					var stationHashCode = 0;
					var sign = 1;
					var measurement = 0;

					// Read the station name.
					var b = mappedByteBuffer.get();
					while(b != ';') {
						tempStationName[stationLength++] = b;
						stationHashCode = stationHashCode * 31 + b;
						b = mappedByteBuffer.get();
					}

					// Read the measurement.
					b = mappedByteBuffer.get();
					if(b == '-') {
						sign = -1;
						b = mappedByteBuffer.get();
					}
					while(b != '\n') {
						if(b != '.') {
							// Ignore periods.
							// Per the spec/rules the measurement will always have 1 decimal digit.
							// So we will keep track of the measurements as integers and divide by 10 when we are finished.
							measurement = (measurement * 10) + (b - '0');
						}
						b = mappedByteBuffer.get();
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

