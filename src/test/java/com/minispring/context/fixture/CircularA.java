package com.minispring.context.fixture;

import com.minispring.annotation.Component;

@Component
public class CircularA {
    public CircularA(CircularB circularB) {}
}
