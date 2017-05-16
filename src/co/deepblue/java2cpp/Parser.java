package co.deepblue.java2cpp;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.FileInputStream;
import java.util.Optional;

import static com.github.javaparser.ast.type.PrimitiveType.intType;

/**
 * Created by levin on 17-5-6.
 */

public class Parser {
    int a = 0;
    class b{
        int x = a;
    }

    public Optional<ClassOrInterfaceDeclaration> parseClass(String classString) {
        CompilationUnit compilationUnit = JavaParser.parse(classString);
        return compilationUnit.getClassByName("A");
    }


    public String parseSourceFile(String fileName) throws Exception {
        FileInputStream in = new FileInputStream(fileName);

        // parse the file
        CompilationUnit cu = JavaParser.parse(in);
        // prints the resulting compilation unit to default system output
        return cu.toString();
    }

    public CompilationUnit parseToCompilationUnit(String fileName) throws Exception {
        FileInputStream in = new FileInputStream(fileName);

        // parse the file
        CompilationUnit cu = JavaParser.parse(in);
        // prints the resulting compilation unit to default system output
        return cu;
    }


    public static class MethodVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            /* here you can access the attributes of the method.
             this method will be called for all methods in this
             CompilationUnit, including inner class methods */
            System.out.println(n.getName());
            super.visit(n, arg);
        }
    }

    public void changeMethods(CompilationUnit cu) {
        // Go through all the types in the file
        NodeList<TypeDeclaration<?>> types = cu.getTypes();
        for (TypeDeclaration<?> type : types) {
            // Go through all fields, methods, etc. in this type
            NodeList<BodyDeclaration<?>> members = type.getMembers();
            for (BodyDeclaration<?> member : members) {
                if (member instanceof MethodDeclaration) {
                    MethodDeclaration method = (MethodDeclaration) member;
                    changeMethod(method);
                }
            }
        }
    }

    public void changeMethod(MethodDeclaration n) {
        // change the name of the method to upper case
        n.setName(n.getNameAsString().toUpperCase());

        // create the new parameter
        n.addParameter(intType(), "value");
    }

}
