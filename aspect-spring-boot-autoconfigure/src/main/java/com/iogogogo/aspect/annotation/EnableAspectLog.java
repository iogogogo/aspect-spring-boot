package com.iogogogo.aspect.annotation;

import com.iogogogo.aspect.AspectLogAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by tao.zeng on 2021/6/21.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({AspectLogAutoConfiguration.class})
public @interface EnableAspectLog {
}
