package co.deepblue.java2cpp.processor;

import co.deepblue.java2cpp.util.StringConstants;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by levin on 17-5-8.
 */
// compile unit processor
public class ComplexEnumProcessor {
    public static void process(EnumDeclaration enumDeclaration, Writer headerWriter, Writer implWriter, String tabSpace) throws Exception {
        String className = enumDeclaration.getNameAsString();
        headerWriter.write(tabSpace + "class " + className);

        if (className.equals("WorldType")) {
            System.out.println("breakpoing debuging " + className);
        }

        NodeList<ClassOrInterfaceType> implementedTypes = enumDeclaration.getImplementedTypes();
        if (implementedTypes.size() > 0) {
            headerWriter.write(" : ");
            headerWriter.write("public " + implementedTypes.get(0));
            for (int i = 1; i < implementedTypes.size(); i++) {
                headerWriter.write(", public " + implementedTypes.get(i));
            }
        }

        headerWriter.write("\n" + tabSpace + "{\n");
        headerWriter.write(tabSpace + "public:\n");

        NodeList<BodyDeclaration<?>> members = enumDeclaration.getMembers();
        List<FieldDeclaration> fieldList = new ArrayList<>();
        List<MethodDeclaration> methodList = new ArrayList<>();
        List<ConstructorDeclaration> constructorList = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> classOrInterfaceList = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> staticClassOrInterfaceList = new ArrayList<>();
        for (BodyDeclaration<?> member : members) {
            if (member instanceof MethodDeclaration) {
                methodList.add((MethodDeclaration) member);
                ///processMethod(method, headerWriter);
            } else if (member instanceof FieldDeclaration) {
                fieldList.add((FieldDeclaration) member);
                //processField(field, headerWriter);
            } else if (member instanceof ConstructorDeclaration) {
                constructorList.add((ConstructorDeclaration) member);
                //processConstructor((ConstructorDeclaration)member, headerWriter);
            } else if (member instanceof ClassOrInterfaceDeclaration) {
                classOrInterfaceList.add((ClassOrInterfaceDeclaration) member);
            } else if (member instanceof EnumDeclaration) {
                EnumDeclaration declaration = (EnumDeclaration) member;
                if (declaration.getMembers().size() <= 0) {
                    SimpleEnumCreator.create(declaration, headerWriter, tabSpace + StringConstants.TAB);
                } else {
                    process(declaration,
                            headerWriter,
                            implWriter,
                            tabSpace + StringConstants.TAB);
                }
            }
        }


        if (staticClassOrInterfaceList.size() > 0) {
            headerWriter.write("\n");
            for (ClassOrInterfaceDeclaration classOrInterface : staticClassOrInterfaceList) {
                ClassOrInterfaceProcessor.process(classOrInterface, headerWriter, implWriter, tabSpace + StringConstants.TAB);
                headerWriter.write("\n\n");
            }
        }

        headerWriter.write("\n");
        for (ClassOrInterfaceDeclaration classOrInterface : classOrInterfaceList) {
            headerWriter.write("class " + classOrInterface.getNameAsString() + ";\n");
        }

        if (fieldList.size() > 0) {
            for (FieldDeclaration field : fieldList) {
                FieldProcessor.processField(field, headerWriter, tabSpace);
            }
            headerWriter.write("\n\n");
        }


        boolean hasDefaultConstructor = AstNodeHelper.containsDefaultConstructor(constructorList);
        if (!hasDefaultConstructor) {
            String scope = AstNodeHelper.getScope(enumDeclaration);
            headerWriter.write("\n");
            Constructor.addDefaultConstructor(className, scope + className + "::", headerWriter, implWriter, tabSpace);
        }

        if (constructorList.size() > 0) {
            for (ConstructorDeclaration constructor : constructorList) {
                MethodProcessor.processConstructor(null, constructor, headerWriter, implWriter, tabSpace);
            }
            headerWriter.write("\n\n");
        }

        if (methodList.size() > 0) {
            for (MethodDeclaration method : methodList) {
                MethodProcessor.process(method, headerWriter, implWriter, tabSpace);
            }
        }

        if (classOrInterfaceList.size() > 0) {
            // is there any static inner class in cpp?
            for (ClassOrInterfaceDeclaration classOrInterface : classOrInterfaceList) {
                ClassOrInterfaceProcessor.process(classOrInterface, headerWriter, implWriter, tabSpace + StringConstants.TAB);
            }
            headerWriter.write("\n\n");
        }


        headerWriter.write(tabSpace + "}");
    }
}
