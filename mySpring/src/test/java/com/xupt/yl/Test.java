package com.xupt.yl;

import com.xupt.yl.demo.action.A;
import framework.context.MYApplicationContext;

public class Test {

    /**
     * 测试- a-b b-a 循环依赖
     * @param args
     */
    public static void main(String[] args) {
        MYApplicationContext context = new MYApplicationContext("classpath:application.properties");
        try {
            A a = (A)context.getBean("a");
            a.getB().print();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
