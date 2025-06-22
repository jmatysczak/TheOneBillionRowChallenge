### The One Billion Row Challenge

Some fun coding using: https://github.com/gunnarmorling/1brc

```
# To run tests for a specific version:
$ gradle run -Pversion=00

# To process a file:
$ gradle run -Pversion=00 --args="../../1brc-data/measurements_100m.txt"

# To run with Java Flight Recorder:
$ gradle run -Pversion=00 -Pjfr="-XX:StartFlightRecording=duration=120s,filename=100m.jfr" --args="../../1brc-data/measurements_100m.txt"

# To view the methods that are sampled the most:
$ jfr view hot-methods app/100m.jfr

```

#### Results

Times in seconds.

| Version                                                                                                                     | Threads | 10m    | 100m   | 1b     |
| :------                                                                                                                     | ------: | -----: | -----: | -----: |
| [Baseline](https://github.com/gunnarmorling/1brc/blob/main/src/main/java/dev/morling/onebrc/CalculateAverage_baseline.java) |       1 |   1.77 |  13.67 | 161.40 |
| [00](https://github.com/jmatysczak/TheOneBillionRowChallenge/blob/main/app/src/main/java/jmat/tobrc/TOBRC00.java)           |       1 |   2.81 |  24.74 | 236.02 |
| [01](https://github.com/jmatysczak/TheOneBillionRowChallenge/blob/main/app/src/main/java/jmat/tobrc/TOBRC01.java)           |       1 |   1.93 |  15.44 | 155.97 |


#### [TOBRC00](https://github.com/jmatysczak/TheOneBillionRowChallenge/blob/main/app/src/main/java/jmat/tobrc/TOBRC00.java)

A simple/basic/straightforward implementation.

I had to use `java.math.BigDecimal` for the mean to get
[measurements-rounding.txt](https://github.com/gunnarmorling/1brc/blob/main/src/test/resources/samples/measurements-rounding.txt)
to pass.

```
$ gradle run -Pversion=00 -Pjfr="-XX:StartFlightRecording=duration=120s,filename=100m.jfr" --args="../../1brc-data/measurements_100m.txt"
$ jfr view hot-methods app/100m.jfr

                                 Java Methods that Executes the Most

Method                                                                                Samples Percent
------------------------------------------------------------------------------------- ------- -------
java.io.BufferedReader.readLine(boolean, boolean[])                                        89  16.70%
java.lang.String.split(char, int, boolean)                                                 83  15.57%
jdk.internal.math.DoubleToDecimal.toChars1(byte[], int, int, int, int, int)                72  13.51%
jdk.internal.math.DoubleToDecimal.toDecimal(byte[], int, double, FormattedFPDecimal)       63  11.82%
jdk.internal.math.FloatingDecimal.readJavaFormatString(String)                             37   6.94%
jmat.tobrc.TOBRC00.calculate(File)                                                         36   6.75%
java.math.BigDecimal.<init>(char[], int, int, MathContext)                                 36   6.75%
sun.nio.cs.UTF_8$Decoder.decodeArrayLoop(ByteBuffer, CharBuffer)                           28   5.25%
jdk.internal.math.FloatingDecimal$ASCIIToBinaryBuffer.doubleValue()                        18   3.38%
java.util.HashMap.getNode(Object)                                                          10   1.88%
jdk.internal.util.ArraysSupport.unsignedHashCode(int, byte[], int, int)                    10   1.88%
java.math.BigDecimal.<init>(char[], int, int)                                               9   1.69%
java.lang.String.substring(int, int)                                                        9   1.69%
jdk.internal.math.ToDecimal.removeTrailingZeroes(byte[], int)                               8   1.50%
jdk.internal.math.DoubleToDecimal.toChars(byte[], int, long, int, FormattedFPDecimal)       7   1.31%
java.lang.String.<init>(Charset, byte[], int, int)                                          4   0.75%
java.lang.String.equals(Object)                                                             3   0.56%
java.lang.String.<init>(byte[], int, int, Charset)                                          3   0.56%
java.lang.StringUTF16.compress(byte[], int, int)                                            3   0.56%
sun.nio.cs.UTF_8$Decoder.xflow(Buffer, int, int, Buffer, int, int)                          2   0.38%
jdk.internal.math.FloatingDecimal.parseDouble(String)                                       1   0.19%
java.math.BigDecimal.valueOf(long, int)                                                     1   0.19%
jdk.internal.util.ArraysSupport.newLength(int, int, int)                                    1   0.19%



$ jfr view allocation-by-class app/100m.jfr

                              Allocation by Class

Object Type                                                 Allocation Pressure
----------------------------------------------------------- -------------------
byte[]                                                                   36.64%
java.lang.String                                                         18.68%
java.math.BigDecimal                                                     17.54%
java.lang.Object[]                                                       11.55%
char[]                                                                    6.25%
java.lang.String[]                                                        5.50%
jdk.internal.math.FloatingDecimal$ASCIIToBinaryBuffer                     3.80%
java.util.concurrent.ConcurrentHashMap$Node[]                             0.04%
java.nio.HeapCharBuffer                                                   0.01%
int[]                                                                     0.00%
java.util.ArrayList                                                       0.00%
java.util.ArrayList$SubList                                               0.00%
java.math.BigInteger                                                      0.00%
java.util.regex.Pattern                                                   0.00%
```


#### [TOBRC01](https://github.com/jmatysczak/TheOneBillionRowChallenge/blob/main/app/src/main/java/jmat/tobrc/TOBRC01.java)

Replaced `java.math.BigDecimal` with `double` and used the rounding code from
[Baseline](https://github.com/gunnarmorling/1brc/blob/main/src/main/java/dev/morling/onebrc/CalculateAverage_baseline.java)
to get
[measurements-rounding.txt](https://github.com/gunnarmorling/1brc/blob/main/src/test/resources/samples/measurements-rounding.txt)
to pass.

```
$ gradle run -Pversion=01 -Pjfr="-XX:StartFlightRecording=duration=120s,filename=100m.jfr" --args="../../1brc-data/measurements_100m.txt"
$ jfr view hot-methods app/100m.jfr

                                          Java Methods that Executes the Most

Method                                                                                                  Samples Percent
------------------------------------------------------------------------------------------------------- ------- -------
java.lang.String.split(char, int, boolean)                                                                   69  22.12%
java.io.BufferedReader.readLine(boolean, boolean[])                                                          67  21.47%
jdk.internal.math.FloatingDecimal.readJavaFormatString(String)                                               34  10.90%
jmat.tobrc.TOBRC01.calculate(File)                                                                           32  10.26%
sun.nio.cs.UTF_8$Decoder.decodeArrayLoop(ByteBuffer, CharBuffer)                                             26   8.33%
jdk.internal.math.FloatingDecimal$ASCIIToBinaryBuffer.doubleValue()                                          21   6.73%
jdk.internal.util.ArraysSupport.unsignedHashCode(int, byte[], int, int)                                      15   4.81%
java.lang.StringUTF16.indexOf(byte[], int, int, int)                                                         11   3.53%
sun.nio.cs.UTF_8$Decoder.xflow(Buffer, int, int, Buffer, int, int)                                           11   3.53%
java.util.HashMap.getNode(Object)                                                                             8   2.56%
java.lang.String.split(String, int, boolean)                                                                  5   1.60%
java.io.BufferedReader.readLine()                                                                             2   0.64%
java.lang.String.decodeASCII(byte[], int, char[], int, int)                                                   2   0.64%
java.io.FileInputStream.available()                                                                           2   0.64%
java.lang.String.equals(Object)                                                                               2   0.64%
java.util.Formatter$FormatSpecifier.print(...)                                                                1   0.32%
java.lang.AbstractStringBuilder.ensureCapacityInternal(int)                                                   1   0.32%
jdk.internal.math.FloatingDecimal.parseDouble(String)                                                         1   0.32%
java.util.HashMap.hash(Object)                                                                                1   0.32%
java.lang.StringUTF16.compress(byte[], int, int)                                                              1   0.32%


jfr view allocation-by-class app/100m.jfr

                              Allocation by Class

Object Type                                                 Allocation Pressure
----------------------------------------------------------- -------------------
byte[]                                                                   35.30%
java.lang.String                                                         28.59%
java.lang.Object[]                                                       15.13%
jdk.internal.math.FloatingDecimal$ASCIIToBinaryBuffer                    13.17%
java.lang.String[]                                                        7.74%
java.util.concurrent.ConcurrentHashMap$Node[]                             0.06%
java.util.ArrayList$SubList                                               0.00%
java.util.ArrayList                                                       0.00%
char[]                                                                    0.00%
java.util.ArrayList$Itr                                                   0.00%
```
