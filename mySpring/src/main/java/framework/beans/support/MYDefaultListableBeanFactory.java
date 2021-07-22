package framework.beans.support;

import framework.beans.config.MYBeanDefinition;
import framework.context.support.MYAbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IOC容器的默认实现（因此继承了AbstractApplicationContext），是扩展其余IOC容器的基础
 */
public class MYDefaultListableBeanFactory extends MYAbstractApplicationContext {

    /**
     * 保存BeanDedifinition信息
     * key : factoryBeanName (可以作为bean的唯一标识)
     * val : bean实例对象
     */
    protected final Map<String, MYBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, MYBeanDefinition>();
}