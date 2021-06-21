package com.example;

import com.iogogogo.aspect.annotation.AspectLog;
import com.iogogogo.aspect.annotation.EnableAspectLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@EnableAspectLog
@Slf4j
@RestController
@RequestMapping("/api")
@SpringBootApplication
public class ExampleAspectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleAspectApplication.class, args);
    }

    @AspectLog
    @GetMapping("/index")
    public String index() {
        log.info("/api/index");
        return "ok";
    }
}
