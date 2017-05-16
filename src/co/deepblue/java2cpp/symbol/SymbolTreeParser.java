package co.deepblue.java2cpp.symbol;

import co.deepblue.java2cpp.processor.AstNodeHelper;
import co.deepblue.java2cpp.statement.StatementTypes;
import co.deepblue.java2cpp.util.StringUtils;
import co.deepblue.java2cpp.type.TypeConverter;
import co.deepblue.java2cpp.processor.ModifierProcessor;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;

import java.util.*;

import static co.deepblue.java2cpp.symbol.EnumSymbolType.*;

/**
 * Created by levin on 17-5-10.
 */
public class SymbolTreeParser {
    static SymbolTreeParser instance_;
    SymbolNode projRoot;
    HashMap<String, List<SymbolNode>> global_symbol_map;
    HashMap<String, CompilationUnit> cuMap;
    ArrayList<CompilationUnit> cuList;

    HashMap<String, Set<String>> includesMap;
    HashMap<String, Set<String>> namespaceMap;
    HashMap<String, String> predefinedCppInclude = new HashMap<>();

    MethodDeclaration mainMethod;
    CompilationUnit mainCU;

    public static SymbolTreeParser getInstance() {
        return instance_;
    }

    public static void createInstance(String projectName, HashMap<String, CompilationUnit> cuMap, ArrayList<CompilationUnit> cuList) {
        if (instance_ == null) {
            instance_ = new SymbolTreeParser(projectName, cuMap, cuList);
        }
    }

    private SymbolTreeParser(String projectName, HashMap<String, CompilationUnit> cuMap, ArrayList<CompilationUnit> cuList) {
        this.cuMap = cuMap;
        this.cuList = cuList;
        global_symbol_map = new HashMap<>();
        includesMap = new HashMap<>();
        namespaceMap = new HashMap<>();

        projRoot = new SymbolNode(null,null, projectName, SYMBOL_PROJECT, null, null, null, true);
    }

    public SymbolNode addNodeToTree(SymbolNode node, SymbolNode parent) {
        parent.addChild(node);
        return node;
    }

    public void scanSymbols() {
        for (CompilationUnit cu : cuList) {
            if (cu.getPackageDeclaration().isPresent()) {
                String packageName = cu.getPackageDeclaration().get().getName().asString();
                NodeList<TypeDeclaration<?>> types = cu.getTypes();
                if (types.size() > 0) {
                    TypeDeclaration typeDeclaration = types.get(0);
                    String classOrEnumName = typeDeclaration.getNameAsString();

                    SymbolNode node = new SymbolNode(projRoot, packageName, classOrEnumName, SYMBOL_TYPE_NONE, typeDeclaration, typeDeclaration, cu, true);
                    add_to_global_map(node.name, node);

                    if (typeDeclaration instanceof ClassOrInterfaceDeclaration) {
                        node.type = SYMBOL_TYPE_CLASSORINTERFACE;
                        buildClassSymbol((ClassOrInterfaceDeclaration)typeDeclaration, node);
                    } else if (typeDeclaration instanceof EnumDeclaration) {
                        node.type = SYMBOL_TYPE_ENUM;
                        EnumDeclaration ed = (EnumDeclaration) typeDeclaration;
                        buildEnumSymbol(ed, node);
                        if (ed.getMembers().size() <= 0) {
                            TypeConverter.getInstance().addSimpleEnumType(ed.getNameAsString(), packageName);
                        }
                    }
                }
            }
        }
    }

    public SymbolNode addNode(TypeDeclaration declaration, EnumSet<Modifier> modifiers, EnumSymbolType type, TypeDeclaration parentDelcaration, SymbolNode parent) {
        String name = declaration.getNameAsString();
        SymbolNode node = new SymbolNode(parent, parent.packageName, name, type, declaration, parentDelcaration, null, false);
        ModifierProcessor modifier = new ModifierProcessor();
        modifier.processModifiers(modifiers);
        node.isStatic = modifier.isStatic;
        add_to_global_map(name, node);

        return node;
    }

