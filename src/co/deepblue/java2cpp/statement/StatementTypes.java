package co.deepblue.java2cpp.statement;

import com.github.javaparser.ast.stmt.*;

import java.io.BufferedWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by levin on 17-5-10.
 */
public class StatementTypes {
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

    public static Set<Class> blockStmtTypes;

    public static void registerStatementTypes(Statement statement) {
        if (blockStmtTypes == null)
            blockStmtTypes = new HashSet<>();
        blockStmtTypes.add(statement.getClass());
    }

    public static void printRegisteredTypes() {
        for (Class cls : blockStmtTypes) {
            System.out.print(cls + ".class");
            System.out.print(", ");
        }
    }


    public static void translateStatement(SynchronizedStmt stmt, BufferedWriter imWriter) {
    }

    public static void translateStatement(ExpressionStmt stmt, BufferedWriter imWriter) {

    }

    public static void translateStatement(IfStmt stmt, BufferedWriter imWriter) {

    }

    public static void translateStatement(ForStmt stmt, BufferedWriter imWriter) {

    }

    public static void translateStatement(ForeachStmt stmt, BufferedWriter imWriter) {

    }

    public static void translateStatement(WhileStmt stmt, BufferedWriter imWriter) {

    }

    public static void translateStatement(SwitchStmt stmt, BufferedWriter imWriter) {

    }

    public static void translateStatement(TryStmt stmt, BufferedWriter imWriter) {

    }

    public static void translateStatement(LabeledStmt stmt, BufferedWriter imWriter) {

    }

    public static void translateStatement(ReturnStmt stmt, BufferedWriter imWriter) {

    }
}
