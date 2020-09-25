package com.myexample.serverless;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SpringBootApplication
public class SpringCloudFunctionSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudFunctionSampleApplication.class, args);
    }

    @Bean
    public Function<String, String> upperCase() {
        return value -> value.toUpperCase();
    }

    @Bean
    public Function<Flux<String>, Flux<String>> lowerCase() {
        return flux -> flux.map(value -> value.toLowerCase());
    }

    @Bean
    public Supplier<String> hello() {
        return () -> "Hello.";
    }

    @Bean
    public Consumer<String> sysout() {
        return System.out::println;
    }

}