    public SymbolNode addNode(String name, BodyDeclaration declaration, EnumSet<Modifier> modifiers, EnumSymbolType type, TypeDeclaration parentDelcaration, SymbolNode parent) {
        SymbolNode node = new SymbolNode(parent, parent.packageName, name, type, declaration, parentDelcaration, null, false);
        ModifierProcessor modifier = new ModifierProcessor();
        modifier.processModifiers(modifiers);
        node.isStatic = modifier.isStatic;
        add_to_global_map(name, node);

        return node;
    }

    public SymbolNode addNode(String name, Node declaration, boolean isStatic, EnumSymbolType type, Node parentDelcaration, SymbolNode parent) {
        SymbolNode node = new SymbolNode(parent, parent.packageName, name, type, declaration, parentDelcaration, null, false);
        node.isStatic = isStatic;
        add_to_global_map(name, node);

        return node;
    }

    public void buildClassSymbol(ClassOrInterfaceDeclaration parentDelcaration, SymbolNode parent) {
        /*
        NodeList<TypeParameter> typeParameters = classOrInterfaceDeclaration.getTypeParameters();  //Template Parameters
        if (typeParameters.size() > 0) {
            for (TypeParameter parameter : typeParameters) {
                classSymbol.template_parameters.add(parameter.getNameAsString());
            }
        }
        */

        NodeList<BodyDeclaration<?>> members = parentDelcaration.getMembers();
        for (BodyDeclaration<?> member : members) {
            if (member instanceof MethodDeclaration) {
                processMethod(parentDelcaration, (MethodDeclaration) member, parent);
            } else if (member instanceof FieldDeclaration) {
                processField(parentDelcaration, (FieldDeclaration)member, parent);
            } else if (member instanceof ConstructorDeclaration) {
                processConstructor(parentDelcaration, (ConstructorDeclaration)member, parent);
            } else if (member instanceof ClassOrInterfaceDeclaration) {
                SymbolNode node = addNode((TypeDeclaration) member, ((ClassOrInterfaceDeclaration) member).getModifiers(),
                        SYMBOL_TYPE_INNER_CLASS, parentDelcaration, parent);
                buildClassSymbol((ClassOrInterfaceDeclaration) member, node);
            } else if (member instanceof EnumDeclaration) {
                SymbolNode node = addNode((TypeDeclaration) member, ((EnumDeclaration) member).getModifiers(), SYMBOL_TYPE_INNER_ENUM,
                        parentDelcaration, parent);
                buildEnumSymbol((EnumDeclaration)member, node);
            }
        }
    }


    public void buildEnumSymbol(EnumDeclaration parentDelcaration, SymbolNode parent) {
        NodeList<BodyDeclaration<?>> members = parentDelcaration.getMembers();
        for (BodyDeclaration<?> member : members) {
            if (member instanceof MethodDeclaration) {
                processMethod(parentDelcaration, (MethodDeclaration) member, parent);
            } else if (member instanceof FieldDeclaration) {
                processField(parentDelcaration, (FieldDeclaration)member, parent);
            } else if (member instanceof ConstructorDeclaration) {
                processConstructor(parentDelcaration, (ConstructorDeclaration)member, parent);
            } else if (member instanceof ClassOrInterfaceDeclaration) {
                SymbolNode node = addNode((TypeDeclaration) member, ((ClassOrInterfaceDeclaration) member).getModifiers(),
                        SYMBOL_TYPE_INNER_CLASS, parentDelcaration, parent);
                buildClassSymbol((ClassOrInterfaceDeclaration) member, node);
            } else if (member instanceof EnumDeclaration) {
                SymbolNode node = addNode((TypeDeclaration) member, ((EnumDeclaration) member).getModifiers(), SYMBOL_TYPE_INNER_ENUM,
                        parentDelcaration, parent);
                buildEnumSymbol((EnumDeclaration)member, node);
            }
        }

        if (parentDelcaration.getMembers().size() <= 0) {
            TypeConverter.getInstance().addSimpleEnumType(parentDelcaration.getNameAsString(), parent.getPackageName());
        }
    }



