package com.minispring.context;

import com.minispring.annotation.Component;
import com.minispring.context.exception.ComponentScanException;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ComponentScanner {
    private final String basePackage;

    public ComponentScanner(String basePackage) {
        this.basePackage = basePackage;
    }

    public Set<Class<?>> scan() {
        Set<Class<?>> classes = new HashSet<>();
        String path = basePackage.replace(".", "/"); // 패키지(.) -> 파일, 클래스패스(/)

        try {
            // 클래스패스에서 베이스 패키지에 해당하는 모든 위치를 URL로.
            Enumeration<URL> resources = Thread.currentThread()
                    .getContextClassLoader()
                    .getResources(path);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();

                if (resource.getProtocol().equals("file")) {
                    scanFromFileSystem(resource, classes);
                } else if (resource.getProtocol().equals("jar")) {
                    scanFromJar(resource, classes);
                }
            }
        } catch (IOException e) {
            throw new ComponentScanException("클래스패스 스캔 실패: " + basePackage, e);
        }

        return classes;
    }

    // jar 탐색.
    private void scanFromJar(URL resource, Set<Class<?>> classes) throws IOException {
        String jarPath = resource.getPath();
        jarPath = jarPath.substring(5, jarPath.indexOf("!")); // file:/path/to/app.jar! -> /path/to/app.jar

        try (JarFile jar = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jar.entries(); // jar 안의 모든 파일/패키지 순회.
            String pathPrefix = basePackage.replace(".", "/");

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                // com/minispring/app/todo/TodoService.class -> com.minispring.app.todo.TodoService
                if (name.startsWith(pathPrefix) && name.endsWith(".class")) {
                    String className = name.replace("/", ".").replace(".class", "");
                    loadIfComponent(className, classes);
                }
            }
        }
    }

    // 파일 탐색.
    private void scanFromFileSystem(URL resource, Set<Class<?>> classes) {
        File directory = new File(resource.getFile());
        scanDirectory(directory, basePackage, classes);
    }

    // 패키지 탐색.
    private void scanDirectory(File directory, String packageName, Set<Class<?>> classes) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) { // com.minispring.app -> com.minispring.app.todo
                scanDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) { // com.minispring.app.todo.TodoService
                String className = packageName + '.' + file.getName().replace(".class", "");
                loadIfComponent(className, classes);
            }
        }
    }

    // 실제로 클래스 로드하고 필터링.
    private void loadIfComponent(String className, Set<Class<?>> classes) {
        try {
            Class<?> cls = Class.forName(className); // 메모리 로드.

            if (isComponent(cls)) classes.add(cls); // Set 추가.
        } catch (ClassNotFoundException e) {
            throw new ComponentScanException("클래스 로드 실패: " + className, e);
        }
    }

    // @Component 판별.
    private boolean isComponent(Class<?> cls) {
        if (cls.isAnnotationPresent(Component.class)) return true; // @Component인지 확인.

        // 메타 어노테이션 확인.
        for (Annotation annotation : cls.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Component.class)) return true;
        }

        return false;
    }
}
