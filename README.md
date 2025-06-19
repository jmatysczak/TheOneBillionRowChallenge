### The One Billion Row Challenge

Some fun coding using: https://github.com/gunnarmorling/1brc

```
# To run tests for a specific version:
$ gradle run -Pversion=00

# To process a file:
$ gradle run -Pversion=00 --args="../../1brc-data/measurements_100m.txt"

# To run with Java Flight Recorder:
$ gradle run -Pversion=00 -Pjfr="-XX:StartFlightRecording=duration=20s,filename=100m.jfr" --args="../../1brc-data/measurements_100m.txt"

# To view the methods that are sampled the most:
$ jfr view hot-methods app/100m.jfr

```

#### Results

Times in seconds.

| Version                                                                                                                     | Threads | 10m    | 100m   | 1b      |
| :------                                                                                                                     | ------: | -----: | -----: | ------: |
| [Baseline](https://github.com/gunnarmorling/1brc/blob/main/src/main/java/dev/morling/onebrc/CalculateAverage_baseline.java) |       1 |   1.77 |  13.67 |  161.40 |
| [00](https://github.com/jmatysczak/TheOneBillionRowChallenge/blob/main/app/src/main/java/jmat/tobrc/TOBRC00.java)           |       1 |   2.84 |  23.78 | 1015.45 |


#### [TOBRC00](https://github.com/jmatysczak/TheOneBillionRowChallenge/blob/main/app/src/main/java/jmat/tobrc/TOBRC00.java)

A simple/basic/straightforward implementation.

I had to use java.math.BigDecimal for the mean to get
[measurements-rounding.txt](https://github.com/gunnarmorling/1brc/blob/main/src/test/resources/samples/measurements-rounding.txt)
to pass.

```
$ jfr view hot-methods app/100m.jfr

                                 Java Methods that Executes the Most

Method                                                                                Samples Percent
------------------------------------------------------------------------------------- ------- -------
java.lang.String.split(char, int, boolean)                                                 84  19.72%
java.io.BufferedReader.readLine(boolean, boolean[])                                        60  14.08%
jdk.internal.math.DoubleToDecimal.toChars1(byte[], int, int, int, int, int)                53  12.44%
jdk.internal.math.DoubleToDecimal.toDecimal(byte[], int, double, FormattedFPDecimal)       41   9.62%
jdk.internal.math.FloatingDecimal.readJavaFormatString(String)                             33   7.75%
jdk.internal.math.FloatingDecimal$ASCIIToBinaryBuffer.doubleValue()                        28   6.57%
jmat.tobrc.TOBRC00.calculate(File)                                                         26   6.10%
java.math.BigDecimal.<init>(char[], int, int, MathContext)                                 17   3.99%
sun.nio.cs.UTF_8$Decoder.decodeArrayLoop(ByteBuffer, CharBuffer)                           13   3.05%
jdk.internal.util.ArraysSupport.unsignedHashCode(int, byte[], int, int)                    11   2.58%
java.math.BigDecimal.<init>(char[], int, int)                                               8   1.88%
java.lang.String.equals(Object)                                                             8   1.88%
java.util.HashMap.getNode(Object)                                                           7   1.64%
jdk.internal.math.ToDecimal.removeTrailingZeroes(byte[], int)                               7   1.64%
java.lang.String.<init>(Charset, byte[], int, int)                                          6   1.41%
java.lang.String.substring(int, int)                                                        6   1.41%
sun.nio.cs.UTF_8$Decoder.xflow(Buffer, int, int, Buffer, int, int)                          5   1.17%
jdk.internal.math.DoubleToDecimal.toChars(byte[], int, long, int, FormattedFPDecimal)       5   1.17%
jdk.internal.math.FloatingDecimal.parseDouble(String)                                       2   0.47%
java.lang.String.<init>(byte[], int, int, Charset)                                          2   0.47%
java.io.BufferedReader.readLine()                                                           1   0.23%
java.lang.StringLatin1.replace(byte[], char, char)                                          1   0.23%
java.io.FileInputStream.available()                                                         1   0.23%
jdk.internal.util.ArraysSupport.vectorizedHashCode(Object, int, int, int, int)              1   0.23%

$ jfr view allocation-by-class app/100m.jfr

                              Allocation by Class

Object Type                                                 Allocation Pressure
----------------------------------------------------------- -------------------
byte[]                                                                   32.39%
java.math.BigDecimal                                                     19.17%
java.lang.String                                                         16.89%
java.lang.Object[]                                                       13.87%
jdk.internal.math.FloatingDecimal$ASCIIToBinaryBuffer                     7.00%
java.lang.String[]                                                        5.83%
char[]                                                                    4.77%
java.util.concurrent.ConcurrentHashMap$Node[]                             0.04%
java.nio.HeapCharBuffer                                                   0.02%
int[]                                                                     0.00%
java.util.ArrayList$SubList                                               0.00%
java.util.ArrayList                                                       0.00%
java.math.BigInteger                                                      0.00%
java.util.regex.Pattern                                                   0.00%
```
