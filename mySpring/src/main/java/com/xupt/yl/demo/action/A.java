package com.xupt.yl.demo.action;

import framework.annotation.MYAutowired;
import framework.annotation.MYService;


/**
 * 测试循环依赖
 */
@MYService
public class A {

    @MYAutowired
    private B b;

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }
}
