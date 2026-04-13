package com.jsontool;

import com.jsontool.config.AppProperties;
import com.jsontool.model.ValidationResult;
import com.jsontool.service.JsonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JsonService
 *
 * Spring Boot 4 test notes:
 *   - JUnit 5 เป็น default (ไม่ต้องเพิ่ม dependency)
 *   - AssertJ ใช้ได้เหมือนเดิม
 *   - @SpringBootTest ยังใช้ได้ — test นี้เป็น plain unit test (เร็วกว่า)
 *
 * Java 25 note: test code ไม่มี breaking change
 */
@DisplayName("JsonService Tests")
class JsonServiceTest {

    private JsonService jsonService;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties();
        props.setFolder("./json-samples");
        jsonService = new JsonService(props);
    }

    @Test
    @DisplayName("validate valid JSON returns success")
    void validateValidJson() {
        ValidationResult result = jsonService.validate("""
                {
                  "name": "test",
                  "value": 42,
                  "active": true
                }
                """);

        assertThat(result.valid()).isTrue();
        assertThat(result.message()).isEqualTo("Valid JSON");
        assertThat(result.formattedJson()).isNotBlank();
        assertThat(result.lineCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("validate invalid JSON returns failure with error detail")
    void validateInvalidJson() {
        ValidationResult result = jsonService.validate("""
                { "name": "missing comma"
                  "value": 42 }
                """);

        assertThat(result.valid()).isFalse();
        assertThat(result.message()).contains("Invalid JSON");
        assertThat(result.errorDetail()).isNotBlank();
        assertThat(result.formattedJson()).isNull();
    }

    @Test
    @DisplayName("validate null or blank input returns empty result")
    void validateEmptyInput() {
        assertThat(jsonService.validate(null).valid()).isFalse();
        assertThat(jsonService.validate("  ").valid()).isFalse();
        assertThat(jsonService.validate("").valid()).isFalse();
    }

    @ParameterizedTest
    @DisplayName("validate valid JSON array and primitive types")
    @ValueSource(strings = {
            "[1, 2, 3]",
            "\"hello\"",
            "42",
            "true",
            "null"
    })
    void validateJsonPrimitives(String json) {
        assertThat(jsonService.validate(json).valid()).isTrue();
    }

    @Test
    @DisplayName("formatted JSON is properly indented")
    void formattedJsonIsIndented() {
        ValidationResult result = jsonService.validate("{\"a\":1,\"b\":2}");

        assertThat(result.valid()).isTrue();
        assertThat(result.formattedJson()).contains("\n");
        assertThat(result.formattedJson()).contains("  ");
    }
}
