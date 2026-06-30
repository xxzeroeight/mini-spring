package com.minispring.context.fixture;

import com.minispring.annotation.Autowired;
import com.minispring.annotation.Component;

@Component
public class MultiConstructorAmbiguous {
    @Autowired
    public MultiConstructorAmbiguous() {}

    @Autowired
    public MultiConstructorAmbiguous(SimpleComponent simpleComponent) {}
}
