package framework.context.support;

public abstract class MYAbstractApplicationContext {
    // 受保护，只提供给子类重写
    protected void refresh() throws Exception {}
}
