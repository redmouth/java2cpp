package co.deepblue.java2cpp.processor;

import co.deepblue.java2cpp.util.StringConstants;

import java.io.Writer;

/**
 * Created by levin on 17-5-11.
 */
public class Constructor {
    public static void addDefaultConstructor(String name, String scope, Writer headerWriter, Writer implWriter, String tabSpace) throws Exception {
        headerWriter.write(tabSpace + StringConstants.TAB + name + "();\n");
        implWriter.write("\n\n");
        implWriter.write(scope + name + "()\n{\n");
        implWriter.write("}\n");
    }
}
