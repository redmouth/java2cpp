package co.deepblue.java2cpp.symbol;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by levin on 17-5-10.
 */
public class SymbolNode {
    public static final DataKey<SymbolNode> dataKey = new DataKey<SymbolNode>() { };

    SymbolNode parent;
    String packageName;
    String name;
    EnumSymbolType type;
    Node astNode;
    Node parentAstNode;
    CompilationUnit unit;
    boolean isStatic;

    List<SymbolNode> children;

    public SymbolNode(SymbolNode parent, String packageName, String name, EnumSymbolType type, Node astNode, Node parentAstNode, CompilationUnit unit, boolean isStatic) {
        this.parent = parent;
        this.packageName = packageName;
        this.name = name;
        this.type = type;
        this.astNode = astNode;
        this.parentAstNode = parentAstNode;
        this.unit = unit;
        this.isStatic = isStatic;

        if (astNode != null)
            astNode.setData(dataKey, this);
        if (parent != null)
            parent.addChild(this);
    }

    public EnumSymbolType getType() {
        return type;
    }

    public String getPackageName() {
        return packageName;
    }

    public void addChild(SymbolNode node) {
        if (children == null) {
            children = new ArrayList<>();
        }
        if (!children.contains(node))
            children.add(node);
    }

    public boolean isInnerClassOrEnum () {
        return type == EnumSymbolType.SYMBOL_TYPE_INNER_CLASS ||
                type == EnumSymbolType.SYMBOL_TYPE_INNER_ENUM;
    }

    public boolean isClassOrEnum () {
        return SymbolNode.isClassOrEnum(type);
    }

    boolean isNotMethodAndConstructor() {
        return type != EnumSymbolType.SYMBOL_TYPE_CLASS_METHOD && type != EnumSymbolType.SYMBOL_TYPE_CLASS_CONSTRUCTOR;
    }

    public boolean isFieldOrLocalVariableOrParameter() {
        return isFieldOrLocalVariableOrParameter(type);
    }

    public static boolean isFieldOrLocalVariableOrParameter(EnumSymbolType type) {
        return type == EnumSymbolType.SYMBOL_TYPE_CLASS_FIELD || type == EnumSymbolType.SYMBOL_TYPE_METHOD_LOCAL_VARIABLE ||
                type == EnumSymbolType.SYMBOL_TYPE_METHOD_OR_CONSTRUCTOR_PARAMETER;
    }

    public static boolean isVariable(EnumSymbolType type) {
        return type == EnumSymbolType.SYMBOL_TYPE_CLASS_FIELD ||
                type == EnumSymbolType.SYMBOL_TYPE_METHOD_LOCAL_VARIABLE ||
                type == EnumSymbolType.SYMBOL_TYPE_METHOD_OR_CONSTRUCTOR_PARAMETER ||
                type == EnumSymbolType.SYMBOL_TYPE_METHOD_STATEMENT_VARIABLE;
    }

    public static boolean isClassOrEnum(EnumSymbolType type) {
        return type == EnumSymbolType.SYMBOL_TYPE_CLASSORINTERFACE ||
                type == EnumSymbolType.SYMBOL_TYPE_ENUM ||
                type == EnumSymbolType.SYMBOL_TYPE_INNER_CLASS ||
                type == EnumSymbolType.SYMBOL_TYPE_INNER_ENUM;
    }

    public SymbolNode searchVariable(String key) {
        if (parent != null) {
            for (SymbolNode child : parent.children) {
                if (child.isNotMethodAndConstructor() && child.name.equals(key))
                    return child;
            }

            return parent.searchVariable(key);
        }
        if (isNotMethodAndConstructor() && this.name.equals(key))
            return this;

        return null;
    }

    public SymbolNode searchForVariable(String key) {
        if (children != null) {
            for (SymbolNode child : children) {
                if (child.isNotMethodAndConstructor() && child.name.equals(key))
                    return child;
            }
        }

        if (parent != null) {
            return parent.searchForVariable(key);
        }

        if (isNotMethodAndConstructor() && this.name.equals(key))
            return this;

        return null;
    }

    public SymbolNode searchForClassOrInterfaceOrEnum(String key) {
        if (children != null) {
            for (SymbolNode child : children) {
                if (child.isClassOrEnum() && child.name.equals(key))
                    return child;
            }
        }

        if (parent != null) {
            return parent.searchForClassOrInterfaceOrEnum(key);
        }

        if (isClassOrEnum() && this.name.equals(key))
            return this;

        return null;
    }

    public Node getAstNode() {
        return astNode;
    }


    public static SymbolNode getSymbolNode(ClassOrInterfaceDeclaration declaration) {
        return declaration.getData(dataKey);
    }

    public static boolean isStatic(ClassOrInterfaceDeclaration declaration) {
        return getSymbolNode(declaration).isStatic;
    }

    public Node getParentAstNode() {
        return parentAstNode;
    }
}
