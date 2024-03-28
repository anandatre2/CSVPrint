package com.anand.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class CSVStreamPrinterTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    private CSVStreamPrinter csvStreamPrinter;

    @BeforeEach
    public void setUpStreams() {

    }

    @Test
    public void testPrintCsvExceptionHandling() {
        // Redirect stdout to capture printed output
        System.setErr(new PrintStream(outContent));
        csvStreamPrinter = new CSVStreamPrinter(""); // Empty file path to simulate exception
        csvStreamPrinter.printCsv();
        // Verify that an error message is printed
        assertEquals("The file path you have supplied is empty.Please supply a correct file path", outContent.toString().trim());
    }

    @Test
    public void testPrintCsvIOExceptionHandling() {
        // Redirect stdout to capture printed output
        System.setErr(new PrintStream(outContent));
        csvStreamPrinter = new CSVStreamPrinter("nonexistent.csv"); // Non-existent file path to simulate IOException
        csvStreamPrinter.printCsv();
        // Verify that an error message is printed
        assertEquals("It seems that the file does not exist at the specified file path. Please check the file and/or the file path", outContent.toString().trim());
    }

    @Test
    public void testPrintCsvOtherExceptionHandling() {
        // Redirect stdout to capture printed output
        //TODO - understand the format of file to send a better message
        System.setErr(new PrintStream(outContent));
        csvStreamPrinter = new CSVStreamPrinter("invalidcsv.txt"); // Invalid file content to simulate exception
        csvStreamPrinter.printCsv();
        // Verify that an error message is printed
        assertEquals("It seems that the file does not exist at the specified file path. Please check the file and/or the file path", outContent.toString().trim());
    }

    //TODO - improve code coverage for exceptions  by adding more JUnit tests

    @Test
    public void testPrintCsv() {
        // Redirect stdout to capture printed output
        Path resourceDirectory = Paths.get("src","test","resources");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        String filePath = absolutePath + "\\cars_v8.csv";
        csvStreamPrinter = new CSVStreamPrinter(filePath);

        System.setOut(new PrintStream(outContent));
        csvStreamPrinter.printCsv();

        // Verify the printed output
        assertEquals("{YEAR=2020, MAKE=MITSUBISHI}\r\n" +
                "{YEAR=2021, MAKE=NISSAN}", outContent.toString().trim());
    }

    @Test
    public void testPrintCsv2() {
        // Redirect stdout to capture printed output

        Path resourceDirectory = Paths.get("src","test","resources");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        String filePath = absolutePath + "\\cars_v2.csv";
        csvStreamPrinter = new CSVStreamPrinter(filePath);
        System.setOut(new PrintStream(outContent));
        csvStreamPrinter.printCsv();

        String check = "{YEAR=2020, Size=\"  SUBCOMPACT\", Model=\"   i-MiEV   \", Make=\"    MITSUBISHI   \"}\r\n" +
                "{YEAR=, Size=\"COMPACT\", Model=\"MEV\", Make=}\r\n" +
                "{YEAR=, Size=SUBCOMPACT, Model=i-MiEV, Make=}\r\n" +
                "{YEAR=, Size=, Model=, Make=}\r\n" +
                "{YEAR=, Size=, Model=, Make=}\r\n" +
                "{YEAR=***, Size=!!!, Model=@@@, Make=$$$}\r\n" +
                "{YEAR=, Size=, Model=, Make=}\r\n" +
                "{YEAR=, Size=, Model=, Make=}\r\n" +
                "{YEAR=\"\", Size=\"\", Model=\"\", Make=\"\"}\r\n" +
                "{YEAR=, Size=\"  \", Model=\"      \", Make=\"       \"}\r\n" +
                "{YEAR=2020, Size=\"  SUBCOMPACT\", Model=\"   i-MiEV   \", Make='    MITSUBISHI   '}\r\n" +
                "{YEAR=2020, Size=\"  SUBCOM,PACT\", Model=\"   i-MiEV   \", Make=\"    MITSU,BISHI   \"}\r\n" +
                "{YEAR=2020, Size=\"  SUBCOM,PACT\", Model=\"   i-Mi,EV   \", Make=\"    MITSU,BISHI   \"}\r\n" +
                "{YEAR=2020, Size=\"  SUBCOM,PACT\", Model=\"   20,30   \", Make=\"    MITSU,BISHI   \"}\r\n" +
                "{YEAR=2020, Size=\"  SUBCOM,PACT\", Model=\"   20.55,30.33   \", Make=\"    MITSU,BISHI   \"}\r\n" +
                "{YEAR=44.44, Size=\"  SUBCOMPACT\", Model=\"   44.44   \", Make=\"    55.55   \"}\r\n" +
                "{YEAR=, Size=, Model=, Make=}\r\n" +
                "{YEAR=2020, Size='  SUBCOMPACT', Model=\"   i-MiEV   \", Make='   MITSUBISHI   '}\r\n"+
                "{YEAR=2020, Size=SUBCOMPACT, Model=i-MiEV, Make=MITSUBISHI}\r\n" +
                "{YEAR=2020, Size='  SUB,COMPACT', Model=\"   i-MiEV   \", Make='   MITSU,BISHI   '}";

        assertEquals(check, outContent.toString().trim());

    }

    @Test
    public void testParseCSVLine() {
        // Input data
        String line = "\"somename\",45,170.5,175.0";
        String delimiter = ",";
        String[] expectedFieldNames = {"Name", "Age", "Weight", "Height"};
        Object[] expectedFieldValues = {"\"somename\"", 45, 170.5, 175.0};


        try {
            Map<String, Object> result = CSVStreamPrinter.parseCSVLine(line, delimiter, Arrays.asList(expectedFieldNames));

            // Assert the result using streams and lambda expressions
            assertEquals(expectedFieldValues.length, result.size());
            Arrays.stream(expectedFieldNames)
                    .forEach(fieldName ->
                            assertEquals(expectedFieldValues[Arrays.asList(expectedFieldNames).indexOf(fieldName)],
                                    result.get(fieldName))
                    );
        } catch (Exception e) {
            e.printStackTrace(); // Handle exception appropriately
        }
    }


    @Test
    public void testParseCSVLine2() throws Exception {
        // Input data
        String line = "2020,\"    MITSUBISHI   \",\"   i-MiEV   \",\"  SUBCOMPACT\"";
        String delimiter = ",";
        List<String> fieldNames = Arrays.asList("YEAR","Make","Model","Size");

        // Expected output
        Map<String, Object> expected = Map.of(
                "YEAR", 2020,
                "Make", "\"    MITSUBISHI   \"",
                "Model", "\"   i-MiEV   \"",
                "Size", "\"  SUBCOMPACT\""
        );

        // Invoke the method under test
        Map<String, Object> result = CSVStreamPrinter.parseCSVLine(line, delimiter, fieldNames);

        // Assert the result

        assertEquals(fieldNames.size(), result.size());
        fieldNames.forEach(value -> assertEquals(expected.get(value), result.get(value)));
    }

    //TODO - Add elaborate JUnit tests similar to testParseCSVLine2 for each line

    @Test
    public void testParseValue_Integer() {
        String value = "123456";
        Object result = CSVStreamPrinter.parseValue(value);
        assertEquals(Integer.parseInt(value), result);
    }

    @Test
    public void testParseValue_Double() {
        String value = "123.45678";
        Object result = CSVStreamPrinter.parseValue(value);
        assertEquals(Double.parseDouble(value), result);
    }

    @Test
    public void testParseValue_String() {
        String value = "Check string";
        Object result = CSVStreamPrinter.parseValue(value);
        assertEquals(value, result);
    }

    @Test
    public void testParseValue_EmptyString() {
        String value = "";
        Object result = CSVStreamPrinter.parseValue(value);
        assertEquals("", result);
    }

    @Test
    public void testExecuteReader() throws IOException {
        String csvData = "\"Name\",\"Age\",\"Weight\",\"Height\"\n" +
                "\"Somename\",45,70.5,175.0\n" +
                "\"Othername\",50,65.2,160.5\n";

        try (BufferedReader br = new BufferedReader(new StringReader(csvData))) {
            List<String> expectedFieldNames = Arrays.asList("Name", "Age", "Weight", "Height");

            String delimiter = ",";
            try {
                List<Map<String, Object>> result = CSVStreamPrinter.executeReader(delimiter,br).collect(Collectors.toList());

                assertEquals(2, result.size());

                // Verify first record
                Map<String, Object> firstRecord = result.get(0);
                assertEquals(expectedFieldNames.size(), firstRecord.size());
                Object obj1 = firstRecord.get("\"Name\"");
                assertEquals("\"Somename\"", obj1.toString());
                assertEquals(45, firstRecord.get("\"Age\""));
                assertEquals(70.5, firstRecord.get("\"Weight\""));
                assertEquals(175.0, firstRecord.get("\"Height\""));

                // Verify second record
                Map<String, Object> secondRecord = result.get(1);
                assertEquals(expectedFieldNames.size(), secondRecord.size());
                assertEquals("\"Othername\"", secondRecord.get("\"Name\""));
                assertEquals(50, secondRecord.get("\"Age\""));
                assertEquals(65.2, secondRecord.get("\"Weight\""));
                assertEquals(160.5, secondRecord.get("\"Height\""));

            }
            catch (Exception e) {

            }
        }
    }

    @Test
    public void testPrintCsvMissingEverything() {
        Path resourceDirectory = Paths.get("src","test","resources");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        String filePath = absolutePath + "\\cars_v4.csv";
        csvStreamPrinter = new CSVStreamPrinter(filePath);

        System.setOut(new PrintStream(outContent));
        csvStreamPrinter.printCsv();

        // Verify the printed output
        assertEquals("", outContent.toString().trim());
    }


    public void testPrintCsvHMEQ() {
        long timeA = System.currentTimeMillis();
        Path resourceDirectory = Paths.get("src","test","resources");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        String filePath = absolutePath + "\\HMEQ.csv";
        csvStreamPrinter = new CSVStreamPrinter(filePath);
        csvStreamPrinter.printCsv();
        long timeB = System.currentTimeMillis();

        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Elapsed time: " + (timeB - timeA));
    }

    public void testPrintCsvPerformance() {
        long timeA = System.currentTimeMillis();
        Path resourceDirectory = Paths.get("src","test","resources");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        String filePath = absolutePath + "\\one_m_records.csv";
        csvStreamPrinter = new CSVStreamPrinter(filePath);
        csvStreamPrinter.printCsv();
        long timeB = System.currentTimeMillis();

        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Elapsed time: " + (timeB - timeA));
    }

}
