package com.iogogogo.aspect;

import com.iogogogo.aspect.core.WebAspectLog;
import com.iogogogo.aspect.properties.AspectLogProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by tao.zeng on 2021/6/21.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AspectLogProperties.class)
@Import({WebAspectLog.class})
public class AspectLogAutoConfiguration {
}
