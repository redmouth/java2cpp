package co.deepblue.java2cpp.processor;

import co.deepblue.java2cpp.statement.StatementTranslator;
import co.deepblue.java2cpp.symbol.SymbolTreeParser;
import co.deepblue.java2cpp.util.StringConstants;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Created by levin on 17-5-8.
 */
public class MethodProcessor {
    public static void process(MethodDeclaration method, Writer headerWriter, Writer implWriter, String tabSpace) throws Exception {
        ModifierProcessor modifier = new ModifierProcessor();
        modifier.processModifiers(method.getModifiers());
        String name = method.getNameAsString();
        if (name.equals("main") && modifier.isStatic && modifier.isPublic) {
            //SymbolTreeParser.getInstance().setMainMethod(method);
            return;
        }

        NodeList<Parameter> parameters = method.getParameters();
        NodeList<ReferenceType> exceptions = method.getThrownExceptions();
        Type returnType = method.getType();
        String returnStr = TypeArgumentsForMethodParameterOrReturnType.parseTypeArgument(returnType);
        String methodParameter = MethodParameterProcessor.assembleParameters(parameters);
        headerWriter.write(tabSpace + StringConstants.TAB + modifier.staticMod + returnStr + " " + name + methodParameter + ";\n");


        implWriter.write("\n\n");
        /*
        example: public <V> ListenableFuture<V> addScheduledTask(Callable<V> callableToSchedule);
        parameters = {Callable<V> callableToSchedule}
        typeParameters = {V}
         */
        NodeList<TypeParameter> typeParameters = method.getTypeParameters();
        if (typeParameters.size() > 0) {
            StringBuilder templatePrefix = new StringBuilder("template <");
            templatePrefix.append("typename ");
            templatePrefix.append(typeParameters.get(0));
            for (int i=1; i<typeParameters.size(); i++) {
                templatePrefix.append(", typename ");
                templatePrefix.append(typeParameters.get(i));
            }
            templatePrefix.append("> ");
            implWriter.write(templatePrefix.toString());
        }

        if (name.equals("getCoordinateInfo")) {
            System.out.println("debug method..");
        }



        String scope = AstNodeHelper.getScope(method);

        String returnScope = "";
        if (returnType instanceof ClassOrInterfaceType) {
            CompilationUnit cu = AstNodeHelper.getCompilationUnit(method);
            returnScope  =  SymbolTreeParser.getInstance().getTypeScope(((ClassOrInterfaceType) returnType).getNameAsString(), cu);
        }

        implWriter.write(returnScope + returnStr + StringConstants.Space + scope + name + methodParameter + "\n{\n");
        Optional<BlockStmt> blockStmt = method.getBody();
        if (blockStmt.isPresent()) {
            processBlockStmt(implWriter, blockStmt.get());
        }
        implWriter.write("}\n");
    }


    public static void processConstructor(HashMap<String, TypeParameter> classTpMap, ConstructorDeclaration constructor, Writer headerWriter, Writer implWriter, String tabSpace) throws Exception {
        ModifierProcessor modifier = new ModifierProcessor();
        modifier.processModifiers(constructor.getModifiers());

        String name = constructor.getNameAsString();
        NodeList<Parameter> parameters = constructor.getParameters();
        NodeList<ReferenceType> exceptions = constructor.getThrownExceptions();
        NodeList<TypeParameter> typeParameters = constructor.getTypeParameters();

        if (name.equals("MixPredicate")) {
            System.out.println("Debug template Constructor.");
        }

        String methodParameterStr = MethodParameterProcessor.assembleParameters(parameters);
        List<String> tpList = null;
        if (classTpMap != null && classTpMap.size() > 0) {
            tpList = new ArrayList<>();
            for (Parameter parameter : parameters) {
                Type type = parameter.getType();
                if (type instanceof ClassOrInterfaceType) {
                    String typeName = ((ClassOrInterfaceType)type).getNameAsString();
                    if (classTpMap.containsKey(typeName) && !tpList.contains(typeName)) {
                        tpList.add(typeName);
                    }
                }
            }
        }

        String scope = AstNodeHelper.getScope(constructor);
        if (tpList != null && tpList.size() > 0) {
            StringBuilder templatePrefix = new StringBuilder("template <");
            templatePrefix.append("typename ");
            templatePrefix.append(tpList.get(0));
            for (int i=1; i<tpList.size(); i++) {
                templatePrefix.append(", typename ");
                templatePrefix.append(tpList.get(i));
            }
            templatePrefix.append("> ");

            headerWriter.write(tabSpace + StringConstants.TAB + templatePrefix + "\n");
            headerWriter.write(tabSpace + StringConstants.TAB + scope + name + methodParameterStr + "\n" + tabSpace + StringConstants.TAB + "{\n");
            headerWriter.write(tabSpace + StringConstants.TAB + "}\n");

            processBlockStmt(implWriter, constructor.getBody());
        } else {
            headerWriter.write(tabSpace + StringConstants.TAB + name + methodParameterStr + ";\n");


            implWriter.write("\n\n");
            implWriter.write(scope + name + methodParameterStr + "\n{\n");
            implWriter.write(StringConstants.TAB + "\n");

            processBlockStmt(implWriter, constructor.getBody());
            implWriter.write("}\n");
        }
    }


    public static void processBlockStmt(Writer implWriter, BlockStmt stmt) throws Exception {
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


}
