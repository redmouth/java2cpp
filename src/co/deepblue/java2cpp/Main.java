package co.deepblue.java2cpp;

/**
 * Created by levin on 17-5-6.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("number of args:" + args.length);
        if (args.length < 3)
            return;

        String javaPackageName = args[1];
        String javaSrcDir = args[2];
        String cppDstDir = args[3];
        String cppNameSpace = "exampleCppNamespace";
        Java2CppMemory j2cpp = new Java2CppMemory(javaPackageName,
                javaSrcDir,
                cppDstDir,
                cppNameSpace);
        j2cpp.startParse();
    }
}
