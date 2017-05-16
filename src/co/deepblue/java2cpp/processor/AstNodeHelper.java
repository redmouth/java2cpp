package co.deepblue.java2cpp.processor;

import co.deepblue.java2cpp.symbol.SymbolNode;
import co.deepblue.java2cpp.symbol.EnumSymbolType;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;

import java.util.List;

/**
 * Created by levin on 17-5-10.
 */
public class AstNodeHelper {
    public static DataKey<String> FilenameDatakey = new DataKey<String>() { };

    public static boolean definedInCurrentCompilationUnit(ClassOrInterfaceType type) {
        SymbolNode symbolNode = getAncestorSymbolNode(type);
        if (symbolNode != null) {
            SymbolNode found = symbolNode.searchForClassOrInterfaceOrEnum(type.getNameAsString());
            if (found != null) {
                if (AstNodeHelper.getCompilationUnit(found.getAstNode()) == AstNodeHelper.getCompilationUnit(type))
                    return true;
            }
        }
        return false;
    }

    public static Node getAstTypeNode(NameExpr node) {
        SymbolNode symbolNode = getAncestorSymbolNode(node);
        if (symbolNode != null) {
            SymbolNode found = symbolNode.searchForVariable(node.getNameAsString());
            if (found != null)  {
                return found.getAstNode();
            }
        }
        return null;
    }

    public static EnumSymbolType getSymbolType(NameExpr node) {
        SymbolNode symbolNode = getAncestorSymbolNode(node);
        if (symbolNode != null) {
            SymbolNode found = symbolNode.searchForVariable(node.getNameAsString());
            if (found != null)
                return found.getType();
        }
        return EnumSymbolType.SYMBOL_TYPE_ERROR;
    }

    public static EnumSymbolType getSymbolType(ClassOrInterfaceType node) {
        SymbolNode symbolNode = getAncestorSymbolNode(node);
        if (symbolNode != null) {
            SymbolNode found = symbolNode.searchForVariable(node.getNameAsString());
            if (found != null)
                return found.getType();
        }
        return EnumSymbolType.SYMBOL_TYPE_ERROR;
    }

    public static boolean isArrayType(NameExpr node) {
        SymbolNode symbolNode = getAncestorSymbolNode(node);
        if (symbolNode != null) {
            SymbolNode found = symbolNode.searchForVariable(node.getNameAsString());
            if (found != null) {
                Node astNode = found.getAstNode();
                if (astNode instanceof VariableDeclarator) {
                    Type type = ((VariableDeclarator) astNode).getType();
                    if (type instanceof ArrayType) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static Type getArrayComponentType(NameExpr node) {
        SymbolNode symbolNode = getAncestorSymbolNode(node);
        if (symbolNode != null) {
            SymbolNode found = symbolNode.searchForVariable(node.getNameAsString());
            if (found != null) {
                Node astNode = found.getAstNode();
                if (astNode instanceof VariableDeclarator) {
                    Type type = ((VariableDeclarator) astNode).getType();
                    if (type instanceof ArrayType) {
                        return  ((ArrayType) type).getComponentType();
                    }
                }
            }
        }
        return new PrimitiveType();
    }

    public static SymbolNode getAncestorSymbolNode(Node node) {
        if (node == null)
            return null;

        SymbolNode symbolNode = node.getData(SymbolNode.dataKey);
        if (symbolNode != null)
            return symbolNode;

        if (node.getParentNode().isPresent())
            return getAncestorSymbolNode(node.getParentNode().get());
        return null;
    }

    public static String getScope(BodyDeclaration constructor) {
        String scopeStr = "";
        Node node = constructor.getParentNode().get();
        while (true) {
            if (!(node instanceof CompilationUnit)) {
                String name = "";
                if (node instanceof ClassOrInterfaceDeclaration)
                    name = ((ClassOrInterfaceDeclaration) node).getNameAsString();
                else if (node instanceof EnumDeclaration)
                    name = ((EnumDeclaration) node).getNameAsString();

                if (!name.isEmpty())
                    scopeStr = name + "::" + scopeStr;
            } else
                break;

            if (node.getParentNode().isPresent())
                node = node.getParentNode().get();
            else
                break;
        }
        return scopeStr;
    }


    public static CompilationUnit getCompilationUnit(BodyDeclaration constructor) {
        Node node = constructor.getParentNode().get();
        while (true) {
            if (node instanceof CompilationUnit) {
                return (CompilationUnit) node;
            }

            if (node.getParentNode().isPresent())
                node = node.getParentNode().get();
            else
                break;
        }

        return null;
    }

    public static CompilationUnit getCompilationUnit(Node leaf) {
        Node node = leaf.getParentNode().get();
        while (true) {
            if (node instanceof CompilationUnit) {
                return (CompilationUnit) node;
            }

            if (node.getParentNode().isPresent())
                node = node.getParentNode().get();
            else
                break;
        }

        return null;
    }

    public static String getFileName(Node leaf) {
        CompilationUnit cu = getCompilationUnit(leaf);
        return cu.getData(FilenameDatakey);
    }



    public static boolean containsDefaultConstructor(List<ConstructorDeclaration> constructorList) {
        if (constructorList.size() > 0) {
            for (ConstructorDeclaration constructor : constructorList) {
                if (AstNodeHelper.isDefaultConstructor(constructor))
                    return true;
            }
        }
        return false;
    }

    static boolean isDefaultConstructor (ConstructorDeclaration constructor) {
        NodeList<Parameter> parameters = constructor.getParameters();
        return parameters.size() <= 0;
    }
}
