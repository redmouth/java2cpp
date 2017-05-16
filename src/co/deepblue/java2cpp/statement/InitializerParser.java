package co.deepblue.java2cpp.statement;

import co.deepblue.java2cpp.processor.TypeArguments;
import co.deepblue.java2cpp.symbol.EnumSymbolType;
import co.deepblue.java2cpp.type.TypeConverter;
import co.deepblue.java2cpp.processor.ExpressionProcessor;
import co.deepblue.java2cpp.symbol.SymbolNode;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.Type;

import java.util.ArrayList;
import java.util.Optional;

import static co.deepblue.java2cpp.processor.ExpressionProcessor.parseInitializerExpressionToCpp;

/**
 * Created by levin on 17-5-10.
 */
public class InitializerParser {
    public static SymbolNode addVariableDeclaratorToSymbolTree(VariableDeclarator declarator) {
        if (declarator.getParentNode().isPresent()) {
            Node curNode = declarator.getParentNode().get();
            while (true) {
                SymbolNode symbolNode = curNode.getData(SymbolNode.dataKey);
                if (symbolNode != null) {
                    symbolNode.addChild(new SymbolNode(symbolNode, symbolNode.getPackageName(), declarator.getNameAsString(),
                            EnumSymbolType.SYMBOL_TYPE_METHOD_STATEMENT_VARIABLE, declarator, curNode, null, false));
                    return symbolNode;
                } else {
                    if (curNode.getParentNode().isPresent())
                        curNode = curNode.getParentNode().get();
                    else
                        break;
                }
            }
        }
        return null;
    }

    // parameter of catch clause...
    public static SymbolNode addClauseParameterToSymbolTree(Parameter parameter) {
        if (parameter.getParentNode().isPresent()) {
            Node curNode = parameter.getParentNode().get();
            while (true) {
                SymbolNode symbolNode = curNode.getData(SymbolNode.dataKey);
                if (symbolNode != null) {
                    symbolNode.addChild(new SymbolNode(symbolNode, symbolNode.getPackageName(), parameter.getNameAsString(),
                            EnumSymbolType.SYMBOL_TYPE_METHOD_CATCH_CLAUSE_PARAMETER, parameter, curNode, null, false));
                    return symbolNode;
                } else {
                    if (curNode.getParentNode().isPresent())
                        curNode = curNode.getParentNode().get();
                    else
                        break;
                }
            }
        }
        return null;
    }

    public static String parseDeclarator(NodeList<VariableDeclarator> variableDeclarators) {
        ArrayList<String> variables = new ArrayList<>();

        Type type = variableDeclarators.get(0).getType();
        String cppType = TypeConverter.getInstance().mapJava2CppType(type.toString());

        for (VariableDeclarator vd : variableDeclarators) {
            SimpleName name = vd.getName();
            Optional<Expression> initializer = vd.getInitializer(); // Optional.empty for Nothing
            String variable = name.toString();
            if (initializer.isPresent()) {
                Expression expression = initializer.get();
                String exp = parseInitializerExpressionToCpp(expression);
                variable = variable + " = " + exp;
            }

            variables.add(variable);

            addVariableDeclaratorToSymbolTree(vd);
        }

        StringBuilder builder = new StringBuilder();
        builder.append(String.valueOf(cppType + " "));
        builder.append(variables.get(0));
        for (int i = 1; i < variables.size(); i++) {
            builder.append(String.valueOf(", " + variables.get(i)));
        }

        return builder.toString();
    }

    public static String parseStatementExpression(NodeList<Expression> expressions) {
        if (expressions.size() > 0) {
            Expression expression = expressions.get(0);
            if (expression instanceof VariableDeclarationExpr) {
                return parseDeclarator(((VariableDeclarationExpr) expression).getVariables());
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append(ExpressionProcessor.parseInitializerExpressionToCpp(expression));
                for (int i = 1; i < expressions.size(); i++) {
                    builder.append(", ");
                    builder.append(ExpressionProcessor.parseInitializerExpressionToCpp(expressions.get(i)));
                }
                return builder.toString();
            }
        }
        return "";
    }

    public static String parseVariable(VariableDeclarator variableDeclarator) {
        String typeName = TypeArguments.parseTypeArgument(variableDeclarator.getType());
        String name = variableDeclarator.getNameAsString();

        addVariableDeclaratorToSymbolTree(variableDeclarator);
        return typeName + " " + name;
    }
}
