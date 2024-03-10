package com.gerefi;

import com.gerefi.enum_reader.Value;
import com.gerefi.util.SystemOut;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * produces java enum class from an enum defined in a C/C++ header
 */
public class ToJavaEnum {
    static private boolean enumWithValues = false;

    public static void main(String[] args) throws IOException {
        InvokeReader invokeReader = new InvokeReader(args).invoke();
        String outputPath = invokeReader.getOutputPath();

        EnumsReader enumsReader = new EnumsReader();

        enumWithValues = "true".equalsIgnoreCase(System.getProperty("enum_with_values"));
        System.out.println("enumWithValues=" + enumWithValues);

        VariableRegistry registry = new VariableRegistry();
        for (String fileName : invokeReader.getDefinitionInputFiles())
            registry.readPrependValues(fileName);

        StringBuilder sb = new StringBuilder();

        for (String inputFile : invokeReader.getInputFiles()) {
            File f = new File(invokeReader.getInputPath() + File.separator + inputFile);
            SystemOut.println("Reading enums from " + f);
            sb.append("// based on ").append(f).append("\n");

            enumsReader.read(new FileReader(f), registry, enumWithValues);
        }

        for (Map.Entry<String /*enum name*/, EnumsReader.EnumState> e : enumsReader.getEnums().entrySet()) {
            String java = generate(sb.toString(), registry, e.getKey(), e.getValue());

            String fullFileName = outputPath + File.separator + e.getKey() + ".java";
            BufferedWriter br = new BufferedWriter(new FileWriter(fullFileName));
            br.write(java);
            br.close();
        }
    }

    public static String generate(VariableRegistry registry, String key, EnumsReader.EnumState enumState) {
        return generate("", registry, key, enumState);
    }

    public static String generate(String header, VariableRegistry registry, String key, EnumsReader.EnumState enumState) {
        StringBuilder sb = new StringBuilder("package com.gerefi.enums;\n");
        sb.append(header);
        sb.append("//auto-generated by ToJavaEnum.java\n\n\n\n");
        sb.append("public enum " + key + " {\n");

        List<Value> sorted = EnumsReader.getSortedByOrder(registry, enumState.values);

        int index = 0;
        for (Value value : sorted) {
            int numericValue = value.getIntValueMaybeResolve(registry);
            if (enumWithValues) {
                sb.append("\t" + value.getName() + "(" + numericValue + "),\n");
            } else {
                if (index != numericValue)
                    throw new IllegalStateException("Got explicit ordinal " + numericValue + " instead of ordinal " + index + " in " + value);
                sb.append("\t" + value.getName() + ",\n");
            }
            index++;
        }

        if (enumWithValues) {
            sb.append("\t;\n\n\n\tprivate int value;\n\n");
            sb.append("\t" + key + "(int v) {\n\t\tvalue = v;\n\t}\n\n");
            sb.append("\tpublic int get() {\n\t\treturn value;\n\t}\n");
        }

        sb.append("}\n");
        return sb.toString();
    }
}
