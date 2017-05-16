package co.deepblue.java2cpp.symbol;

import co.deepblue.java2cpp.processor.AstNodeHelper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

/**
 * Created by levin on 17-5-10.
 */
public class SymbolNodeHelper {
    public static String queryFileName(SymbolNode symbolNode, CompilationUnit unit) {
        String scope = "";

        Node node = symbolNode.astNode;
        while (true) {
        //while (node != null && node.astNode != unit) {
            if (node.getParentNode().isPresent()) {
                node = node.getParentNode().get();
                if (node == unit)
                    return scope;
                SymbolNode sn = node.getData(SymbolNode.dataKey);
                if (sn != null)
                    scope = sn.name + "::" + scope;
            }
            else
                return null;
        }
    }

    public static String queryFileName(SymbolNode symbolNode) {
        Node node = symbolNode.astNode;
        while (true) {
            //while (node != null && node.astNode != unit) {
            if (node.getParentNode().isPresent()) {
                node = node.getParentNode().get();
                if (node instanceof CompilationUnit) {
                    return node.getData(AstNodeHelper.FilenameDatakey);
                }
            }
            else
                return null;
        }
    }
}

