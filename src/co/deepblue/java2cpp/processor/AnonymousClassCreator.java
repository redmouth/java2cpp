package co.deepblue.java2cpp.processor;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;

/**
 * Created by levin on 17-5-8.
 */
public class AnonymousClassCreator {

    public static String create(NodeList<BodyDeclaration<?>> anonymousBody, String baseType) {
        for (BodyDeclaration<?> body : anonymousBody) {
        }
        return "";
    }
}
