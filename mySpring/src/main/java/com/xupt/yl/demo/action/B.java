package com.xupt.yl.demo.action;

import framework.annotation.MYAutowired;
import framework.annotation.MYService;

/**
 * 测试循环依赖
 */
@MYService
public class B {
    @MYAutowired
    private A a;

    public void print() {
        System.out.println("获取到A");
    }
}
