package jmat.tobrc;


import java.io.File;


public record TestCase(String name, File inputFile, File expectedFile) {
}

