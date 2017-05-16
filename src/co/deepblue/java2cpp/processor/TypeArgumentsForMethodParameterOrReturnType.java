package co.deepblue.java2cpp.processor;

import co.deepblue.java2cpp.type.TypeConverter;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

/**
 * Created by levin on 17-5-8.
 */
public class TypeArgumentsForMethodParameterOrReturnType {
    public static String parseTypeArgument(Type argument) {
        if (argument instanceof ClassOrInterfaceType) { // EnumDeclaration is also ClassOrInterfaceType
            ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) argument;
            String scopeStr = "";
            if (classOrInterfaceType.getScope().isPresent()) {
                scopeStr = TypeArguments.parseTypeArgument(classOrInterfaceType.getScope().get()) + "::";
            }

            String argumentStr = "";
            if (classOrInterfaceType.getTypeArguments().isPresent())
                argumentStr = TypeArguments.assembleTypeArguments(classOrInterfaceType.getTypeArguments().get());

            String typeName = classOrInterfaceType.getNameAsString();
            String referenceModifier = "";
            String pointerModifier = "";
            String constModifier = "";
            if (TypeConverter.getInstance().isContainerType(typeName)) {
                referenceModifier = "&";
                constModifier = "const ";
            } else if (!TypeConverter.getInstance().canPassValueAsArgument(typeName)) {
                // might be enums
                pointerModifier = "*";
            }

            return constModifier + scopeStr + classOrInterfaceType.getName() + argumentStr + referenceModifier + pointerModifier;
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
}
