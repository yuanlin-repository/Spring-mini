package com.xupt.yl.beans.support;

import framework.beans.support.MYBeanDefinitionReader;

public class MYBeanDefinitionReaderTest {

    /**
     * 测试方法-从properties文件加载bean信息
     * @param args
     */
    public static void main(String[] args) {
        MYBeanDefinitionReader reader = new MYBeanDefinitionReader("classpath:application.properties");
        reader.loadBeanDefinitions();
    }
}
