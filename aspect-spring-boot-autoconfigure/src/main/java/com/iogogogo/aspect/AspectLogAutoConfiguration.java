package com.iogogogo.aspect;

import com.iogogogo.aspect.core.WebAspectLog;
import com.iogogogo.aspect.properties.AspectLogProperties;
import com.iogogogo.aspect.properties.InetUtilsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * https://blog.csdn.net/u010675669/article/details/109010042
 * https://blog.csdn.net/yunxing323/article/details/108655250
 * <p>
 * Spring 5.2.0+的版本，建议你的配置类均采用Lite模式去做，即显示设置proxyBeanMethods = false。
 * Spring Boot在2.2.0版本（依赖于Spring 5.2.0）起就把它的所有的自动配置类的此属性改为了false，即@Configuration(proxyBeanMethods = false)，提高Spring启动速度
 * <p>
 * proxyBeanMethods = false ==> 告诉springboot这是一个配置类 == 配置文件
 * <p>
 * Created by tao.zeng on 2021/6/21.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(value = {AspectLogProperties.class, InetUtilsProperties.class})
@Import({WebAspectLog.class})
public class AspectLogAutoConfiguration {
}
