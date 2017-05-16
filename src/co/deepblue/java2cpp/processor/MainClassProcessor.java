package co.deepblue.java2cpp.processor;

import co.deepblue.java2cpp.symbol.SymbolTreeParser;
import co.deepblue.java2cpp.util.StringConstants;
import co.deepblue.java2cpp.statement.StatementTranslator;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.Type;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by levin on 17-5-8.
 */
// compile unit processor
public class MainClassProcessor {

    public static void process(ClassOrInterfaceDeclaration declaration, Writer implWriter, String tabSpace) throws Exception {
        NodeList<BodyDeclaration<?>> members = declaration.getMembers();
        List<FieldDeclaration> fieldList = new ArrayList<>();
        List<MethodDeclaration> methodList = new ArrayList<>();
        //List<ConstructorDeclaration> constructorList = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> classOrInterfaceList = new ArrayList<>();
        MethodDeclaration mainMethod = null;

        for (BodyDeclaration<?> member : members) {
            if (member instanceof MethodDeclaration) {
                if (member == SymbolTreeParser.getInstance().getMainMethod())
                    mainMethod = (MethodDeclaration) member;
                else
                    methodList.add((MethodDeclaration) member);
                ///processMethod(method, headerWriter);
            } else if (member instanceof FieldDeclaration) {
                fieldList.add((FieldDeclaration) member);
                //processField(field, headerWriter);
            } else if (member instanceof ConstructorDeclaration) {
                //constructorList.add((ConstructorDeclaration) member);
                //processConstructor((ConstructorDeclaration)member, headerWriter);
            } else if (member instanceof ClassOrInterfaceDeclaration) {
                classOrInterfaceList.add((ClassOrInterfaceDeclaration) member);
            }
        }

        if (classOrInterfaceList.size() > 0) {
            for (ClassOrInterfaceDeclaration classOrInterface : classOrInterfaceList) {
                process(classOrInterface, implWriter, tabSpace + StringConstants.TAB);
            }
        }


        if (fieldList.size() > 0) {
            for (FieldDeclaration field : fieldList) {
                FieldProcessor.processField(field, implWriter, tabSpace);
            }
        }

        /*
        if (constructorList.size() > 0) {
            for (ConstructorDeclaration constructor : constructorList) {
                MethodProcessor.processConstructor(typeParameterHashMap, constructor, headerWriter, implWriter, tabSpace);
            }
        }
        */


        if (methodList.size() > 0) {
            for (MethodDeclaration method : methodList) {
                processMethod(method, implWriter);
            }
        }

        if (mainMethod != null)
            processMethod(mainMethod, implWriter);
    }




    public static void processMethod(MethodDeclaration method, Writer implWriter) throws Exception {
        ModifierProcessor modifier = new ModifierProcessor();
        modifier.processModifiers(method.getModifiers());
        String name = method.getNameAsString();
        boolean isMainMethod = (method == SymbolTreeParser.getInstance().getMainMethod());
        NodeList<Parameter> parameters = method.getParameters();
        Type returnType = method.getType();
        String returnStr;
        String methodParameter;
        if (isMainMethod) {
            returnStr = "int";
            methodParameter = "(int argc, char**argv)";
        } else {
            returnStr = TypeArgumentsForMethodParameterOrReturnType.parseTypeArgument(returnType);
            methodParameter = MethodParameterProcessor.assembleParameters(parameters);
        }


        implWriter.write("\n\n");

        if (name.equals("getCoordinateInfo")) {
            System.out.println("debug method..");
        }


        implWriter.write(returnStr + StringConstants.Space + name + methodParameter + "\n{\n");
        Optional<BlockStmt> blockStmt = method.getBody();
        if (blockStmt.isPresent()) {
            BlockStmt stmt = blockStmt.get();
            NodeList<com.github.javaparser.ast.stmt.Statement> statements = stmt.getStatements();
            for (com.github.javaparser.ast.stmt.Statement statement : statements) {
                String statementStr = "";
                if (statement instanceof ExpressionStmt) {
                    statementStr = StatementTranslator.translateExpressionStmt((ExpressionStmt)statement);
                } else if (statement instanceof IfStmt) {
                    statementStr = StatementTranslator.translateIfStmt((IfStmt)statement);
                } else if (statement instanceof ForStmt) {
                    statementStr = StatementTranslator.translateForStmt((ForStmt)statement);
                } else if (statement instanceof ForeachStmt) {
                    statementStr = StatementTranslator.translateForeachStmt((ForeachStmt)statement);
                } else if (statement instanceof WhileStmt) {
                    statementStr = StatementTranslator.translateWhileStmt((WhileStmt)statement);
                } else if (statement instanceof SwitchStmt) {
                    statementStr = StatementTranslator.translateSwitchStmt((SwitchStmt)statement);
                } else if (statement instanceof TryStmt) {
                    statementStr = StatementTranslator.translateTryStmt((TryStmt)statement);
                } else if (statement instanceof ThrowStmt) {
                    statementStr = StatementTranslator.translateThrowStmt((ThrowStmt)statement);
                } else if (statement instanceof LabeledStmt) {
                    statementStr = StatementTranslator.translateLabeledStmt((LabeledStmt)statement);
                } else if (statement instanceof ReturnStmt) {
                    statementStr = StatementTranslator.translateReturnStmt((ReturnStmt)statement);
                } else if (statement instanceof SynchronizedStmt) {
                    statementStr = StatementTranslator.translateSynchronizedStmt((SynchronizedStmt)statement);
                }

                implWriter.write(statementStr + "\n");
            }
        }

        if (isMainMethod)
            implWriter.write("\n" + StringConstants.TAB + "return 0;\n");

        implWriter.write("}\n");
    }
}
