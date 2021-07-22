package framework.context;

import framework.annotation.MYAutowired;
import framework.annotation.MYController;
import framework.annotation.MYService;
import framework.beans.MYBeanWrapper;
import framework.beans.config.MYBeanDefinition;
import framework.beans.config.MYBeanPostProcessor;
import framework.beans.factory.MYBeanFactory;
import framework.beans.support.MYBeanDefinitionReader;
import framework.beans.support.MYDefaultListableBeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class MYApplicationContext extends MYDefaultListableBeanFactory implements MYBeanFactory {

    /**
     * 配置文件路径 application.properties
     */
    private String[] configLocations;

    /**
     * 用于读取配置
     */
    private MYBeanDefinitionReader reader;

    /**
     * 存放beanWrapper
     */
    private Map<String, MYBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<String, MYBeanWrapper>();

    /**
     * 存放单例bean实例
     */
    private Map<String, Object> factoryBeanObejctCache = new ConcurrentHashMap<String, Object>();

    /**
     * 调用refresh方法初始化容器
     * @param configLocations
     */
    public MYApplicationContext(String... configLocations) {
        this.configLocations = configLocations;
        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化容器
     * @throws Exception
     */
    @Override
    protected void refresh() throws Exception {
        // 1. 加载配置文件到BeanDefinition
        reader = new MYBeanDefinitionReader(configLocations);
        List<MYBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();
        // 2. 将配置信息注册到容器
        doRegisterBeanDefiniton(beanDefinitions);
        // 3. 初始化非懒加载实例
        doAutowired();
    }

    /**
     * 初始化非懒加载的bean
     */
    private void doAutowired() {
        for (Map.Entry<String, MYBeanDefinition> entry : super.beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            // 判断是否懒加载
            if (!entry.getValue().isLazyInit()) {
                try {
                    getBean(beanName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 实际注册BeanDefinition
     * @param beanDefinitions
     * @throws Exception
     */
    private void doRegisterBeanDefiniton(List<MYBeanDefinition> beanDefinitions) throws Exception {
        for (MYBeanDefinition beanDefinition : beanDefinitions) {
            if (beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("【DEBUG】---- beanDefinition" + beanDefinition.getFactoryBeanName() + "已经存在!");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
        }
    }

    /**
     * 获取bean实例
     *      调用getBean的时机
     *      1.DispatchServlet创建IOC容器：refresh --> doAutowired
     *      2.DispatchServlet创建HandlerMapping，要拿出所有Bean
     *      3.手动getBean
     * @param beanName bean名称
     * @return bean实例
     * @throws Exception
     */
    @Override
    public Object getBean(String beanName) throws Exception {
        MYBeanDefinition myBeanDefinition = null;
        myBeanDefinition = this.beanDefinitionMap.get(beanName);
        // 如果已经初始化，则直接获取
        Object instance = getSingleton(beanName);
        if (instance != null) {
            return instance;
        }
        // 前置事件处理器
        MYBeanPostProcessor beanPostProcessor = new MYBeanPostProcessor();
        // 在实例化bean之前进行一些操作
        beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
        // 没有这个bean则抛出异常
        if (myBeanDefinition == null) {
            throw new Exception("This Bean not exists");
        }
        // 将Bean封装成BeanWrapper
        MYBeanWrapper beanWrapper = instantiteBean(beanName, myBeanDefinition);
        // 将拿到的BeanWrapper放入IOC容器
        this.factoryBeanInstanceCache.put(beanName, beanWrapper);
        // 后置事件处理器
        beanPostProcessor.postProcessAfterInitialization(instance, beanName);
        // 依赖注入, 对bean进行初始化, 反射注入bean的各项属性
        populateBean(beanName, myBeanDefinition, beanWrapper);
        // 即使是单例模式有单例IOC容器，但获取Instance也要先封装为BeanWrapper，然后在通用容器中取
        return this.factoryBeanObejctCache.get(beanName);
    }

    /**
     * 反射实现依赖注入
     * @param beanName bean的名称
     * @param myBeanDefinition beanDefinition信息
     * @param myBeanWrapper bean的beanWrapper
     */
    private void populateBean(String beanName, MYBeanDefinition myBeanDefinition, MYBeanWrapper myBeanWrapper) {
        Class<?> clazz = myBeanWrapper.getWrappedClass();
        Object instance = myBeanWrapper.getWrappedInstance();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(MYAutowired.class)) {
                continue;
            }
            MYAutowired annotation = field.getAnnotation(MYAutowired.class);
            // 获取自定义beanName
            String autowiredBeanName = annotation.value().trim();
            // 如果没有自定义beanName则获取依赖bean的全限定类名
            if ("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }
            field.setAccessible(true);
            try {
                String autowiredBeanFactoryName = beanDefinitionMap.get(autowiredBeanName).getFactoryBeanName();
                Object dependBean = this.factoryBeanObejctCache.get(autowiredBeanFactoryName);
                // 如果依赖的bean还没有初始化, 先初始化依赖的bean
                if(dependBean == null) {
                    dependBean = this.factoryBeanInstanceCache.get(autowiredBeanName);
                    if (dependBean == null) {
                        dependBean = getBean(autowiredBeanName);
                    } else {
                        dependBean = ((MYBeanWrapper)dependBean).getWrappedInstance();
                    }
                }
                // 反射注入
                field.set(instance, dependBean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.factoryBeanInstanceCache.remove(beanName);
        this.factoryBeanObejctCache.put(myBeanDefinition.getFactoryBeanName(), instance);
        this.factoryBeanObejctCache.put(myBeanDefinition.getBeanClassName(), instance);
    }

    /**
     * 实例化bean(尚未初始化)
     * @param beanName
     * @param myBeanDefinition
     * @return beanWrapper(包含尚未初始化的bean)
     */
    private MYBeanWrapper instantiteBean(String beanName, MYBeanDefinition myBeanDefinition) {
        // 1. 拿到类的全限定类名
        String className = myBeanDefinition.getBeanClassName();
        // 2. 通过反射进行实例化
        Object instance = null;
        try {
            Class<?> clazz = Class.forName(className);
            instance = clazz.newInstance();
            //------------------------- AOP部分入口代码 -----------------------
            // TODO
            //---------------------------------------------------------------
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 3. 封装BeanWrapper
        MYBeanWrapper beanWrapper = new MYBeanWrapper(instance);
        this.factoryBeanInstanceCache.put(myBeanDefinition.getFactoryBeanName(), beanWrapper);
        this.factoryBeanInstanceCache.put(className, beanWrapper);
        return beanWrapper;
    }

    /**
     * 从容器中直接获取bean
     * @param beanName bean名称
     * @return bean实例
     */
    public Object getSingleton(String beanName) {
        Object o = this.factoryBeanObejctCache.get(beanName);
        if (o != null) {
            return o;
        }
        return null;
    }

    /**
     * 通过class获取bean
     * @param beanCLass
     * @return bean实例
     * @throws Exception
     */
    @Override
    public Object getBean(Class<?> beanCLass) throws Exception {
        return null;
    }

    /**
     * 返回beanDefinitionName集合
     * @return beanDefinitionName集合
     */
    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig() {
        return this.reader.getConfig();
    }
}
