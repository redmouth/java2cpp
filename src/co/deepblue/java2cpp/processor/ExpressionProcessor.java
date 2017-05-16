package co.deepblue.java2cpp.processor;

import co.deepblue.java2cpp.statement.InitializerParser;
import co.deepblue.java2cpp.symbol.EnumSymbolType;
import co.deepblue.java2cpp.symbol.SymbolNode;
import co.deepblue.java2cpp.symbol.SymbolTreeParser;
import co.deepblue.java2cpp.type.TypeConverter;
import co.deepblue.java2cpp.type.TypeInferencer;
import co.deepblue.java2cpp.util.StringConstants;
import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;

import java.util.Optional;

/**
 * Created by levin on 17-5-8.
 */
public class ExpressionProcessor {

    public static String assembleArguments(NodeList<Expression> arguments) {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        if (arguments.size() > 0) {
            //builder.append(arguments.get(0).toString());
            builder.append(parseInitializerExpressionToCpp(arguments.get(0)));

            for (int i = 1; i < arguments.size(); i++) {
                builder.append(", ");
                //builder.append(arguments.get(i).toString());
                builder.append(parseInitializerExpressionToCpp(arguments.get(i)));
            }
        }
        builder.append(")");
        return builder.toString();
    }

    public static String parseInitializerExpressionToCpp(Expression expression) {
        String exp = "";
        if (expression instanceof VariableDeclarationExpr) {
            StringBuilder builder = new StringBuilder();
            NodeList<VariableDeclarator> variableDeclarators = ((VariableDeclarationExpr) expression).getVariables();
            for (VariableDeclarator variable : variableDeclarators) {
                InitializerParser.addVariableDeclaratorToSymbolTree(variable);

                Type type = variable.getType();
                String typeStr = TypeInferencer.inferenceTypeForMethodExpression(type) + StringConstants.Space;
                String name = variable.getNameAsString();

                if (type instanceof ClassOrInterfaceType) {
                    CompilationUnit cu = AstNodeHelper.getCompilationUnit(type);
                    if (!AstNodeHelper.definedInCurrentCompilationUnit((ClassOrInterfaceType) type)) {
                        SymbolTreeParser.getInstance().addIncludeForType(cu, ((ClassOrInterfaceType) type).getNameAsString());
                    }
                }


                Optional<Expression> initializer = variable.getInitializer();
                String initStr = "";
                if (initializer.isPresent()) {
                    Expression initExpr = initializer.get();
                    if (initExpr instanceof ArrayCreationExpr) {
                        initStr = processArrayCreationExpr((ArrayCreationExpr) initExpr, name, true);
                    } else
                        initStr = " = " + parseInitializerExpressionToCpp(initializer.get());
                }
                builder.append(String.valueOf(typeStr + name + initStr));
            }
            exp = builder.toString();
        } else if (expression instanceof FieldAccessExpr) {
            String accessOperator = "->";
            FieldAccessExpr accessExpr = (FieldAccessExpr) expression;
            Expression scope = accessExpr.getScope().get();
            String scopeStr = scope.toString();
            if (scope instanceof NameExpr) { // for array type, arr.length
                EnumSymbolType type = AstNodeHelper.getSymbolType((NameExpr) scope);
                if (SymbolNode.isClassOrEnum(type))
                    accessOperator = "::";
                else if (SymbolNode.isVariable(type)) {
                    accessOperator = "->";
                    boolean isArray = AstNodeHelper.isArrayType((NameExpr) scope);
                    if (isArray && accessExpr.getNameAsString().equals("length")) {
                        accessOperator = ".";
                    }
                }
            } else if (scope instanceof ThisExpr) {
                scopeStr = "this";
                accessOperator = "->";
            } else if (scope instanceof ArrayAccessExpr) {
                accessOperator = ".";
                scopeStr = ((ArrayAccessExpr) scope).getName().toString() + "[" +
                            parseInitializerExpressionToCpp(((ArrayAccessExpr) scope).getIndex()) + "]";
            } else {
                scopeStr = parseInitializerExpressionToCpp(scope);
            }

            return scopeStr + accessOperator + accessExpr.getNameAsString();
        } else if (expression instanceof MethodCallExpr) {
            String scopeStr = "";
            boolean isCout = false;
            String newLine = "";
            String expressionStr = (expression).toString().trim();
            if (expressionStr.startsWith("System.out.print")) {
                isCout = true;
                if (expressionStr.startsWith("System.out.println")) {
                    newLine = "<<endl";
                }
            } else {
                if (((MethodCallExpr) expression).getScope().isPresent()) {
                    String accessOperator = "->";
                    Expression sc = ((MethodCallExpr) expression).getScope().get();
                    if (sc instanceof EnclosedExpr) {
                        Expression inner = ((EnclosedExpr) sc).getInner().get();
                        if (inner instanceof ObjectCreationExpr) {
                            accessOperator = "->";
                        }
                    } else if (sc instanceof NameExpr) { // might be static or non-static method
                        EnumSymbolType type = AstNodeHelper.getSymbolType((NameExpr) sc);
                        if (SymbolNode.isVariable(type))
                            accessOperator = "->";
                        else //if (SymbolNode.isClassOrEnum(type))
                            accessOperator = "::"; //static member access
                        //else if (type == EnumSymbolType.SYMBOL_TYPE_ERROR) {
                        //}
                    }
                    scopeStr = parseInitializerExpressionToCpp(sc) + accessOperator;
                }
            }

            String argumentStr = assembleArguments(((MethodCallExpr) expression).getArguments());
            if (isCout) {
                String fileName = AstNodeHelper.getFileName(expression);
                SymbolTreeParser.getInstance().addInclude(fileName, "<iostream>");
                SymbolTreeParser.getInstance().addNamespace(fileName, "std");
                if (((MethodCallExpr) expression).getArguments().size() <= 0)
                    argumentStr = "";
                else
                    argumentStr = "<<" + argumentStr;
                exp = "cout" + argumentStr + newLine;
            } else {
                exp = scopeStr + ((MethodCallExpr) expression).getName() + argumentStr;
            }
        } else if (expression instanceof AssignExpr) {
            AssignExpr assignExpr = (AssignExpr) expression;
            Expression target = assignExpr.getTarget();
            Expression value = assignExpr.getValue();
            String operator = assignExpr.getOperator().asString();
            String valueStr = parseInitializerExpressionToCpp(value);
            String targetStr = parseInitializerExpressionToCpp(target);
            String space = StringConstants.Space;

            if (value instanceof ObjectCreationExpr) {
                if (target instanceof ArrayAccessExpr) {
                    valueStr = valueStr.substring("new".length());
                }
            } else if (value instanceof ArrayCreationExpr) {
                targetStr = processArrayCreationExpr((ArrayCreationExpr)value, targetStr, false);
                operator = "";
                valueStr = "";
                space = "";
            }
            exp = targetStr + space + operator +
                    space + valueStr;
        } else if (expression instanceof LiteralExpr) {
            if (expression instanceof NullLiteralExpr) {
                exp = "NULL";
            } else
                exp = ((LiteralExpr) expression).toString();
        } else if (expression instanceof ArrayInitializerExpr) {
            exp = ((ArrayInitializerExpr) expression).toString();
        } else if (expression instanceof BinaryExpr) {
            Expression leftExpr =((BinaryExpr) expression).getLeft();
            String left = parseInitializerExpressionToCpp(leftExpr);
            String right = parseInitializerExpressionToCpp(((BinaryExpr) expression).getRight());
            String operator = (((BinaryExpr) expression).getOperator()).asString();
            BinaryExpr.Operator optr = ((BinaryExpr) expression).getOperator();
            if (optr.name().equals("UNSIGNED_RIGHT_SHIFT")) {
                operator = ">>";
                if (leftExpr instanceof NameExpr) {
                    Node node = AstNodeHelper.getAstTypeNode((NameExpr) leftExpr);
                    if (node != null) {
                        if (node instanceof VariableDeclarator) {
                            String typeStr = ((PrimitiveType) ((VariableDeclarator) node).getType()).asString();
                            typeStr = TypeConverter.getInstance().toUnsignedType(typeStr);
                            left = "((" + typeStr + ")" + left + ")";
                        }
                    }
                } else {
                    left = TypeInferencer.inferExpressionType(leftExpr);
                }
            }

            exp = left + " " + operator + " " + right;
        } else if (expression instanceof ArrayCreationExpr) {
            ArrayCreationExpr aC = (ArrayCreationExpr) expression;
            NodeList<ArrayCreationLevel> levelList = aC.getLevels();
            Type elementType = aC.getElementType();
            String typeStr = TypeInferencer.inferenceTypeForMethodExpression(elementType);
            String levelStr = "";
            for (ArrayCreationLevel level : levelList) {
                if (level.getDimension().isPresent()) {
                    Expression dimExpr = level.getDimension().get();
                    levelStr = levelStr + "[" + parseInitializerExpressionToCpp(dimExpr) + "]";
                }
            }
            exp = "new " + typeStr + levelStr;
        } else if (expression instanceof ObjectCreationExpr) {
            Optional<ClassOrInterfaceType> scope = ((ObjectCreationExpr) expression).getType().getScope();
            String scopeStr = "";
            if (scope.isPresent()) {
                scopeStr = scope.get().getNameAsString() + "::";
            }
            Optional<NodeList<BodyDeclaration<?>>> anonymousBody = ((ObjectCreationExpr) expression).getAnonymousClassBody();

            String scopedType = scopeStr + ((ObjectCreationExpr) expression).getType().getName();
            if (anonymousBody.isPresent()) {
                AnonymousClassCreator.create(anonymousBody.get(), scopedType);
            }
            exp = "new " + scopedType + assembleArguments(((ObjectCreationExpr) expression).getArguments());
        } else if (expression instanceof ArrayAccessExpr) {
            ArrayAccessExpr aexpr = (ArrayAccessExpr) expression;
            exp = aexpr.getName().toString() + "[" +
                    parseInitializerExpressionToCpp(aexpr.getIndex()) + "]";
        } else {
            exp = expression.toString();
        }

        return exp;
    }


    public static String processArrayCreationExpr(ArrayCreationExpr expr, String objName, boolean isDeclaration) {
        String initStr = "";
        //int[] a = new int[] {};
        String firstLineTab = "";
        if (isDeclaration) {
            initStr = ";\n";
            firstLineTab = StringConstants.TAB;
        }
        ArrayCreationLevel level = expr.getLevels().get(0);
        if (level.getDimension().isPresent()) { // new int[20]
            Expression dimen = level.getDimension().get();
            String dimenStr = parseInitializerExpressionToCpp(dimen);
            initStr += firstLineTab + objName + ".create(" + dimenStr + ")";
        } else { //new int[] {}
            if (expr.getInitializer().isPresent()) {
                ArrayInitializerExpr arrayInitializerExpr = expr.getInitializer().get();
                NodeList<Expression> values = arrayInitializerExpr.getValues();
                String dimenStr = String.valueOf(values.size());
                initStr += firstLineTab + objName + ".create(" + dimenStr + ")";
                for (int i = 0; i < values.size(); i++) {
                    initStr += ";\n" + StringConstants.TAB + objName + ".set(" + i + ", " + parseInitializerExpressionToCpp(values.get(i)) + ")";
                }
            }
        }

        return initStr;
    }
}
