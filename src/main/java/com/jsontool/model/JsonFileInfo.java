package com.jsontool.model;

import java.time.LocalDateTime;

/**
 * File metadata record — Java 21 record (zero-boilerplate DTO)
 *
 * Java 25 upgrade note: ไม่มีการเปลี่ยนแปลง — records เป็น stable feature
 */
public record JsonFileInfo(
        String fileName,
        long sizeBytes,
        LocalDateTime lastModified,
        boolean valid,
        String preview
) {}
