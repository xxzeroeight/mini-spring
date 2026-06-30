package com.minispring.context;

import com.minispring.annotation.Autowired;
import com.minispring.context.exception.BeanCreationException;
import com.minispring.context.exception.CircularDependencyException;
import com.minispring.context.exception.NoSuchBeanException;
import com.minispring.context.exception.NoSuchConstructorException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BeanFactory {
    private final Set<Class<?>> componentClasses;
    private final Map<Class<?>, Object> singletons = new HashMap<>();
    private final Object singletonLock = new Object();
    private final ThreadLocal<Set<Class<?>>> currentlyCreating = ThreadLocal.withInitial(HashSet::new); // 순환 참조 추적.

    public BeanFactory(Set<Class<?>> componentClasses) {
        this.componentClasses = componentClasses;
    }

    public void init() {
        for (Class<?> componentClass : componentClasses) {
            getBean(componentClass);
        }
    }

    // 빈 호출.
    public Object getBean(Class<?> type) {
        Class<?> implClass= findImplementationClass(type);

        synchronized (singletonLock) {
            if (singletons.containsKey(implClass)) {
                return singletons.get(implClass);
            }
            return createBean(implClass); // 없으면 생성.
        }
    }

    // 인터페이스 -> 구현 클래스 찾기.
    // JdbcTodoRepository implements TodoRepository
    private Class<?> findImplementationClass(Class<?> type) {
        if (componentClasses.contains(type)) {
            return type;
        }

        for (Class<?> candidate : componentClasses) {
            if (type.isAssignableFrom(candidate)) { // TodoRepository repo = new JdbcTodoRepository() -> ?
                return candidate;
            }
        }

        throw new NoSuchBeanException("등록된 빈을 찾을 수 없습니다: " + type.getName());
    }

    // 빈 생성.
    private Object createBean(Class<?> implClass) {
        Set<Class<?>> creationPath = currentlyCreating.get();

        if (creationPath.contains(implClass)) {
            throw new CircularDependencyException("순환 참조: " + implClass.getName());
        }

        creationPath.add(implClass);

        try {
            Constructor<?> constructor = findInjectableConstructor(implClass); // 생성자 결정.
            Class<?>[] parameterTypes = constructor.getParameterTypes(); // 파라미터 모음.
            Object[] dependencies = new Object[parameterTypes.length]; // 빈 검색 및 생성.

            for (int i=0; i<parameterTypes.length; i++) {
                dependencies[i] = getBean(parameterTypes[i]);
            }

            Object instance = constructor.newInstance(dependencies); // 리플렉션으로 인스턴스 새로 생성.

            singletons.put(implClass, instance);

            return instance;
        } catch (ReflectiveOperationException e) {
            throw new BeanCreationException("빈 생성 실패: " +  implClass.getName(), e);
        } finally {
            creationPath.remove(implClass);

            if (creationPath.isEmpty()) currentlyCreating.remove();
        }
    }

    // 어떤 생성자를 쓸지 결정.
    private Constructor<?> findInjectableConstructor(Class<?> type) {
        Constructor<?>[] constructors = type.getConstructors(); // public만.

        if (constructors.length == 1) {
            return constructors[0];
        }

        List<Constructor<?>> autowiredConstructors = new ArrayList<>();
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                autowiredConstructors.add(constructor);
            }
        }

        if (autowiredConstructors.size() == 1) return autowiredConstructors.get(0);

        if (autowiredConstructors.isEmpty()) {
            throw new NoSuchConstructorException("생성자가 여러 개인데 @Autowired가 없습니다: " + type.getName());
        }

        // 어떤 걸 써야 할지 모호한 경우.
        throw new NoSuchConstructorException("@Autowired가 2개 이상 붙은 생성자가 존재합니다: " + type.getName());
    }

    // 빈 이름 생성.
    private String resolveBeanName(Class<?> type) {
        String simpleName = type.getSimpleName();

        // TodoService -> todoService
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
}
