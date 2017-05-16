package co.deepblue.java2cpp.type;

import co.deepblue.java2cpp.processor.AstNodeHelper;
import co.deepblue.java2cpp.processor.TypeArguments;
import co.deepblue.java2cpp.symbol.SymbolTreeParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

/**
 * Created by levin on 17-5-11.
 */
public class TypeInferencer {
    public static String inferenceTypeForMethodExpression(Type type) {
        if (type instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) type;
            String scopeStr = "";
            if (classOrInterfaceType.getScope().isPresent()) {
                scopeStr = inferenceTypeForMethodExpression(classOrInterfaceType.getScope().get()) + "::";
            }

            String argumentStr = "";
            if (classOrInterfaceType.getTypeArguments().isPresent())
                argumentStr = assembleTypeArguments(classOrInterfaceType.getTypeArguments().get());
            return scopeStr + classOrInterfaceType.getName() + argumentStr + "*";
        } else if (type instanceof ArrayType) {
            CompilationUnit cu = AstNodeHelper.getCompilationUnit(type);
            SymbolTreeParser.getInstance().addPredefinedIncludeToUnit(cu, "Array");
            String componentName = TypeArguments.parseTypeArgument(((ArrayType) type).getComponentType());
            return "Array<" + componentName + ">";
            //return inferenceTypeForMethodExpression(((ArrayType) type).getComponentType()) + "*";
        }
        return TypeConverter.getInstance().mapJava2CppType(type.toString());
    }


    public static  String assembleTypeArguments(NodeList<Type> typeArguments) {
        if (typeArguments.size() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("<");
            builder.append(inferenceTypeForMethodExpression(typeArguments.get(0)));

            for (int i=1; i<typeArguments.size(); i++) {
                builder.append(", ");
                builder.append(inferenceTypeForMethodExpression(typeArguments.get(i)));
            }

            builder.append(">");
            return builder.toString();
        }
        return "";
    }


    public static String inferExpressionType(Expression expression) {
        //todo
        /*
        1 Binary expression: 4 + 1, -2 + 1, x + y, 1 + x, x + 4
        2 Method call: Math.square(x), a.length, a.size,

        int l = 4L; //wrong
        int l = 4;  //wrong
        long l = 4L; //correct
        long l = 4; //correct
         */
        return expression.toString();
    }

    int jint = 0;
}
