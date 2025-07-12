package jmat.tobrc;


import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;


public abstract class AbstractTOBRC {
	public void run(final String args[]) throws Exception {
		if(args.length == 0) {
			this.runTests();
		} else if (args.length == 1) {
			this.processFile(args[0]);
		} else {
			System.out.println("Usage: Specify no arguments to run tests. Specify a single file path to process that file.");
		}
	}

	protected void runTests() throws Exception {
		final var nameToTestCase = new HashMap<String, TestCase>();

		System.out.println("Loading test cases...");
		final var testCaseFiles = new File("../../1brc-main/src/test/resources/samples/").listFiles();
		for(var testCaseFile : testCaseFiles) {
			final var name = testCaseFile.getName().substring(0, testCaseFile.getName().lastIndexOf("."));
			final var extensionlessTestCaseFile = testCaseFile.toString().substring(0, testCaseFile.toString().lastIndexOf(".") + 1);

			if(!nameToTestCase.containsKey(name)) {
				final var testCase = new TestCase(
					name,
					new File(extensionlessTestCaseFile + "txt"),
					new File(extensionlessTestCaseFile + "out")
				);
				nameToTestCase.put(name, testCase);

				System.out.format("\t%s%n", testCase);
			}
		}

		System.out.println("");

		System.out.println("Running rest cases:");
		for(var testCase : nameToTestCase.values()) {
			System.out.format("\t%s...", testCase.name());

			final var actualFile = File.createTempFile(testCase.name(), ".txt");

			this.processFile(testCase.inputFile(), actualFile);

			final var actualResults = Files.readString(actualFile.toPath());
			final var expectedResults = Files.readString(testCase.expectedFile().toPath());

			if(actualResults.equals(expectedResults)) {
				actualFile.delete();
				System.out.println("Passed!");
			} else {
				System.out.println("Failed!");
				System.out.format("\t\tExpected: %s%n", expectedResults);
				System.out.format("\t\tActual:   %s%n", actualResults);
				System.exit(1);
			}
		}
	}

	protected void processFile(final String inputFile) throws Exception {
		final long startTime = System.nanoTime();

		this.processFile(new File(inputFile), File.createTempFile("TOBRCResults", ".txt"));

		final double elapsedTimeSeconds = (System.nanoTime() - startTime) / 1_000_000_000.00;

		System.out.format("Elapsed time for %s: %s seconds", this.getClass().getName(), elapsedTimeSeconds);
	}

	protected void processFile(final File inputFile, final File actualFile) throws Exception {
		final var results = this.calculate(inputFile);
		this.write(results, actualFile);
	}

	protected abstract Collection<? extends StationSummary> calculate(final File inputFile) throws Exception;

	protected void write(final Collection<? extends StationSummary> results, final File outputFile) throws Exception {
		try(final var writer = new FileWriter(outputFile)) {
			writer.write("{");
			boolean addComma = false;
			final var resultsSorted = results.stream().sorted().toList();
			for(final var result : resultsSorted) {
				if(addComma) writer.write(", ");

				writer.write(result.getStation());
				writer.write("=");
				writer.write(String.format("%.1f", result.getMin()));
				writer.write("/");
				writer.write(String.format("%.1f", result.getAvg()));
				writer.write("/");
				writer.write(String.format("%.1f", result.getMax()));

				addComma = true;
			}
			writer.write("}\n");
		}
	}
}

