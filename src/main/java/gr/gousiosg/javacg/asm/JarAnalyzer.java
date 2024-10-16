package gr.gousiosg.javacg.asm;

/**
 * @Author yunlungchia
 * @Date 10/16/24
 */
import org.objectweb.asm.*;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

public class JarAnalyzer {


    /**
     * 你是一个java资深开发工程师
     * 帮忙使用ASM分析本地package.jar文件，生成调用路径，解决接口调用实现断开问题
     */
    public static void main(String[] args) throws IOException {
        String jarPath = "path/to/package.jar";  // 替换为你的jar路径
        Map<String, List<String>> callGraph = analyzeJar(jarPath);
        callGraph.forEach((k, v) -> {
            System.out.println("Class: " + k);
            v.forEach(method -> System.out.println("    calls: " + method));
        });
    }

    public static Map<String, List<String>> analyzeJar(String jarFilePath) throws IOException {
        Map<String, List<String>> callGraph = new HashMap<>();
        try (JarFile jarFile = new JarFile(jarFilePath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    try (InputStream inputStream = jarFile.getInputStream(entry)) {
                        analyzeClass(inputStream, callGraph);
                    }
                }
            }
        }
        return callGraph;
    }

    private static void analyzeClass(InputStream inputStream, Map<String, List<String>> callGraph) throws IOException {
        ClassReader classReader = new ClassReader(inputStream);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);

        for (MethodNode method : classNode.methods) {
            String className = classNode.name.replace("/", ".");
            String methodName = method.name + method.desc;

            List<String> methodCalls = new ArrayList<>();
            InsnList instructions = method.instructions;
            for (int i = 0; i < instructions.size(); i++) {
                AbstractInsnNode insn = instructions.get(i);
                if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL || insn.getOpcode() == Opcodes.INVOKEINTERFACE ||
                        insn.getOpcode() == Opcodes.INVOKESTATIC || insn.getOpcode() == Opcodes.INVOKESPECIAL) {
                    MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
                    String calledMethod = methodInsnNode.owner.replace("/", ".") + "." +
                            methodInsnNode.name + methodInsnNode.desc;
                    methodCalls.add(calledMethod);
                }
            }
            callGraph.put(className + "." + methodName, methodCalls);
        }
    }
}

