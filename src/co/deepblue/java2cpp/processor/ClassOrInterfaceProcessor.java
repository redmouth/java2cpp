package co.deepblue.java2cpp.processor;

import co.deepblue.java2cpp.symbol.SymbolNode;
import co.deepblue.java2cpp.util.StringConstants;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by levin on 17-5-8.
 */
// compile unit processor
public class ClassOrInterfaceProcessor {

    public static void process(ClassOrInterfaceDeclaration classOrInterfaceDeclaration, Writer headerWriter, Writer implWriter, String tabSpace) throws Exception {
        String className = classOrInterfaceDeclaration.getNameAsString();
        if (className.equals("WorldType")) {
            System.out.println("breakpoing debuging " + className);
        }

        NodeList<TypeParameter> typeParameters = classOrInterfaceDeclaration.getTypeParameters();  //Template Parameters
        HashMap<String, TypeParameter> typeParameterHashMap = new HashMap<>();
        boolean hasTemplateParameters = false;
        if (typeParameters.size() > 0) {
            hasTemplateParameters = true;
            headerWriter.write(tabSpace + "template <");
            StringBuilder tpList = new StringBuilder("typename " + typeParameters.get(0).getNameAsString());
            typeParameterHashMap.put(typeParameters.get(0).getNameAsString(), typeParameters.get(0));
            for (int i = 1; i < typeParameters.size(); i++) {
                tpList.append(", typename ");
                tpList.append(typeParameters.get(i).getNameAsString());
                typeParameterHashMap.put(typeParameters.get(i).getNameAsString(), typeParameters.get(i));
            }
            headerWriter.write(tpList.toString());
            headerWriter.write(">\n");
        }

        headerWriter.write(tabSpace + "class " + className);
        if (hasTemplateParameters) {
            headerWriter.write("<" + typeParameters.get(0).getNameAsString());
            for (int i = 1; i < typeParameters.size(); i++) {
                headerWriter.write(", " + typeParameters.get(i).getNameAsString());
            }
            headerWriter.write(">");
        }

        NodeList<ClassOrInterfaceType> extendedTypes = classOrInterfaceDeclaration.getExtendedTypes();
        NodeList<ClassOrInterfaceType> implementedTypes = classOrInterfaceDeclaration.getImplementedTypes();
        if (extendedTypes.size() > 0 || implementedTypes.size() > 0) {
            headerWriter.write(" : ");
            ArrayList<String> inheritedNames = new ArrayList<>();
            for (ClassOrInterfaceType ci : extendedTypes) {
                inheritedNames.add(ci.getNameAsString());
            }
            for (ClassOrInterfaceType ci : implementedTypes) {
                inheritedNames.add(ci.getNameAsString());
            }
            headerWriter.write("public " + inheritedNames.get(0));
            for (int i = 1; i < inheritedNames.size(); i++) {
                headerWriter.write(", public " + inheritedNames.get(i));
            }
        }

        headerWriter.write("\n" + tabSpace+ "{\n");
        headerWriter.write(tabSpace + "public:\n");

        NodeList<BodyDeclaration<?>> members = classOrInterfaceDeclaration.getMembers();
        List<FieldDeclaration> fieldList = new ArrayList<>();
        List<MethodDeclaration> methodList = new ArrayList<>();
        List<ConstructorDeclaration> constructorList = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> classOrInterfaceList = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> staticClassOrInterfaceList = new ArrayList<>();
        for (BodyDeclaration<?> member : members) {
            if (member instanceof MethodDeclaration) {
                methodList.add((MethodDeclaration) member);
            } else if (member instanceof FieldDeclaration) {
                fieldList.add((FieldDeclaration) member);
            } else if (member instanceof ConstructorDeclaration) {
                constructorList.add((ConstructorDeclaration) member);
            } else if (member instanceof ClassOrInterfaceDeclaration) {
                if (SymbolNode.isStatic((ClassOrInterfaceDeclaration)member))
                    staticClassOrInterfaceList.add((ClassOrInterfaceDeclaration)member);
                else
                    classOrInterfaceList.add((ClassOrInterfaceDeclaration) member);
            } else if (member instanceof EnumDeclaration) {
                EnumDeclaration enumDeclaration = (EnumDeclaration) member;
                if (enumDeclaration.getMembers().size() <= 0) {
                    SimpleEnumCreator.create(enumDeclaration, headerWriter, tabSpace + StringConstants.TAB);
                } else {
                    ComplexEnumProcessor.process((EnumDeclaration)member,
                            headerWriter,
                            implWriter,
                            tabSpace + StringConstants.TAB);
                }

                headerWriter.write("\n");
            }
        }

        if (staticClassOrInterfaceList.size() > 0) {
            headerWriter.write("\n");
            for (ClassOrInterfaceDeclaration classOrInterface : staticClassOrInterfaceList) {
                process(classOrInterface, headerWriter, implWriter, tabSpace + StringConstants.TAB);
                headerWriter.write("\n\n");
            }
        }

        headerWriter.write("\n");
        for (ClassOrInterfaceDeclaration classOrInterface : classOrInterfaceList) {
            headerWriter.write(tabSpace + StringConstants.TAB + "class " + classOrInterface.getNameAsString() + ";\n\n");
        }


        if (fieldList.size() > 0) {
            for (FieldDeclaration field : fieldList) {
                FieldProcessor.processField(field, headerWriter, tabSpace);
            }
        }

        boolean hasDefaultConstructor = AstNodeHelper.containsDefaultConstructor(constructorList);
        if (!hasDefaultConstructor) {
            String scope = AstNodeHelper.getScope(classOrInterfaceDeclaration);
            headerWriter.write("\n");
            Constructor.addDefaultConstructor(className, scope + className + "::", headerWriter, implWriter, tabSpace);
        }

        if (constructorList.size() > 0) {
            headerWriter.write("\n");
            for (ConstructorDeclaration constructor : constructorList) {
                MethodProcessor.processConstructor(typeParameterHashMap, constructor, headerWriter, implWriter, tabSpace);
            }
        }

        if (methodList.size() > 0) {
            headerWriter.write("\n\n");
            for (MethodDeclaration method : methodList) {
                MethodProcessor.process(method, headerWriter, implWriter, tabSpace);
            }
        }

        if (classOrInterfaceList.size() > 0) {
            // is there any static inner class in cpp?
            headerWriter.write("\n");
            for (ClassOrInterfaceDeclaration classOrInterface : classOrInterfaceList) {
                ClassOrInterfaceProcessor.process(classOrInterface, headerWriter, implWriter, tabSpace + StringConstants.TAB);
            }
            headerWriter.write("\n\n");
        }

        headerWriter.write(tabSpace + "};");
    }
}
