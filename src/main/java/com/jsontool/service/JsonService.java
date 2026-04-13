package com.jsontool.service;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jsontool.config.AppProperties;
import com.jsontool.model.FileListResponse;
import com.jsontool.model.JsonFileInfo;
import com.jsontool.model.ValidationResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * JSON validation and file-reading service
 *
 * Java 21 features used:
 *   - Pattern matching for instanceof
 *   - Switch expressions
 *   - Text blocks (for error messages)
 *   - Records (model classes)
 *   - Sequenced collections (List.copyOf)
 *
 * Java 25 upgrade note:
 *   - Primitive types in patterns (JEP 455) — สามารถใช้แทน if/else chains
 *   - Value classes (JEP 401 preview) — JsonFileInfo สามารถเป็น value class ได้
 *   - ไม่มี breaking change กับ code ชุดนี้
 */
@Service
public class JsonService {

    private final ObjectMapper objectMapper;
    private final AppProperties props;

    public JsonService(AppProperties props) {
        this.props = props;
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    // ─── Validation ─────────────────────────────────────────────────────────

    public ValidationResult validate(String jsonInput) {
        // Java 21: pattern matching + switch expression
        if (jsonInput == null || jsonInput.isBlank()) {
            return ValidationResult.empty();
        }

        try {
            JsonNode node = objectMapper.readTree(jsonInput);
            String formatted = objectMapper.writeValueAsString(node);
            return ValidationResult.success(formatted);
        } catch (JsonProcessingException e) {
            return ValidationResult.failure(
                    "Invalid JSON: " + e.getOriginalMessage(),
                    buildErrorDetail(e)
            );
        }
    }

    // ─── File Listing ────────────────────────────────────────────────────────

    public FileListResponse listJsonFiles() throws IOException {
        Path folder = resolvedFolder();

        if (!Files.exists(folder)) {
            Files.createDirectories(folder);
            return new FileListResponse(List.of(), folder.toString(), 0);
        }

        List<JsonFileInfo> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.json")) {
            for (Path path : stream) {
                files.add(buildFileInfo(path));
            }
        }

        files.sort(Comparator.comparing(JsonFileInfo::fileName));
        return new FileListResponse(
                List.copyOf(files),   // Java 21: Sequenced collection — immutable copy
                folder.toString(),
                files.size()
        );
    }

    // ─── File Reading ────────────────────────────────────────────────────────

    public ValidationResult readFile(String fileName) throws IOException {
        // Security: reject path traversal attempts
        if (isUnsafeFileName(fileName)) {
            return ValidationResult.failure(
                    "Invalid file name",
                    "File name contains illegal characters."
            );
        }

        Path folder = resolvedFolder();
        Path filePath = folder.resolve(fileName).normalize();

        // Security: ensure file is inside the configured folder
        if (!filePath.startsWith(folder)) {
            return ValidationResult.failure(
                    "Access denied",
                    "File is outside the allowed directory."
            );
        }

        if (!Files.exists(filePath)) {
            return ValidationResult.failure(
                    "File not found",
                    "The file '%s' does not exist.".formatted(fileName)  // Java 15+ text block style
            );
        }

        return validate(Files.readString(filePath));
    }

    // ─── Internal Helpers ────────────────────────────────────────────────────

    private Path resolvedFolder() throws IOException {
        return Path.of(props.getFolder()).toAbsolutePath().normalize();
    }

    private boolean isUnsafeFileName(String name) {
        return name == null
                || name.contains("..")
                || name.contains("/")
                || name.contains("\\")
                || name.isBlank();
    }

    private JsonFileInfo buildFileInfo(Path path) throws IOException {
        String content    = Files.readString(path);
        boolean isValid   = isValidJson(content);
        long size         = Files.size(path);
        LocalDateTime mod = LocalDateTime.ofInstant(
                Files.getLastModifiedTime(path).toInstant(),
                ZoneId.systemDefault()
        );

        // Java 25 note: boolean switch with case true/false (JEP 455) is still preview in Java 25.
        // Using ternary to avoid --enable-preview requirement.
        String preview = isValid
                ? truncate(content.strip(), 120)
                : "(invalid JSON)";

        return new JsonFileInfo(
                path.getFileName().toString(),
                size,
                mod,
                isValid,
                preview
        );
    }

    private boolean isValidJson(String content) {
        try {
            objectMapper.readTree(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    private static String buildErrorDetail(JsonProcessingException e) {
        // Java 25: pattern matching for instanceof
        if (e.getLocation() instanceof JsonLocation loc) {
            return "Line %d, Column %d: %s"
                    .formatted(loc.getLineNr(), loc.getColumnNr(), e.getOriginalMessage());
        }
        return e.getOriginalMessage();
    }
}
