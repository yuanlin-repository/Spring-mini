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

    private String[] configLocations;
    private MYBeanDefinitionReader reader;

    // 通用IOC容器，存的是 BeanWrapper
    private Map<String, MYBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<String, MYBeanWrapper>();
    // 单例IOC容器，存的是实例对象（相当于缓存，避免重复创建Bean）
    private Map<String, Object> factoryBeanObejctCache = new ConcurrentHashMap<String, Object>();

    // 传入配置文件，调用refresh方法
    public MYApplicationContext(String... configLocations) {
        this.configLocations = configLocations;
        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void refresh() throws Exception {
        // 1.定位配置文件
        reader = new MYBeanDefinitionReader(configLocations);
        // 2.加载，加载配置文件到内存（BeanDefinition）
        List<MYBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();
        // 3.注册，注册到配置信息到容器里面（伪IOC容器）
        doRegisterBeanDefiniton(beanDefinitions);
        // 4.把不是延迟加载的类提前初始化
        doAutowired();
    }

    private void doAutowired() {
        for (Map.Entry<String, MYBeanDefinition> entry : super.beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            // beanDefinition默认isLazyInit是false，会提前将bean放入IOC容器
            if (!entry.getValue().isLazyInit()) {
                try {
                    getBean(beanName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doRegisterBeanDefiniton(List<MYBeanDefinition> beanDefinitions) throws Exception {
        for (MYBeanDefinition beanDefinition : beanDefinitions) {
            if (super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("【DEBUG】---- beanDefinition" + beanDefinition.getFactoryBeanName() + "已经存在!");
            }
            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
            super.beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
        }

    }

    @Override
    // 依赖注入
    // 1.读取BeanDefinition，通过反射创建Bean实例，包装成BeanWrapper，并放入IOC容器
    // 2.对IOC容器管理的Bean进行依赖注入
    /**
     * 调用getBean的时机
     * 1.DispatchServlet创建IOC容器：refresh --> doAutowired
     * 2.DispatchServlet创建HandlerMapping，要拿出所有Bean
     * 3.手动getBean
     */
    public Object getBean(String beanName) throws Exception {
        MYBeanDefinition myBeanDefinition = this.beanDefinitionMap.get(beanName);
        Object instance = getSingleton(beanName);

        // 如果已经初始化，则直接获取
        if (instance != null) {
            return instance;
        }
        // 否则进行相关逻辑
        // 创建前置事件处理器
        MYBeanPostProcessor beanPostProcessor = new MYBeanPostProcessor();

        // 在实例化bean之前进行一些操作
        beanPostProcessor.postProcessBeforeInitialization(instance, beanName);

        // 判断是否是Spring管理的对象
        // 只有Spring管理的对象才有BeanDefinition
        if (myBeanDefinition == null) {
            throw new Exception("This Bean not exists");
        }

        // 1.所有Bean都要封装成BeanWrapper，然后在BeanWrapper中再取出Bean实例
        MYBeanWrapper beanWrapper = instantiteBean(beanName, myBeanDefinition);

        // 2.将拿到的BeanWrapper放入IOC容器
        this.factoryBeanInstanceCache.put(beanName, beanWrapper);

        // 在实例化bean之后进行一些动作
        beanPostProcessor.postProcessAfterInitialization(instance, beanName);

        // 3.依赖注入
        populateBean(beanName, myBeanDefinition, beanWrapper);

        Object o = this.factoryBeanObejctCache.get(beanName);
        // 即使是单例模式有单例IOC容器，但获取Instance也要先封装为BeanWrapper，然后在通用容器中取
        return o;
    }

    private void populateBean(String beanName, MYBeanDefinition myBeanDefinition, MYBeanWrapper myBeanWrapper) {
        Class<?> clazz = myBeanWrapper.getWrappedClass();
        // 只有容器管理的bean才会给他依赖注入
        if (!(clazz.isAnnotationPresent(MYController.class) || clazz.isAnnotationPresent(MYService.class))) { return; }

        Object instance = myBeanWrapper.getWrappedInstance();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(MYAutowired.class)) {continue;}

            MYAutowired annotation = field.getAnnotation(MYAutowired.class);
            // 自定义的beanName
            String autowiredBeanName = annotation.value().trim();
            if ("".equals(autowiredBeanName)) {
                // 获取全限定类名
                autowiredBeanName = field.getType().getName();
            }

            field.setAccessible(true);

            try {
                // 因为要给当前Bean注入时，可能要注入的Bean还没初始化，因此就暂时不给这个字段注入
                // 但是当正式使用时还会getBean一次，这时所有bean都初始化完成了，就可以注入了
                String autowiredBeanFactoryName = beanDefinitionMap.get(autowiredBeanName).getFactoryBeanName();

                Object dependBean = this.factoryBeanObejctCache.get(autowiredBeanFactoryName);
                if(dependBean == null) {
                    dependBean = this.factoryBeanInstanceCache.get(autowiredBeanName);
                    if (dependBean == null) {
                        dependBean = getBean(autowiredBeanName);
                    } else {
                        dependBean = ((MYBeanWrapper)dependBean).getWrappedInstance();
                    }
                }
                field.set(instance, dependBean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.factoryBeanInstanceCache.remove(beanName);
        this.factoryBeanObejctCache.put(myBeanDefinition.getFactoryBeanName(), instance);
        this.factoryBeanObejctCache.put(myBeanDefinition.getBeanClassName(), instance);
    }

    private MYBeanWrapper instantiteBean(String beanName, MYBeanDefinition myBeanDefinition) {
        // 1.拿到类的全限定类名
        String className = myBeanDefinition.getBeanClassName();
        // 2.通过反射进行实例化
        Object instance = null;
        try {

            Class<?> clazz = Class.forName(className);
            instance = clazz.newInstance();

            //-------------------------AOP部分入口代码-----------------------


            //----------------------------------------------------------------



        } catch (Exception e) {
            e.printStackTrace();
        }
        MYBeanWrapper beanWrapper = new MYBeanWrapper(instance);
        this.factoryBeanInstanceCache.put(myBeanDefinition.getFactoryBeanName(), beanWrapper);
        this.factoryBeanInstanceCache.put(className, beanWrapper);
        // 3.封装BeanWrapper
        // 注：无论单例多例，都要先封装成BeanWrapper

        return beanWrapper;
    }

    public Object getSingleton(String beanName) {
        Object o = this.factoryBeanObejctCache.get(beanName);
        if (o != null) {
            return o;
        }
        return null;
    }

    @Override
    public Object getBean(Class<?> beanCLass) throws Exception {
        return null;
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig() {
        return this.reader.getConfig();
    }
}
