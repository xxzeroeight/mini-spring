package com.minispring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.CONSTRUCTOR) // 생성자 주입만.
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {
}
