package com.minispring.context.fixture;

import com.minispring.annotation.Component;

@Component
public class MultiConstructorNoAutowired {
    public MultiConstructorNoAutowired() {}
    public MultiConstructorNoAutowired(SimpleComponent simpleComponent) {}
}
