package co.deepblue.java2cpp.processor;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;

/**
 * Created by levin on 17-5-8.
 */
public class MethodParameterProcessor {
    public static String parseParameter(Parameter parameter) {
        String typeStr = TypeArgumentsForMethodParameterOrReturnType.parseTypeArgument(parameter.getType());
        return typeStr + " " + parameter.getNameAsString();
    }

    public static String assembleParameters(NodeList<Parameter> parameters) {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        if (parameters.size() > 0) {
            //builder.append(arguments.get(0).toString());
            builder.append(parseParameter(parameters.get(0)));

            for (int i=1; i<parameters.size(); i++) {
                builder.append(", ");
                //builder.append(arguments.get(i).toString());
                builder.append(parseParameter(parameters.get(i)));
            }
        }
        builder.append(")");
        return builder.toString();
    }
}
