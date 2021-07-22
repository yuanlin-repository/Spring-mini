package framework.beans;

public class MYBeanWrapper {

    /**
     * 原始bean对象
     */
    private Object wrappedInstance;

    /**
     * 保存class，为了多例模式服务
     */
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
