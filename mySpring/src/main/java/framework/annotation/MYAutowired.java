package framework.annotation;

import jdk.nashorn.internal.ir.annotations.Reference;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MYAutowired {
    String value() default "";
}
