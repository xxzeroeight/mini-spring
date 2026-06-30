package com.minispring.context.fixture;

import com.minispring.annotation.Component;

@Component
public class CircularB {
    public CircularB(CircularA circularA) {}
}
