# 自定义spring-boot-starter

- pom配置
```xml
<dependency>
    <groupId>com.iogogogo.aspect</groupId>
    <artifactId>aspect-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

- Usage
```java
// 启用自动配置
@EnableAspectLog
@Slf4j
@RestController
@RequestMapping("/api")
@SpringBootApplication
public class ExampleAspectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleAspectApplication.class, args);
    }

    // 需要记录日志的方法添加@AspectLog注解
    @AspectLog
    @GetMapping("/index")
    public String index() {
        log.info("/api/index");
        return "ok";
    }
}
```

- log
```shell
2021-06-21 23:22:09.008  INFO 50757 --- [nio-8080-exec-2] com.iogogogo.aspect.core.WebAspectLog    : URL : http://127.0.0.1:8080/api/index
2021-06-21 23:22:09.009  INFO 50757 --- [nio-8080-exec-2] com.iogogogo.aspect.core.WebAspectLog    : HTTP_METHOD : GET
2021-06-21 23:22:09.010  INFO 50757 --- [nio-8080-exec-2] com.iogogogo.aspect.core.WebAspectLog    : IP : 192.168.0.102
2021-06-21 23:22:09.011  INFO 50757 --- [nio-8080-exec-2] com.iogogogo.aspect.core.WebAspectLog    : CLASS_METHOD : com.example.ExampleAspectApplication.index()
2021-06-21 23:22:09.011 DEBUG 50757 --- [nio-8080-exec-2] com.iogogogo.aspect.core.WebAspectLog    : ARGS : []
2021-06-21 23:22:09.011  INFO 50757 --- [nio-8080-exec-2] com.example.ExampleAspectApplication     : /api/index
2021-06-21 23:22:09.011 DEBUG 50757 --- [nio-8080-exec-2] com.iogogogo.aspect.core.WebAspectLog    : RET : ok

```