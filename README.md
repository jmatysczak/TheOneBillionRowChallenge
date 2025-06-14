### The One Billion Row Challenge

Some fun coding using: https://github.com/gunnarmorling/1brc

```
# To run tests for a specific version:
$ gradle run -Pversion=00

# To process a file:
$ gradle run -Pversion=00 --args="src/main/resources/measurements_10m.txt"
```

#### Results

Times in seconds.

| Version | 10m   | 100m  | 1b    |
| ------- | ----- | ----- | ----- |
| 00      |  2.38 | 20.60 |       |


#### TOBRC00

A simple/basic/straightforward implementation.
