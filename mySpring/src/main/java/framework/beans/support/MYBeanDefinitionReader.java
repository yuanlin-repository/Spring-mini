package framework.beans.support;

import framework.annotation.MYComponent;
import framework.annotation.MYController;
import framework.annotation.MYService;
import framework.beans.config.MYBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MYBeanDefinitionReader {

    /**
     * 保存所有bean的全限定类名
     */
    private List<String> registerBeanClasses = new ArrayList<String>();

    private Properties config = new Properties();

    /**
     * 扫描路径
     */
    private final String SCAN_PACKAGE = "scanPackage";

    public MYBeanDefinitionReader(String... locations) {
        // 获取配置文件io流
        InputStream is = null;
        is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:", ""));
        try {
            // 加载配置信息
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // 扫描scanPackage下的配置(注解)
        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    /**
     * 扫描包，读取注解配置
     * @param scanPackage 扫描路径
     */
    private void doScanner(String scanPackage) {
        // 1. 创建扫描包的File对象
        // 这里通过getResource方法返回的URL对象（指明scanPackage的绝对路径）来创建File
        URL url = this.getClass().getResource("/"+ scanPackage.replaceAll("\\.", "/"));
        File classpath = null;
        try {
            // 需要用URLDecoder进行解码，否则会出现乱码
            classpath = new File(URLDecoder.decode(url.getFile(), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            System.out.println("【DEBUG】----获取scanPackage路径乱码");
        }

        // 2. 递归扫描包, 保留.class文件
        for (File file : classpath.listFiles()) {
            if (file.isDirectory()) {
                // 递归遍历文件夹
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith("class")) {
                    continue;
                }
                // 保存全限定类名，后面通过Class.forName获取Class对象
                String className = (scanPackage + "." + file.getName().replace(".class", ""));
                // 保存到list中
                registerBeanClasses.add(className);
            }
        }
    }

    /**
     * 读取配置文件中配置
     * @return BeanDefinition配置信息集合
     */
    public List<MYBeanDefinition> loadBeanDefinitions() {
        List<MYBeanDefinition> result = new ArrayList<MYBeanDefinition>();
        try {
            for (String className : registerBeanClasses) {
                // 加载类
                Class<?> clazz = Class.forName(className);
                // 不处理FactoryBean
                if (clazz.isInterface()) {
                    continue;
                }
                // 如果不是IOC容器管理则直接跳过
                if (!clazz.isAnnotationPresent(MYService.class) &&
                        !clazz.isAnnotationPresent(MYController.class) &&
                                !clazz.isAnnotationPresent(MYComponent.class)) {
                    continue;
                }
                // 创建BeanDefinition注入
                result.add(doCreateBeanDefinition(toLowerFirstCase(clazz.getSimpleName()), clazz.getName()));
                Class<?>[] interfaces = clazz.getInterfaces();
                for (Class<?> i : interfaces) {
                    result.add(doCreateBeanDefinition(i.getName(), clazz.getName()));
                }
                // 3.自定义BeanName
                // TODO
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 实际创建BeanDefinition
     * @param factoryBeanName 唯一标识
     * @param beanClassName 全限定类名
     * @return
     */
    private MYBeanDefinition doCreateBeanDefinition(String factoryBeanName, String beanClassName) {

        MYBeanDefinition myBeanDefinition = new MYBeanDefinition();
        // 设置全限定类名
        myBeanDefinition.setBeanClassName(beanClassName);
        // 设置唯一标识
        myBeanDefinition.setFactoryBeanName(factoryBeanName);
        return myBeanDefinition;
    }

    /**
     * 将第一个字母转为小写
     */
    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public Properties getConfig() {
        return config;
    }

}
