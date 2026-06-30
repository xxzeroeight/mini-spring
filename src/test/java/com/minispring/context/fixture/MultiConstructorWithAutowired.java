package com.minispring.context.fixture;

import com.minispring.annotation.Autowired;
import com.minispring.annotation.Component;

@Component
public class MultiConstructorWithAutowired {
    private final SimpleComponent simpleComponent;

    public MultiConstructorWithAutowired() {
        this.simpleComponent = null;
    }

    @Autowired
    public MultiConstructorWithAutowired(SimpleComponent simpleComponent) {
        this.simpleComponent = simpleComponent;
    }

    public SimpleComponent getSimpleComponent() {
        return simpleComponent;
    }
}