    public void processConstructor(TypeDeclaration parentDeclaration, ConstructorDeclaration constructor, SymbolNode parent) {
        String name = constructor.getNameAsString();
        SymbolNode node = addNode(name, constructor, constructor.getModifiers(), SYMBOL_TYPE_CLASS_CONSTRUCTOR, parentDeclaration, parent);

        BlockStmt blockStmt = constructor.getBody();
        NodeList<Statement> statements = blockStmt.getStatements();
        for (Statement statement : statements) {
            if (statement instanceof ExpressionStmt) {
                Expression expression = ((ExpressionStmt) statement).getExpression();
                if (expression instanceof VariableDeclarationExpr) {
                    processVariableDeclaration(constructor, (VariableDeclarationExpr)expression, node);
                }
            } else if (statement instanceof LocalClassDeclarationStmt) {
                LocalClassDeclarationStmt lcd = (LocalClassDeclarationStmt) statement;
            }
        }
    }


    public void processMethod(TypeDeclaration parentDeclaration, MethodDeclaration method, SymbolNode parent) {
        String name = method.getNameAsString();
        ModifierProcessor modifier = new ModifierProcessor();
        modifier.processModifiers(method.getModifiers());
        boolean isPublicStaticVoidMain = false;
        if (name.equals("main") && modifier.isStatic && modifier.isPublic) {
            Type type = method.getType();
            if (type instanceof VoidType) {
                isPublicStaticVoidMain = true;
            }
        }

        SymbolNode methodNode = addNode(name, method, method.getModifiers(), SYMBOL_TYPE_CLASS_METHOD, parentDeclaration, parent);

        NodeList<Parameter> parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            addNode(parameter.getNameAsString(), parameter, false, SYMBOL_TYPE_METHOD_OR_CONSTRUCTOR_PARAMETER, method, methodNode);
        }
        if (isPublicStaticVoidMain && parameters.size() == 1 && (parameters.get(0).getType() instanceof ArrayType)) {
            ArrayType arrayType = (ArrayType) parameters.get(0).getType();
            Type componentType = arrayType.getComponentType();
            if ((componentType instanceof ClassOrInterfaceType) && ((ClassOrInterfaceType) componentType).getNameAsString().equals("String")) {
                setMainMethod(method);
            }
        }

