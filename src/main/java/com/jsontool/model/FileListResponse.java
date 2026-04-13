package com.jsontool.model;

import java.util.List;

/**
 * Response wrapper for file listing endpoint
 *
 * Java 25 note: ใช้ records สำหรับ API response ได้เลย — no change needed
 */
public record FileListResponse(
        List<JsonFileInfo> files,
        String folderPath,
        int count
) {}
