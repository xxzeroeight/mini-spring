package com.minispring.context.fixture;

import com.minispring.annotation.Component;

@Component
public class DependentComponent {
    private final SimpleComponent simpleComponent;

    public DependentComponent(SimpleComponent simpleComponent) {
        this.simpleComponent = simpleComponent;
    }

    public SimpleComponent getSimpleComponent() {
        return simpleComponent;
    }
}
