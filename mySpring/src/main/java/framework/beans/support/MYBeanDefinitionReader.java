package framework.beans.support;

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

    // 保存所有 Bean 的 class 信息（全限定类名）
    private List<String> registerBeanClasses = new ArrayList<String>();

    private Properties config = new Properties();

    // 定义Properties文件中要扫描包的key
    private final String SCAN_PACKAGE = "scanPackage";

    public MYBeanDefinitionReader(String... locations) {
        // 得到properties文件的IO流
        InputStream is = null;
        is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:", ""));

        // 通过Properties类加载IO流，最后关闭IO流
        try {
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
        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    private void doScanner(String scanPackage) {
        // 1.获取需要扫描包的File对象
        // 这里通过 getResource 方法返回的 URL 对象（指明scanPackage的绝对路径）来创建File
        URL url = this.getClass().getResource("/"+ scanPackage.replaceAll("\\.", "/"));
        File classpath = null;
        try {
            classpath = new File(URLDecoder.decode(url.getFile(), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            System.out.println("【DEBUG】----获取scanPackage路径乱码");
        }

        // 遍历文件夹，寻找class文件
        for (File file : classpath.listFiles()) {
            if (file.isDirectory()) {
                //  这里通过递归遍历文件夹
                doScanner(scanPackage + "." + file.getName());
            } else {
                // 不是class文件不管
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

    public List<MYBeanDefinition> loadBeanDefinitions() {
        // 保存创建所有 BeanDefinition
        List<MYBeanDefinition> result = new ArrayList<MYBeanDefinition>();
        try {
            for (String className : registerBeanClasses) {
                // 加载类
                Class<?> clazz = Class.forName(className);
                // 接口不能实例化，不处理 factoryBean
                if (clazz.isInterface()) {
                    continue;
                }

                // 一个Class可以对应多个BeanDefinition
                // 一个BeanDefinition对应一个Bean
                // 一个Bean对应多个beanName
                // 1.类
                result.add(doCreateBeanDefinition(toLowerFirstCase(clazz.getSimpleName()), clazz.getName()));
                // 2.接口
                Class<?>[] interfaces = clazz.getInterfaces();
                for (Class<?> i : interfaces) {
                    // 若一个接口有多个实现类，这里就会覆盖
                    // 可以通过注入Bean时指定name解决
                    result.add(doCreateBeanDefinition(i.getName(), clazz.getName()));
                }
                // 3.自定义BeanName
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    private MYBeanDefinition doCreateBeanDefinition(String factoryBeanName, String beanClassName) {

        MYBeanDefinition myBeanDefinition = new MYBeanDefinition();
        // 设置全限定类名
        myBeanDefinition.setBeanClassName(beanClassName);
        // 设置唯一标识
        myBeanDefinition.setFactoryBeanName(factoryBeanName);
        return myBeanDefinition;
    }

    // 将第一个字母转为小写
    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public Properties getConfig() {
        return config;
    }

    public static void main(String[] args) {
        MYBeanDefinitionReader reader = new MYBeanDefinitionReader("classpath:application.properties");
        reader.loadBeanDefinitions();
    }
}
