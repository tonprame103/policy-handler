package com.jsontool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * JSON Tool — Spring Boot 4.0.5 / Java 21
 *
 * Java 25 upgrade checklist:
 *   1. pom.xml: เปลี่ยน <java.version>21</java.version> → 25
 *   2. (optional) ใช้ Java 25 features: primitive types in patterns, value classes
 *   3. mvn rewrite:run  ← OpenRewrite จัดการ migration อัตโนมัติ
 *   4. mvn clean package && java -jar target/json-tool-1.0.0.jar
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class JsonToolApplication {

    public static void main(String[] args) {
        SpringApplication.run(JsonToolApplication.class, args);
    }
}
