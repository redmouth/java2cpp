package co.deepblue.java2cpp.symbol;

/**
 * Created by levin on 17-5-10.
 */
public enum  EnumSymbolType {
    SYMBOL_TYPE_ERROR,
    SYMBOL_PROJECT,
    SYMBOL_PACKAGE,
    SYMBOL_TYPE_ENUM,
    SYMBOL_TYPE_CLASSORINTERFACE,
    SYMBOL_TYPE_CLASS_FIELD,
    SYMBOL_TYPE_CLASS_CONSTRUCTOR,
    SYMBOL_TYPE_CLASS_METHOD,
    SYMBOL_TYPE_METHOD_LOCAL_VARIABLE,
    SYMBOL_TYPE_METHOD_STATEMENT_VARIABLE, // declared in for statement, catch block.
    SYMBOL_TYPE_INNER_CLASS,
    SYMBOL_TYPE_INNER_ENUM,
    SYMBOL_TYPE_METHOD_OR_CONSTRUCTOR_PARAMETER,
    SYMBOL_TYPE_METHOD_CATCH_CLAUSE_PARAMETER,
    SYMBOL_TYPE_NONE,
}
