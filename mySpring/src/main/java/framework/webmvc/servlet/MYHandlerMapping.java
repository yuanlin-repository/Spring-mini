package framework.webmvc.servlet;

import framework.annotation.MYRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 用来处理具体请求
 */
public class MYHandlerMapping {
    // 处理具体请求的Controller对象
    private Object Controller;
    // 处理请求的具体方法
    private Method method;
    // 处理的请求路径
    private Pattern pattern;

    public MYHandlerMapping(Object controller, Method method, Pattern pattern) {
        Controller = controller;
        this.method = method;
        this.pattern = pattern;
    }

    public Object getController() {
        return Controller;
    }

    public void setController(Object controller) {
        Controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }
}
