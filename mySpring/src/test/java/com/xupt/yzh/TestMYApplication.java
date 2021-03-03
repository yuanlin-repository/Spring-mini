package com.xupt.yzh;

import com.xupt.yl.demo.action.MyAction;
import framework.context.MYApplicationContext;

public class TestMYApplication {
    public static void main(String[] args) throws Exception {
        MYApplicationContext context = new MYApplicationContext("classpath:application.properties");
        MyAction myAction = (MyAction) context.getBean("myAction");
        System.out.println(myAction);
        myAction.test("张三");
    }
}
