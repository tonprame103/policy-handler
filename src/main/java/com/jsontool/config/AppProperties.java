package com.jsontool.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration binding (Spring Boot 4 requires private fields + getters/setters)
 *
 * NOTE: Spring Boot 4 ไม่รองรับ public field binding อีกต่อไป
 * ต้องใช้ private field + getter/setter หรือ Java Record แทน
 *
 * Java 25 note: สามารถเปลี่ยนเป็น record ได้โดยตรงเมื่อ upgrade
 */
@ConfigurationProperties(prefix = "json.files")
public class AppProperties {

    /**
     * Path to the folder containing .json files to browse.
     * Override via: --json.files.folder=/path or JSON_FOLDER env variable.
     */
    private String folder = "./json-samples";

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
}
