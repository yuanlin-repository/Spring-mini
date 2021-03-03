package framework.beans;

// Spring的做法，不会把最原始的对象放出去，而是会用BeanWrapper进行一次包装
public class MYBeanWrapper {

    private Object wrappedInstance;
    // 保存class，为了多例模式服务
    private Class<?> wrappedClass;

    public MYBeanWrapper(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
    }

    public Object getWrappedInstance() {
        return wrappedInstance;
    }

    public Class<?> getWrappedClass() {
        return wrappedInstance.getClass();
    }
}
