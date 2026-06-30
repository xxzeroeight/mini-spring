package com.minispring.context;

import com.minispring.context.exception.CircularDependencyException;
import com.minispring.context.exception.NoSuchBeanException;
import com.minispring.context.exception.NoSuchConstructorException;
import com.minispring.context.fixture.CircularA;
import com.minispring.context.fixture.CircularB;
import com.minispring.context.fixture.DependentComponent;
import com.minispring.context.fixture.MultiConstructorAmbiguous;
import com.minispring.context.fixture.MultiConstructorNoAutowired;
import com.minispring.context.fixture.MultiConstructorWithAutowired;
import com.minispring.context.fixture.SampleRepository;
import com.minispring.context.fixture.SampleRepositoryImpl;
import com.minispring.context.fixture.SimpleComponent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BeanFactoryTest {
    @Nested
    @DisplayName("기본 생성/주입 - 단순 생성, 의존성 주입, 싱글톤 동일성")
    class BasicBeanCreation {
        @Test
        void createsSimpleComponentSuccessfully() {
            BeanFactory beanFactory = new BeanFactory(Set.of(SimpleComponent.class));

            Object bean = beanFactory.getBean(SimpleComponent.class);

            assertNotNull(bean);
        }

        @Test
        void injectsDependencyIntoConstructor() {
            BeanFactory beanFactory = new BeanFactory(Set.of(SimpleComponent.class, DependentComponent.class));

            DependentComponent bean = (DependentComponent) beanFactory.getBean(DependentComponent.class);

            assertNotNull(bean.getSimpleComponent());
        }

        @Test
        void returnsSameInstanceOnRepeatedLookup() {
            BeanFactory beanFactory = new BeanFactory(Set.of(SimpleComponent.class));

            Object first = beanFactory.getBean(SimpleComponent.class);
            Object second = beanFactory.getBean(SimpleComponent.class);

            assertSame(first, second);
        }

        @Test
        void sharesSameSingletonInstanceAcrossInjectionAndDirectLookup() {
            BeanFactory beanFactory = new BeanFactory(Set.of(SimpleComponent.class, DependentComponent.class));

            SimpleComponent directLookup = (SimpleComponent) beanFactory.getBean(SimpleComponent.class);
            DependentComponent dependent = (DependentComponent) beanFactory.getBean(DependentComponent.class);

            assertSame(directLookup, dependent.getSimpleComponent());
        }
    }

    @Nested
    @DisplayName("인터페이스 매칭 - 인터페이스로 조회 시 구현체 반환")
    class InterfaceResolution {
        @Test
        void resolvesImplementationWhenLookedUpByInterface() {
            BeanFactory beanFactory = new BeanFactory(Set.of(SampleRepositoryImpl.class));

            Object bean = beanFactory.getBean(SampleRepository.class);

            assertEquals(SampleRepositoryImpl.class, bean.getClass());
        }
    }

    @Nested
    @DisplayName("생성자 선택 규칙 - @Autowired 없음/1개/2개 이상")
    class ConstructorSelection {
        @Test
        void throwsWhenMultipleConstructorsExistWithoutAutowired() {
            BeanFactory beanFactory = new BeanFactory(Set.of(MultiConstructorNoAutowired.class));

            assertThrows(NoSuchConstructorException.class,
                    () -> beanFactory.getBean(MultiConstructorNoAutowired.class));
        }

        @Test
        void selectsConstructorAnnotatedWithAutowired() {
            BeanFactory beanFactory = new BeanFactory(Set.of(SimpleComponent.class, MultiConstructorWithAutowired.class));

            MultiConstructorWithAutowired bean =
                    (MultiConstructorWithAutowired) beanFactory.getBean(MultiConstructorWithAutowired.class);

            assertNotNull(bean.getSimpleComponent());
        }

        @Test
        void throwsWhenMultipleConstructorsAreAnnotatedWithAutowired() {
            BeanFactory beanFactory = new BeanFactory(Set.of(MultiConstructorAmbiguous.class));

            assertThrows(NoSuchConstructorException.class,
                    () -> beanFactory.getBean(MultiConstructorAmbiguous.class));
        }
    }

    @Nested
    @DisplayName("예외 상황 - 순환 참조, 미등록 빈")
    class ExceptionHandling {
        @Test
        void throwsOnCircularDependency() {
            BeanFactory beanFactory = new BeanFactory(Set.of(CircularA.class, CircularB.class));

            assertThrows(CircularDependencyException.class,
                    () -> beanFactory.getBean(CircularA.class));
        }

        @Test
        void throwsWhenBeanIsNotRegistered() {
            BeanFactory beanFactory = new BeanFactory(Set.of(SimpleComponent.class));

            assertThrows(NoSuchBeanException.class,
                    () -> beanFactory.getBean(DependentComponent.class));
        }
    }
}
