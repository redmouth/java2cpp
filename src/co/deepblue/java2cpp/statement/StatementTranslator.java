package co.deepblue.java2cpp.statement;

import co.deepblue.java2cpp.processor.AstNodeHelper;
import co.deepblue.java2cpp.util.StringConstants;
import co.deepblue.java2cpp.processor.ExpressionProcessor;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import java.util.Optional;

/**
 * Created by levin on 17-5-10.
 */
public class StatementTranslator {
    public static Class[] types = {
            SynchronizedStmt.class,
            ExpressionStmt.class,
            IfStmt.class,
            ForStmt.class,
            ForeachStmt.class,
            WhileStmt.class,
            SwitchStmt.class,
            TryStmt.class,
            ThrowStmt.class,
            LabeledStmt.class,
            ReturnStmt.class
    };

    public static String translateBlockStmt(BlockStmt blockStmt) {
        if (!blockStmt.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (Statement statement : blockStmt.getStatements()) {
                builder.append(translateStmt(statement)).append("\n");
            }
            return builder.toString();
        }
        return "";
    }

    public static String translateStmt(Statement statement) {
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
        } else if (statement instanceof BlockStmt) {
            statementStr = translateBlockStmt((BlockStmt) statement);
        }

        return statementStr;
    }

    public static String translateSynchronizedStmt(SynchronizedStmt stmt) {

        StringBuilder builder = new StringBuilder();

        String mutexName = "coutMutex";
        //builder.append("std::lock_guard<std::mutex> lock(coutMutex);");
        builder.append("std::lock_guard<std::mutex> lock(").append(mutexName).append(");\n");


        builder.append(translateBlockStmt(stmt.getBody()));

        return builder.toString();
    }

    public static String translateExpressionStmt(ExpressionStmt stmt) {
        return StringConstants.TAB + ExpressionProcessor.parseInitializerExpressionToCpp(stmt.getExpression()) + ";\n";
    }

    public static String translateIfStmt(IfStmt stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("if (");
        Expression condition = stmt.getCondition();
        String expStr = ExpressionProcessor.parseInitializerExpressionToCpp(condition);
        builder.append(expStr);
        builder.append(") {\n");

        builder.append(translateStmt(stmt.getThenStmt()));

        builder.append("} ");

        Optional<Statement> elseStmt = stmt.getElseStmt();
        if (elseStmt.isPresent()) {
            builder.append(" else {\n");
            Statement elseSt = elseStmt.get();
            builder.append(translateStmt(elseSt));
            builder.append("}\n");
        } else
            builder.append("\n");

        builder.append("\n");

        return builder.toString();
    }

    public static String translateForStmt(ForStmt stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("for (");
        String initializer = InitializerParser.parseStatementExpression(stmt.getInitialization());
        builder.append(initializer);
        builder.append("; ");

        if (stmt.getCompare().isPresent()) {
            Expression expression = stmt.getCompare().get();
            String comparator = ExpressionProcessor.parseInitializerExpressionToCpp(expression);
            builder.append(comparator);
        }
        builder.append("; ");

        builder.append(InitializerParser.parseStatementExpression(stmt.getUpdate()));

        builder.append(") {\n");

        String blockStr = translateStmt(stmt.getBody());
        builder.append(blockStr);

        builder.append("\n}\n");

        builder.append("\n");

        return builder.toString();
    }

    public static String translateForeachStmt(ForeachStmt stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("for (");

        String variable = InitializerParser.parseVariable(stmt.getVariable().getVariables().get(0));

        String iterable = stmt.getIterable().toString();
        builder.append(variable);
        builder.append(" : ");
        builder.append(iterable);


        builder.append(") {\n");
        String blockStr = translateStmt(stmt.getBody());
        builder.append(blockStr);

        builder.append("\n}\n");

        builder.append("\n");

        return builder.toString();
    }

    public static String translateWhileStmt(WhileStmt stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("while (");

        Expression condition = stmt.getCondition();
        String expStr = ExpressionProcessor.parseInitializerExpressionToCpp(condition);
        builder.append(expStr);

        builder.append(") {\n");

        String blockStr = translateStmt(stmt.getBody());
        builder.append(blockStr);

        builder.append("\n}\n");

        builder.append("\n");

        return builder.toString();
    }

    public static String translateSwitchStmt(SwitchStmt stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("switch (");
        builder.append(ExpressionProcessor.parseInitializerExpressionToCpp(stmt.getSelector()));
        builder.append(") {\n");

        for (SwitchEntryStmt entryStmt : stmt.getEntries()) {
            builder.append(String.valueOf("case " + entryStmt.getLabel() + ":\n"));
            for (Statement statement : entryStmt.getStatements()) {
                String stStr = translateStmt(statement);
                builder.append(stStr).append("\n");
            }
            builder.append("break;\n");
        }

        builder.append("\n}\n");
        builder.append("\n");

        return builder.toString();
    }

    public static String translateTryStmt(TryStmt stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("try {\n");

        if (stmt.getTryBlock().isPresent()) {
            String blockStr = translateBlockStmt(stmt.getTryBlock().get());
            builder.append(blockStr);
            builder.append("\n");
        }

        builder.append("\n} ");

        for (CatchClause catchClause : stmt.getCatchClauses()) {
            InitializerParser.addClauseParameterToSymbolTree(catchClause.getParameter());

            builder.append("catch (const ")
                    .append(catchClause.getParameter().getType())
                    .append("& ")
                    .append(catchClause.getParameter().getName())
                    .append(") {\n")

                    .append(translateBlockStmt(catchClause.getBody()))

                    .append("} \n");
        }

        builder.append("\n");

        return builder.toString();
    }

    public static String translateLabeledStmt(LabeledStmt stmt) {
        String labelName = stmt.getLabel().getIdentifier();
        StringBuilder builder = new StringBuilder();
        builder.append(labelName).append(":{\n");

        builder.append(translateStmt(stmt.getStatement()));

        builder.append("}\n");

        return builder.toString();
    }

    public static String translateReturnStmt(ReturnStmt stmt) {
        String expStr = "";
        if (stmt.getExpression().isPresent()) {
            Expression expression = stmt.getExpression().get();
            expStr = ExpressionProcessor.parseInitializerExpressionToCpp(stmt.getExpression().get());

            if (expression instanceof ArrayAccessExpr) {
                Expression nameExpr = ((ArrayAccessExpr) expression).getName();
                if (nameExpr instanceof NameExpr) {
                    Type componentType = AstNodeHelper.getArrayComponentType((NameExpr)nameExpr);
                    if (componentType instanceof ClassOrInterfaceType) {
                        expStr = "&" + expStr;
                    }
                }
            }
        }
        return StringConstants.TAB + "return " + expStr + ";";
    }

    public static String translateThrowStmt(ThrowStmt stmt) {
        return "";
    }
}
