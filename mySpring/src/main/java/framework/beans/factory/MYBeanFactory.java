package framework.beans.factory;

public interface MYBeanFactory {
    /**
     * 通过 beanName 得到 bean 实例
     * @param beanName
     * @return
     * @throws Exception
     */
    Object getBean(String beanName) throws Exception;

    /**
     * 通过 Class 得到 bean
     * @param beanCLass
     * @return
     * @throws Exception
     */
    Object getBean(Class<?> beanCLass) throws Exception;
}
