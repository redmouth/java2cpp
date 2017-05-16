package co.deepblue.java2cpp.type;

import java.util.*;

/**
 * Created by levin on 17-5-7.
 */
public class TypeConverter {
    static TypeConverter instance;
    HashMap<String, String> primitiveTypeMaps = new HashMap<>();
    HashSet primitiveTypes = new HashSet();
    HashMap<String, String> containerTypes = new HashMap<>();
    HashMap<String, String> simpleEnumTypes = new HashMap<>();

    HashMap<String, String> toUnsignedMap = new HashMap<>();

    public static TypeConverter getInstance() {
        if (instance == null)
            instance = new TypeConverter();
        return instance;
    }

    private TypeConverter() {
        init();
    }

    public void init() {
        toUnsignedMap.put("int", "uint32_t");
        toUnsignedMap.put("short", "uint16_t");
        toUnsignedMap.put("char", "uint16_t");
        toUnsignedMap.put("byte", "unsigned char");
        toUnsignedMap.put("long", "uint64_t");
        toUnsignedMap.put("Integer", "uint32_t");
        toUnsignedMap.put("Short", "uint16_t");
        toUnsignedMap.put("Char", "uint16_t");
        toUnsignedMap.put("Byte", "unsigned char");
        toUnsignedMap.put("Long", "uint64_t");

        //https://android.googlesource.com/platform/libnativehelper/+/jb-mr2-release/include/nativehelper/jni.h
        primitiveTypeMaps.put("boolean", "bool");
        primitiveTypeMaps.put("byte", "int8_t");
        primitiveTypeMaps.put("char", "uint16_t");
        primitiveTypeMaps.put("short", "int16_t");
        primitiveTypeMaps.put("int", "int32_t");
        primitiveTypeMaps.put("long", "int64_t");
        primitiveTypeMaps.put("float", "float");
        primitiveTypeMaps.put("double", "double");

        containerTypes.put("String", "String");
        containerTypes.put("List", "List");
        containerTypes.put("Set", "Set");
        containerTypes.put("HashMap", "HashMap");
        containerTypes.put("Vector", "Vector");
        containerTypes.put("Map", "Map");
        containerTypes.put("LinkedList", "LinkedList");
        containerTypes.put("HashSet", "HashSet");
        containerTypes.put("LinkedHashSet", "LinkedHashSet");
        containerTypes.put("TreeMap", "TreeMap");
        containerTypes.put("IntHashMap", "IntHashMap");
    }

    public String toUnsignedType(String javaType) {
        if (toUnsignedMap.containsKey(javaType))
            return toUnsignedMap.get(javaType);
        return javaType;
    }

    public boolean isContainerType(String javaType) {
        return containerTypes.containsKey(javaType);
    }

    public boolean isBasicTypes(String javaType) {
        return primitiveTypeMaps.containsKey(javaType) || simpleEnumTypes.containsKey(javaType);
    }

    //value of primitive type and simple enums can be pass as function parameter
    public boolean canPassValueAsArgument(String javaType) {
        return primitiveTypeMaps.containsKey(javaType) || simpleEnumTypes.containsKey(javaType);
    }

    public void addSimpleEnumType(String name, String packageName) {
        simpleEnumTypes.put(name, packageName);
    }

    public String mapJava2CppType(String javaType) {
        if (primitiveTypeMaps.containsKey(javaType))
            return primitiveTypeMaps.get(javaType);

        return javaType;
    }
}
