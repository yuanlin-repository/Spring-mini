package com.xupt.yzh;

import com.xupt.yl.demo.action.A;
import framework.context.MYApplicationContext;

public class Test {

    public static void main(String[] args) {
        MYApplicationContext context = new MYApplicationContext("classpath:application.properties");
        try {
            /**
             * 测试循环依赖
             */
            A a = (A)context.getBean("a");
            a.getB().print();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
