package com.jsontool.controller;

import com.jsontool.model.FileListResponse;
import com.jsontool.model.ValidationResult;
import com.jsontool.service.JsonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

/**
 * JSON Tool REST API
 *
 * Spring Boot 4 notes:
 *   - @CrossOrigin ยังใช้งานได้ แต่ CORS กลางกำหนดใน WebConfig แล้ว
 *   - Spring Framework 7 ไม่เปลี่ยน annotation style ของ @RestController
 *   - ใช้ Java record เป็น response type โดยตรงได้เลย (Jackson serialize ได้)
 *
 * Java 25 upgrade note:
 *   - Controller class ไม่ต้องเปลี่ยน — annotations เหมือนเดิมทุกอย่าง
 */
@RestController
@RequestMapping("/api")
public class JsonController {

    private final JsonService jsonService;

    public JsonController(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    /**
     * POST /api/validate
     * Body: { "json": "..." }
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidationResult> validate(
            @RequestBody Map<String, String> body) {

        String json = body.getOrDefault("json", "");
        return ResponseEntity.ok(jsonService.validate(json));
    }

    /**
     * GET /api/files
     * List all .json files in configured folder
     */
    @GetMapping("/files")
    public ResponseEntity<FileListResponse> listFiles() {
        try {
            return ResponseEntity.ok(jsonService.listJsonFiles());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/files/{fileName}
     * Read and validate a specific file
     */
    @GetMapping("/files/{fileName}")
    public ResponseEntity<ValidationResult> readFile(
            @PathVariable String fileName) {
        try {
            return ResponseEntity.ok(jsonService.readFile(fileName));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(ValidationResult.failure("IO Error", e.getMessage()));
        }
    }

    /**
     * GET /api/health  (lightweight custom check — actuator /actuator/health ก็มีให้แล้ว)
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "JSON Tool",
                "spring-boot", "4.0.5",
                "java", System.getProperty("java.version")
        ));
    }
}
