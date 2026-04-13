package com.jsontool.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Validation result — Java record (Java 16+, idiomatic in Java 21/25)
 *
 * @JsonInclude(NON_NULL) — Jackson 3 compatible: ไม่ส่ง field ที่เป็น null ออก JSON
 *
 * Java 25 upgrade note:
 *   - Record syntax ไม่เปลี่ยน — ใช้ได้เลยโดยไม่ต้องแก้ไข
 *   - สามารถเพิ่ม @SuppressWarnings("preview") สำหรับ preview features ถ้าต้องการ
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ValidationResult(
        boolean valid,
        String message,
        String formattedJson,
        int lineCount,
        String errorDetail
) {
    /** Factory — valid JSON */
    public static ValidationResult success(String formattedJson) {
        int lines = formattedJson.split("\n", -1).length;
        return new ValidationResult(true, "Valid JSON", formattedJson, lines, null);
    }

    /** Factory — invalid JSON */
    public static ValidationResult failure(String message, String errorDetail) {
        return new ValidationResult(false, message, null, 0, errorDetail);
    }

    /** Factory — empty input */
    public static ValidationResult empty() {
        return new ValidationResult(false, "Input is empty",
                null, 0, "Please provide JSON text to validate.");
    }
}
