package co.deepblue.java2cpp.processor;

import co.deepblue.java2cpp.util.StringConstants;
import com.github.javaparser.ast.body.EnumDeclaration;

import java.io.Writer;

/**
 * Created by levin on 17-5-8.
 */
public class SimpleEnumCreator {
    public static void create(EnumDeclaration enumDeclaration, Writer headerWriter, String tabSpace) throws Exception {
        if (enumDeclaration.getMembers().size() <= 0) {
            headerWriter.write(tabSpace + "enum " + enumDeclaration.getNameAsString());
            headerWriter.write("\n" + tabSpace + "{\n");
            headerWriter.write(tabSpace + StringConstants.TAB + enumDeclaration.getEntry(0));
            for (int i=1; i<enumDeclaration.getEntries().size(); i++) {
                headerWriter.write(",\n" + tabSpace + StringConstants.TAB + enumDeclaration.getEntry(i));
            }
            headerWriter.write("\n" + tabSpace + "};");
        }
    }
}
