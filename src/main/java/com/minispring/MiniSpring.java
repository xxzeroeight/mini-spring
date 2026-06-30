package com.minispring;

import com.minispring.context.BeanFactory;
import com.minispring.context.ComponentScanner;

import java.util.Set;

public class MiniSpring {
    private MiniSpring() {}

    public static void run(Class<?> primarySource, int port) {
        String basePackage = primarySource.getPackageName();

        ComponentScanner scanner = new ComponentScanner(basePackage);
        Set<Class<?>> components = scanner.scan();

        BeanFactory beanFactory = new BeanFactory(components);
        beanFactory.init();
    }
}
