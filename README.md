# Spring Cloud Function を使って WebAPI を作る

# Spring Initializer を使ってプロジェクトを作成

* IntelliJ IDEA -> New Project 
* Spring Initializr を選択
* Project SDK -> 11
* Group / Artifact を適当に指定
* Dependencies に Spring Cloud 系は「入れない」
* プロジェクトができたら、build.gradel を編集
  * org.springframework.boot:spring-boot-starter は不要なので削除
  * `implementation 'org.springframework.cloud:spring-cloud-starter-function-web:3.0.10.RELEASE'` を追加

# API 作成

## シンプルな API の作成
* main 以下のソース： `SpringCloudFunctionSampleApplication.java` を編集
```
package com.myexample.serverless;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringCloudFunctionSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudFunctionSampleApplication.class, args);
    }

    // 以下を追加
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
```
* main を起動

### テスト
* `curl -H "Content-Type: text/plain" localhost:8080/upperCase -d Hello`
  * `HELLO` が返る
* `curl -H "Content-Type: text/plain" localhost:8080/lowerCase -d Hello`
  * `["hello"]` が返る
* curl -H "Content-Type: text/plain" localhost:8080/lowerCase -d 'Hello                    
                                      WORLD
                                      !!'
  * `["hello\nworld\n!!"]` が返る(Fluxだから？)
* `curl -H "Content-Type: text/plain" localhost:8080/hello`
    * `Hello.` が返る
* `curl -H "Content-Type: text/plain" localhost:8080/sysout -d Hello`
    * 標準出力に Hello が出力される

## 関数用のクラスを作る
* functions パッケージを切る
* CharCounter.java として以下のクラスを作成
```
package com.myexample.serverless.functions;

import java.util.function.Function;

public class CharCounter implements Function<String, Integer> {

    @Override
    public Integer apply(String s) {
        return s.length();
    }
    
}
```
* application.properties に以下の行を追加
```
spring.cloud.function.scan.packages=com.myexample.serverless.functions
```
* main を起動

### テスト
* `curl -H "Content-Type: text/plain" localhost:8080/charCounter -d Hello`
  * `5` が返る

## JSON でやりとりする
* Lombok を build.gradle に追加
```
plugins {
    id 'org.springframework.boot' version '2.3.4.RELEASE'
    id 'io.spring.dependency-management' version '1.0.10.RELEASE'
    id 'java'
}

group = 'com.myexample'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-function-web:3.0.10.RELEASE'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation('org.springframework.boot:spring-boot-starter-test') {
        exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
    }
}

test {
    useJUnitPlatform()
}
```
  * オブジェクトに getter/setter が無いと、JSONに変換してくれないため
* Greet.java として以下のクラスを作成
```
package com.myexample.serverless.functions;

import lombok.Data;

import java.util.function.Function;

public class Greet implements Function<Greet.Greeting, Greet.Greeting> {

    @Data
    public static class Greeting {
        String name;
        String message;
    }

    @Override
    public Greeting apply(Greeting greeting) {
        var res = new Greeting();
        res.name = "Spring Cloud Function";
        res.message = String.format("Hello, %s", greeting.name);
        return res;
    }
}
```  
* main を起動

### テスト 
* `curl -H "Content-Type: application/json" localhost:8080/greet -d '{"name": "Itagaki", "message": "Hello"}'` を実行
  * `{"name":"Spring Cloud Function","message":"Hello, Itagaki"}` が返る
