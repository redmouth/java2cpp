package co.deepblue.java2cpp.processor;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.Modifier;

import java.util.EnumSet;

/**
 * Created by levin on 17-5-8.
 */
public class ModifierProcessor {
    public String staticMod;
    public String accessLevel;
    public boolean isStatic;
    public boolean isPublic;

    public void processModifiers(EnumSet<Modifier> modifiers) {
        AccessSpecifier accessModifier = Modifier.getAccessSpecifier(modifiers);
        accessLevel = accessModifier.asString();
        if (accessModifier == AccessSpecifier.DEFAULT) //In cpp, the default access level assigned to members of a class is private.
            accessLevel = "private";
        else if (accessModifier == AccessSpecifier.PUBLIC)
            isPublic = true;

        staticMod = "";
        for (Modifier modifier : modifiers) {
            if (modifier == Modifier.STATIC) {
                staticMod = "static ";
                isStatic = true;
            }
        }
    }
}
