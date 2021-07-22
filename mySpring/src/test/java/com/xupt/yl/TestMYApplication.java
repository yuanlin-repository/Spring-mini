package com.xupt.yl;

import com.xupt.yl.demo.action.MyAction;
import framework.context.MYApplicationContext;

public class TestMYApplication {
    /**
     * 测试- ioc容器获取bean实例
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        MYApplicationContext context = new MYApplicationContext("classpath:application.properties");
        MyAction myAction = (MyAction) context.getBean("myAction");
        System.out.println(myAction);
        myAction.test("张三");
    }
}
