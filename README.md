### The One Billion Row Challenge

Some fun coding using: https://github.com/gunnarmorling/1brc

```
# To run tests for a specific version:
$ gradle run -Pversion=03

# To process a file:
$ gradle run -Pversion=03 --args="../../1brc-data/measurements_1b.txt"

# To run with Java Flight Recorder:
$ gradle run -Pversion=03 -Pjfr="-XX:StartFlightRecording=duration=120s,filename=1b.jfr" --args="../../1brc-data/measurements_1b.txt"
```

#### Results

Times in seconds.

| Version                                                                                                                     | Threads | 10m    | 100m   | 1b     |
| :------                                                                                                                     | ------: | -----: | -----: | -----: |
| [Baseline](https://github.com/gunnarmorling/1brc/blob/main/src/main/java/dev/morling/onebrc/CalculateAverage_baseline.java) |       1 |   1.77 |  13.67 | 161.40 |
| [00](/app/src/main/java/jmat/tobrc/TOBRC00.java)                                                                            |       1 |   2.81 |  24.74 | 236.02 |
| [01](/app/src/main/java/jmat/tobrc/TOBRC01.java)                                                                            |       1 |   1.93 |  15.44 | 155.97 |
| [02](/app/src/main/java/jmat/tobrc/TOBRC02.java)                                                                            |       1 |   0.69 |   7.52 |  77.26 |
| [03](/app/src/main/java/jmat/tobrc/TOBRC03.java)                                                                            |       1 |   0.59 |   6.72 |  69.90 |
| [04](/app/src/main/java/jmat/tobrc/TOBRC04.java)                                                                            |       1 |   0.78 |   8.35 |  84.64 |
| [05](/app/src/main/java/jmat/tobrc/TOBRC05.java)                                                                            |       1 |   0.62 |   6.67 |  67.69 |
| [06](/app/src/main/java/jmat/tobrc/TOBRC06.java)                                                                            |       1 |   0.56 |   4.04 |  66.66 |
| [07](/app/src/main/java/jmat/tobrc/TOBRC07.java)                                                                            |       1 |   0.58 |   4.62 |  68.77 |


#### [TOBRC00](/app/src/main/java/jmat/tobrc/TOBRC00.java)

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


#### [TOBRC01](/app/src/main/java/jmat/tobrc/TOBRC01.java)

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


$ jfr view allocation-by-class app/100m.jfr

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


#### [TOBRC02](/app/src/main/java/jmat/tobrc/TOBRC02.java)

Process the input as bytes.

```
$ gradle run -Pversion=02 -Pjfr="-XX:StartFlightRecording=duration=120s,filename=1b.jfr" --args="../../1brc-data/measurements_1b.txt"
$ jfr view hot-methods app/1b.jfr

                          Java Methods that Executes the Most

Method                                                                  Samples Percent
----------------------------------------------------------------------- ------- -------
jdk.internal.util.ArraysSupport.mismatch(byte[], int, byte[], int, int)     307  56.85%
java.util.HashMap.getNode(Object)                                           208  38.52%
jmat.tobrc.TOBRC02.calculate(File)                                           25   4.63%


$ jfr view allocation-by-class app/1b.jfr

                              Allocation by Class

Object Type                                                 Allocation Pressure
----------------------------------------------------------- -------------------
java.util.concurrent.ConcurrentHashMap$Node[]                            87.34%
java.lang.Object[]                                                        5.07%
byte[]                                                                    5.07%
int[]                                                                     2.51%
```


#### [TOBRC03](/app/src/main/java/jmat/tobrc/TOBRC03.java)

Instead of using a `java.util.HashMap` use the hash that is already being calculated and index into an array.

```
$ gradle run -Pversion=03 -Pjfr="-XX:StartFlightRecording=duration=120s,filename=1b.jfr" --args="../../1brc-data/measurements_1b.txt"
$ jfr view hot-methods app/1b.jfr

                          Java Methods that Executes the Most

Method                                                                  Samples Percent
----------------------------------------------------------------------- ------- -------
jmat.tobrc.TOBRC03.calculate(File)                                          259  76.63%
jdk.internal.util.ArraysSupport.mismatch(byte[], int, byte[], int, int)      78  23.08%
jdk.jfr.internal.periodic.JVMEventTask.execute(long, PeriodicType)            1   0.30%


$ jfr view allocation-by-class app/1b.jfr

                              Allocation by Class

Object Type                                                 Allocation Pressure
----------------------------------------------------------- -------------------
java.util.concurrent.ConcurrentHashMap$Node[]                            87.32%
byte[]                                                                    7.61%
java.lang.StringBuilder                                                   2.54%
java.util.concurrent.ConcurrentHashMap$Node                               2.54%
```


#### [TOBRC04](/app/src/main/java/jmat/tobrc/TOBRC04.java)

No performance improvements. Refectored the hash table code to see if I could make it cleaner.

```
$ gradle run -Pversion=04 -Pjfr="-XX:StartFlightRecording=duration=120s,filename=1b.jfr" --args="../../1brc-data/measurements_1b.txt"
$ jfr view hot-methods app/1b.jfr

                          Java Methods that Executes the Most

Method                                                                  Samples Percent
----------------------------------------------------------------------- ------- -------
jmat.tobrc.TOBRC04$HashMap.addOrUpdate(int, byte[], int, int, int)          335  48.06%
jmat.tobrc.TOBRC04.calculate(File)                                          234  33.57%
jdk.internal.util.ArraysSupport.mismatch(byte[], int, byte[], int, int)     126  18.08%
java.io.FileInputStream.available()                                           1   0.14%
java.nio.CharBuffer.wrap(char[], int, int)                                    1   0.14%


$ jfr view allocation-by-class app/1b.jfr

                              Allocation by Class

Object Type                                                 Allocation Pressure
----------------------------------------------------------- -------------------
java.util.concurrent.ConcurrentHashMap$Node[]                            87.33%
java.lang.Double                                                          5.06%
java.util.HashMap                                                         2.53%
java.lang.String                                                          2.53%
char[]                                                                    2.53%
```


#### [TOBRC05](/app/src/main/java/jmat/tobrc/TOBRC05.java)

Based on version 3 with a change to how the station names are compared.

```
$ gradle run -Pversion=05 -Pjfr="-XX:StartFlightRecording=duration=120s,filename=1b.jfr" --args="../../1brc-data/measurements_1b.txt"
$ jfr view hot-methods app/1b.jfr

                      Java Methods that Executes the Most

Method                                                          Samples Percent
--------------------------------------------------------------- ------- -------
jmat.tobrc.TOBRC05.calculate(File)                                  755  99.74%
java.io.FileInputStream.read(byte[], int, int)                        1   0.13%
jmat.tobrc.TOBRC05$MeasurementSummary.add(int)                        1   0.13%


$ jfr view allocation-by-class app/1b.jfr

                              Allocation by Class

Object Type                                                 Allocation Pressure
----------------------------------------------------------- -------------------
java.util.concurrent.ConcurrentHashMap$Node[]                            87.26%
java.nio.HeapCharBuffer                                                   5.07%
byte[]                                                                    5.07%
char[]                                                                    2.60%
```


#### [TOBRC06](/app/src/main/java/jmat/tobrc/TOBRC06.java)

Use a FileChannel instead of a FileInputStream to read the file. Also changed the logic for reading measurements.


#### [TOBRC07](/app/src/main/java/jmat/tobrc/TOBRC07.java)

Map the input file as a MemorySegment instead of a MappedByteBuffer.