        Optional<BlockStmt> blockStmt = method.getBody();
        if (blockStmt.isPresent()) {
            BlockStmt stmt = blockStmt.get();
            NodeList<Statement> statements = stmt.getStatements();
            for (Statement statement : statements) {
                StatementTypes.registerStatementTypes(statement);
                if (statement instanceof ExpressionStmt) {
                    Expression expression = ((ExpressionStmt) statement).getExpression();
                    if (expression instanceof VariableDeclarationExpr) {
                        processVariableDeclaration(method, (VariableDeclarationExpr)expression, methodNode);
                    }
                } else if (statement instanceof LocalClassDeclarationStmt) {
                    LocalClassDeclarationStmt lcd = (LocalClassDeclarationStmt) statement;
                } else if (statement instanceof ReturnStmt) {

                }
            }
        }
    }


    public void processField(TypeDeclaration parentDeclaration, FieldDeclaration fieldDeclaration, SymbolNode parent) {
        ModifierProcessor modifier = new ModifierProcessor();
        modifier.processModifiers(fieldDeclaration.getModifiers());

        for (VariableDeclarator vd : fieldDeclaration.getVariables()) {
            String name = vd.getNameAsString();
            addNode(name, vd, modifier.isStatic, SYMBOL_TYPE_CLASS_FIELD, parentDeclaration, parent);
        }
    }

    public void processVariableDeclaration(BodyDeclaration parentDeclaration, VariableDeclarationExpr vExpr, SymbolNode parent) {
        ModifierProcessor modifier = new ModifierProcessor();
        modifier.processModifiers(vExpr.getModifiers());
        NodeList<VariableDeclarator> variables = vExpr.getVariables();
        for (VariableDeclarator variable : variables) {
            String name = variable.getNameAsString();
            addNode(name, variable, modifier.isStatic, SYMBOL_TYPE_METHOD_LOCAL_VARIABLE, parentDeclaration, parent);
        }
    }

    private void add_to_global_map(String name, SymbolNode symbol) {
        if (global_symbol_map.containsKey(name)) {
            List<SymbolNode> symbolList = global_symbol_map.get(name);
            symbolList.add(symbol);
        } else {
            List<SymbolNode> symbolList = new ArrayList<>();
            symbolList.add(symbol);
            global_symbol_map.put(name, symbolList);
        }
    }

    public String getTypeScope(String name, CompilationUnit unit) {
        List<SymbolNode> nodeList = global_symbol_map.get(name);
        if (nodeList != null) {
            for (SymbolNode node : nodeList) {
                if (node.isInnerClassOrEnum()) {
                    String scope = SymbolNodeHelper.queryFileName(node, unit);
                    if (scope != null)
                        return scope;
                }
            }
        }
        return "";
    }


    private String findSymbol(String typeName) {
        List<SymbolNode> nodeList = global_symbol_map.get(typeName);
        if (nodeList != null) {
            for (SymbolNode node : nodeList) {
                if (node.isClassOrEnum()) {
                    String fullName = SymbolNodeHelper.queryFileName(node);
                    if (fullName != null) {
                        return fullName;
                    }
                }
            }
        }
        return null;
    }

    public void addIncludeForType(CompilationUnit unit, String typeName) {
        String className = unit.getData(AstNodeHelper.FilenameDatakey);
        Set<String> includes = includesMap.get(className);
        if (includes != null) {
            for (String include : includes) {
                if (include.endsWith("." + typeName)) {
                    return;
                }
            }
        }

        String includeName = findSymbol(typeName);
        addInclude(className, includeName);
    }

    public void addInclude(String className, String include) {
        if (!StringUtils.isEmpty(include)) {
            if (includesMap.containsKey(className)) {
                Set<String> includes = includesMap.get(className);
                includes.add(include);
            } else {
                Set<String> includes = new HashSet<>();
                includes.add(include);
                includesMap.put(className, includes);
            }
        }
    }

    public void addIncludeForUnit(CompilationUnit cu, String include) {
        if (!StringUtils.isEmpty(include)) {
            String className = cu.getData(AstNodeHelper.FilenameDatakey);
            if (includesMap.containsKey(className)) {
                Set<String> includes = includesMap.get(className);
                includes.add(include);
            } else {
                Set<String> includes = new HashSet<>();
                includes.add(include);
                includesMap.put(className, includes);
            }
        }
    }

    public Set<String> getInclude(String fileName) {
        if (includesMap.containsKey(fileName))
            return includesMap.get(fileName);
        return null;
    }

    public void addNamespace(String className, String namespace) {
        if (namespaceMap.containsKey(className)) {
            Set<String> namespaces = namespaceMap.get(className);
            namespaces.add(namespace);
        } else {
            Set<String> namespaces = new HashSet<>();
            namespaces.add(namespace);
            namespaceMap.put(className, namespaces);
        }
    }

    public Set<String> getNamespace(String fileName) {
        if (namespaceMap.containsKey(fileName))
            return namespaceMap.get(fileName);
        return null;
    }

    public MethodDeclaration getMainMethod() {
        return mainMethod;
    }

    public CompilationUnit getMainCU() {
        return mainCU;
    }

    public void setMainMethod(MethodDeclaration method) {
        this.mainMethod = method;
        mainCU = AstNodeHelper.getCompilationUnit(method);
    }


    public void registerPredefinedInclude(String typeName, String include) {
        predefinedCppInclude.put(typeName, include);
    }

    public void addPredefinedIncludeToUnit(CompilationUnit cu, String typeName) {
        if (predefinedCppInclude.containsKey(typeName)) {
            String include = predefinedCppInclude.get(typeName);
            addIncludeForUnit(cu, include);
        }
    }


    int totalNumberOfSymbols = 0;
    public int getTotalNumberOfSymbols() {
        if (totalNumberOfSymbols == 0) {
            int total = 0;
            for (Map.Entry<String, List<SymbolNode>> entry : global_symbol_map.entrySet()) {
                total += entry.getValue().size();
            }
            totalNumberOfSymbols = total;
        }
        return totalNumberOfSymbols;
    }
}
