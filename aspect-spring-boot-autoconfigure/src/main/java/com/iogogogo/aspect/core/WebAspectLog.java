package com.iogogogo.aspect.core;

import com.iogogogo.aspect.properties.InetUtilsProperties;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.PriorityOrdered;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by tao.zeng on 2021/6/21.
 */
@Slf4j
@Aspect
@Configuration(proxyBeanMethods = false)
@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
@ConditionalOnProperty(prefix = "aspect", name = "enable", havingValue = "true", matchIfMissing = true)
public class WebAspectLog implements PriorityOrdered {

    private final InetUtils inetUtils;

    public WebAspectLog(InetUtilsProperties inetUtilsProperties) {
        inetUtils = new InetUtils(inetUtilsProperties);
    }

    @Pointcut("@annotation(com.iogogogo.aspect.annotation.AspectLog)")
    public void webLog() {
    }

    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        InetUtils.HostInfo hostInfo = inetUtils.findFirstNonLoopbackHostInfo();
        log.debug("ARGS : " + Arrays.toString(joinPoint.getArgs()));

        log.info("CLASS_METHOD : {}.{}()", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());

        // 环绕通知前
        Optional.ofNullable(attributes).ifPresent(x -> {
            HttpServletRequest request = x.getRequest();
            // web 记录请求内容
            log.info("IP : {}", hostInfo.getIpAddress());
            log.info("URL : {}", request.getRequestURL().toString());
            log.info("HTTP_METHOD : {}", request.getMethod());
        });

        // 环绕通知后
        try {
            Object ret = joinPoint.proceed();
            log.debug("RESPONSE : {}", ret);
            return ret;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    @Override
    public int getOrder() {
        //保证事务等切面先执行
        return Integer.MAX_VALUE;
    }
}
