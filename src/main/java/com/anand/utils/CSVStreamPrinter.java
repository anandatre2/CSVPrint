package com.anand.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import java.util.Collections;
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
            System.err.println("The file path you have supplied is empty.Please supply a correct file path");
            return;
        }

        Path path = Paths.get(this.filePath);

        try (BufferedReader br= Files.newBufferedReader(path)) {
            //TODO - use logger to check the time-spent
            //long timeA = System.currentTimeMillis();
            executeReader(delimiter, br)
                    .forEach(System.out::println);
            //long timeB = System.currentTimeMillis();
            //System.out.println("Elapsed time: " + (timeB - timeA));
        }
        catch (IOException e) {
            System.err.println("It seems that the file does not exist at the specified file path. Please check the file and/or the file path");
        }
        catch(Exception e) {
            System.out.println("An unknown error has occurred. Please contact your System Administrator");
            e.printStackTrace();
        }
    }


    //static method to carry out the necessary execution
    public static Stream<Map<String, Object>> executeReader(String delimiter, BufferedReader br) throws IOException {
        String headerLine = br.readLine();
        if (headerLine == null) {
            return Stream.empty(); // Return an empty stream if header line is null
        }

        List<String> fieldNames = Arrays.asList(headerLine.split(delimiter));

        return br.lines()
                .parallel()
                .map(line -> parseCSVLine(line, delimiter, fieldNames))
                .onClose(() -> closeBufferedReader(br));
    }

    // static method to parse a CSV line into a map of field names and values
    public static Map<String, Object> parseCSVLine(String line, String delimiter, List<String> fieldNames)  {
        try {
            //the following code is responsible for handling commma inside both the double quotes and single quotes.

            //define the pattern to capture both double quotes and single quotes.
            Pattern pattern = Pattern.compile("[\"']([^\"']*)[\"']");
            Matcher matcher = pattern.matcher(line);
            //the following line uses the string #comma94befe2f# to replace comma within the quotes since our delimiter is also comma
            //the string #comma94befe2f# will be replaced by comma after parsing the value at the end
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
        catch (IndexOutOfBoundsException e) {
            System.err.println("Error parsing CSV line: " + e.getMessage());
            //TODO - use logger to spit out the error
            e.printStackTrace();
        }
        // Return a default value if an exception occurs
        return Collections.emptyMap();
    }

    //static method to parse a String value
    public static Object parseValue(String value) {
        if (value == null || value.isEmpty()) {
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

    private static void closeBufferedReader(BufferedReader br) {
        try {
            br.close();
        } catch (IOException e) {
            System.err.println("Error closing BufferedReader: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

