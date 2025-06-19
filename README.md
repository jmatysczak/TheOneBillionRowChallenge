### The One Billion Row Challenge

Some fun coding using: https://github.com/gunnarmorling/1brc

```
# To run tests for a specific version:
$ gradle run -Pversion=00

# To process a file:
$ gradle run -Pversion=00 --args="../../1brc-data/measurements_1b.txt"
```

#### Results

Times in seconds.

| Version                                                                                                                     | Threads | 10m    | 100m   | 1b      |
| :------                                                                                                                     | ------: | -----: | -----: | ------: |
| [Baseline](https://github.com/gunnarmorling/1brc/blob/main/src/main/java/dev/morling/onebrc/CalculateAverage_baseline.java) |       1 |   1.77 |  13.67 |  161.40 |
| [00](https://github.com/jmatysczak/TheOneBillionRowChallenge/blob/main/app/src/main/java/jmat/tobrc/TOBRC00.java)           |       1 |   2.84 |  23.78 | 1015.45 |


#### TOBRC00

A simple/basic/straightforward implementation.

I had to use java.math.BigDecimal for the mean to get
[measurements-rounding.txt](https://github.com/gunnarmorling/1brc/blob/main/src/test/resources/samples/measurements-rounding.txt)
to pass.
