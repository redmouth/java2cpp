package co.deepblue.java2cpp.processor;

import co.deepblue.java2cpp.type.TypeConverter;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

/**
 * Created by levin on 17-5-8.
 */
public class TypeArguments {
    public static String parseTypeArgument(Type argument) {
        if (argument instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) argument;
            String scopeStr = "";
            if (classOrInterfaceType.getScope().isPresent()) {
                scopeStr = parseTypeArgument(classOrInterfaceType.getScope().get()) + "::";
            }

            String argumentStr = "";
            if (classOrInterfaceType.getTypeArguments().isPresent())
                argumentStr = assembleTypeArguments(classOrInterfaceType.getTypeArguments().get());
            return scopeStr + classOrInterfaceType.getName() + argumentStr;
        } else if (argument instanceof ArrayType) {
            return parseTypeArgument(((ArrayType) argument).getComponentType()) + "*";
        }
        return TypeConverter.getInstance().mapJava2CppType(argument.toString());
    }

    public static  String assembleTypeArguments(NodeList<Type> typeArguments) {
        if (typeArguments.size() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("<");
            builder.append(parseTypeArgument(typeArguments.get(0)));

            for (int i=1; i<typeArguments.size(); i++) {
                builder.append(", ");
                builder.append(parseTypeArgument(typeArguments.get(i)));
            }

            builder.append(">");
            return builder.toString();
        }
        return "";
    }

    int jint = 0;
}
