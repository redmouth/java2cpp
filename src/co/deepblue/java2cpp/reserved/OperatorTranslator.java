package co.deepblue.java2cpp.reserved;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by levin on 17-5-13.
 */
public class OperatorTranslator {
    static OperatorTranslator instance;
    HashMap<String, String> operatorMap = new HashMap<>();

    //http://en.cppreference.com/w/cpp/keyword
    HashSet<String> reservedKeywords = new HashSet<>();

    public static OperatorTranslator getInstance() {
        if (instance == null)
            instance = new OperatorTranslator();
        return instance;
    }

    private OperatorTranslator() {
        operatorMap.put(">>>", ">>");

        reservedKeywords.add("alignas");
        reservedKeywords.add("alignof");
        reservedKeywords.add("and");
        reservedKeywords.add("and_eq");
        reservedKeywords.add("asm");
        reservedKeywords.add("atomic_cancel");
        reservedKeywords.add("atomic_commit");
        reservedKeywords.add("atomic_noexcept");
        reservedKeywords.add("auto");
        reservedKeywords.add("bitand");
        reservedKeywords.add("bitor");
        reservedKeywords.add("bool");
        reservedKeywords.add("break");
        reservedKeywords.add("case");
    }

    public String translateOperator(String javaOp) {
        if (operatorMap.containsKey(javaOp))
            return operatorMap.get(javaOp);

        return javaOp;
    }
}
