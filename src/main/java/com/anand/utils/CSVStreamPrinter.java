package com.anand.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class CSVStreamPrinter {

    /*
    The class CSVStreamPrinter accepts a csv file and is responsible for reading it into a Map object
    and printing out its contents.
     */

    private String filePath = "";

    //TODO - currently only comma(,) is the supported delimiter
    private String delimiter = ",";

    public CSVStreamPrinter(String filePath) {
        this.delimiter = ",";
        this.filePath = filePath;
    }

    public void printCsv() {
        if (filePath.isEmpty()) {
            System.out.println("The file path you have supplied is empty.Please supply a correct file path");
            return;
        }

        Path path = Paths.get(this.filePath);

        try (BufferedReader br= Files.newBufferedReader(path)) {
            //TODO - use logger to check the time-spent
            //long timeA = System.currentTimeMillis();
            Stream<Map<String, Object>> finalMap = executeReader(delimiter, br);
            finalMap.forEach(System.out::println);
            //long timeB = System.currentTimeMillis();
            //System.out.println("Elapsed time: " + (timeB - timeA));
        }
        catch (IOException e) {
            System.out.println("It seems that the file does not exist at the specified file path. Please check the file and/or the file path");
        }
        catch(Exception e) {
            //TODO - this exception needs to be reproduced
            System.out.println("An unknown error has occurred. Please contact your System Administrator");
            e.printStackTrace();
        }
    }


    //static method to carry out the necessary execution
    public static Stream<Map<String, Object>> executeReader(String delimiter, BufferedReader br) throws IOException, Exception {
        String headerLine = br.readLine();
        if (headerLine == null) {
            //TODO - do we want to handle it an exception or returning empty stream is ok?
            //TODO - add JUnit test
            return Stream.empty();
        }

        List<String> fieldNames = Arrays.asList(headerLine.split(delimiter));

        return br.lines()
                .parallel() // Use parallel stream to get bonus points and for potential performance boost
                .map(line -> {
                    try {
                        return parseCSVLine(line, delimiter, fieldNames);
                    } catch (Exception e) {
                        //TODO - this exception needs to be reproduced
                        throw new RuntimeException(e);
                    }
                })
                .onClose(() -> {
                    try {
                        br.close(); // Close the BufferedReader when the stream is closed
                    } catch (IOException e) {
                        //TODO - this exception needs to be reproduced
                        throw new RuntimeException(e);
                    }
                });

    }

    // static method to parse a CSV line into a map of field names and values
    public static Map<String, Object> parseCSVLine(String line, String delimiter, List<String> fieldNames) throws Exception {
        try {
            //this is to handle the comma within the string
            Pattern pattern = Pattern.compile("\"([^\"]*)\"");
            Matcher matcher = pattern.matcher(line);
            String result = matcher.replaceAll(matchResult -> matchResult.group().replace(",", "#comma94befe2f#"));

            Stream<String> fieldStream = Arrays.stream(result.split(delimiter));
            List<String> fieldValues = fieldStream.collect(Collectors.toList());

            return fieldNames.stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            fieldName -> {
                                String value = fieldValues.isEmpty() ? "" : fieldValues.remove(0).trim();
                                return parseValue(value.replace("#comma94befe2f#", ",")); // Parse the value
                            }
                    ));
        }
        catch (Exception e) {
            //TODO - this exception needs to be reproduced
            throw new RuntimeException("Failed to parse CSV line: " + line, e);
        }
    }

    //static method to parse a String value
    public static Object parseValue(String value) {
        try {
            if (value.isEmpty()) {
                //TODO - assuming returning an empty string is ok and meets the acceptance criteria
                return "";
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e1) {
                try {
                    return Double.parseDouble(value);
                } catch (NumberFormatException e2) {
                    return value;
                }
            }
        }
        catch (Exception e) {
            //TODO - this exception needs to be reproduced
            throw new RuntimeException("Failed to parse value: " + value, e);
        }
    }
}

