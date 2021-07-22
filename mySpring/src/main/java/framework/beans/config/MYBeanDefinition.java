package framework.beans.config;

public class MYBeanDefinition {

    /**
     * 保存 beanName，作为 bean 的唯一标识
     */
    private String factoryBeanName;

    /**
     * 类的全限定类名，为了后面创建实例和注解判断
     */
    private String beanClassName;

    /**
     * 懒加载
     */
    private boolean isLazyInit = false;

    /**
     * 是否单例
     */
    private boolean isSingleton = true;

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public boolean isLazyInit() {
        return isLazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        isLazyInit = lazyInit;
    }

    public boolean isSingleton() {
        return isSingleton;
    }

    public void setSingleton(boolean singleton) {
        isSingleton = singleton;
    }
}
