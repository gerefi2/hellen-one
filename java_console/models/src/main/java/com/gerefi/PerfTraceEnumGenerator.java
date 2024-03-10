package com.gerefi;

import com.gerefi.tracing.Entry;
import com.gerefi.tracing.EnumNames;
import com.gerefi.tracing.JsonOutput;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This tool generates C# or Java class based on enum values from C/C++ header related to gerefi own Perf Trace
 * <p>
 * This allows developers to only edit C/C++ header yet see proper names in chrome://tracing JSON file
 * <p>
 * This is not used in runtime while profiling actual firmware
 *
 * @see JsonOutput
 * @see EnumNames
 * @see EnumNames
 * @see Entry
 */
public class PerfTraceEnumGenerator {
    private static final String ENUM_START_TAG = "enum_start_tag";
    private static final String ENUM_END_TAG = "enum_end_tag";

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Input and output file names expected");
            System.exit(-1);
        }
        String inputFileName = args[0];
        String outputFileName = args[1];
        List<String> enumNames = readEnums(inputFileName);
        System.out.println("Got enums: " + enumNames);


        writeClass(outputFileName, enumNames);
    }

    private static void writeClass(String outputFileName, List<String> enumNames) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));

        writer.write("package com.gerefi.tracing;\n");
        writer.write("// generated " + new Date() + " by " + PerfTraceEnumGenerator.class + "\n");
        writer.write("public class EnumNames {\n");
        writer.write("\t" +
            "public static final String" +
            "[] TypeNames = {" + "\n");

        for (String enumValue : enumNames)
            writer.write("\t\"" + enumValue + "\",\n");


        writer.write("\t};" + "\n");
        writer.write("}" + "\n");
        writer.close();

        System.out.println("Done writing to " + outputFileName);
    }

    private static List<String> readEnums(String inputFileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputFileName));

        boolean weAreInBusiness = false;
        List<String> result = new ArrayList<>();

        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains(ENUM_START_TAG)) {
                weAreInBusiness = true;
                continue;
            }

            if (line.contains(ENUM_END_TAG)) {
                // we are done here
                break;
            }

            if (!weAreInBusiness)
                continue;

            line = line.trim().replaceAll("\\s", "");

            if (line.endsWith(",")) {
                result.add(line.substring(0, line.length() - 1));
            }


        }
        return result;
    }
}
