package co.deepblue.java2cpp.processor;

import co.deepblue.java2cpp.type.TypeInferencer;
import co.deepblue.java2cpp.type.TypeConverter;
import co.deepblue.java2cpp.util.StringConstants;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Optional;

import static co.deepblue.java2cpp.processor.ExpressionProcessor.parseInitializerExpressionToCpp;

/**
 * Created by levin on 17-5-8.
 */
public class FieldProcessor {

    public static void processField(FieldDeclaration fieldDeclaration, Writer writer, String tabSpace) throws Exception {
        Type type = null;
        ArrayList<String> fields = new ArrayList<>();

        ModifierProcessor modifier = new ModifierProcessor();
        modifier.processModifiers(fieldDeclaration.getModifiers());

        boolean isContainerType = false;
        type = fieldDeclaration.getVariable(0).getType();
        String typeName = TypeInferencer.inferenceTypeForMethodExpression(type);
        String cppType = "";
        if (type instanceof ClassOrInterfaceType) {
            isContainerType = TypeConverter.getInstance().isContainerType(((ClassOrInterfaceType)type).getName().toString());
        }

        if (!isContainerType && !TypeConverter.getInstance().isBasicTypes(type.toString()) && !(type instanceof ArrayType)) {
            cppType = "SharedPtr<" + typeName + ">";
        } else
            cppType = typeName;

        boolean hasInitializer = false;
        for (VariableDeclarator vd : fieldDeclaration.getVariables()) {
            SimpleName name = vd.getName();
            Optional<Expression> initializer = vd.getInitializer(); // Optional.empty for Nothing
            String variable = name.toString();
            if (!isContainerType && initializer.isPresent()) {
                hasInitializer = true;
                Expression expression = initializer.get();
                if (expression instanceof ObjectCreationExpr) {
                    //if (!isContainerType)
                    cppType = type.toString() + "*";
                }
                String exp = parseInitializerExpressionToCpp(expression);

                if (!exp.isEmpty())
                    variable = variable + " = " + exp;
            }


            fields.add(variable);
        }

        if (!isContainerType && !TypeConverter.getInstance().isBasicTypes(type.toString()) && hasInitializer && !(type instanceof ArrayType)) {
            cppType = type.toString() + "*";
        }

        writer.write(tabSpace + StringConstants.TAB + modifier.staticMod + cppType + " ");
        writer.write(fields.get(0));
        for (int i = 1; i < fields.size(); i++) {
            writer.write(", " + fields.get(i));
        }
        writer.write(";\n");
    }
}
