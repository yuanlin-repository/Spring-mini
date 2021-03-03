package framework.beans.support;

import framework.beans.config.MYBeanDefinition;
import framework.context.support.MYAbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IOC容器的默认实现（因此继承了AbstractApplicationContext），是扩展其余IOC容器的基础
 */
public class MYDefaultListableBeanFactory extends MYAbstractApplicationContext {
    //保存BeanDedifinition类信息
    // 这里的key是factoryBeanName，因为我们选用的factoryBeanName作为Bean的唯一标识
    protected final Map<String, MYBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, MYBeanDefinition>();
}