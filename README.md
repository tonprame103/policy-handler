# JSON Tool — Spring Boot 4.0.5 + Java 21 (Java 25 Ready)

Web application สำหรับ **Validate JSON** และ **อ่านไฟล์ JSON จาก folder**

---

## Stack

| Component      | Version                         |
|----------------|---------------------------------|
| Spring Boot    | **4.0.5** (latest stable)       |
| Spring Framework | 7.x                           |
| Java           | **21** (upgrade-ready ถึง 25)   |
| Jakarta EE     | 11                              |
| Jackson        | 3.x                             |
| Virtual Threads | ✅ เปิดแล้ว (Project Loom)      |

---

## Quick Start

```bash
# Requirements: Java 21+, Maven 3.9+

mvn clean package -DskipTests
java -jar target/json-tool-1.0.0.jar

# เปิด browser
open http://localhost:8080
```

---

## Upgrade ไป Java 25 (3 ขั้นตอน)

### ขั้นที่ 1 — เปลี่ยน Java version ใน pom.xml
```xml
<!-- เปลี่ยนจาก -->
<java.version>21</java.version>

<!-- เป็น -->
<java.version>25</java.version>
```

### ขั้นที่ 2 — (Optional) ให้ OpenRewrite migrate code อัตโนมัติ
ปลด comment ใน pom.xml แล้วรัน:
```bash
mvn rewrite:run
```
OpenRewrite จะจัดการ:
- Replace deprecated API calls
- Modernize language constructs
- Update build configuration

### ขั้นที่ 3 — Build และ test
```bash
mvn clean package
java -jar target/json-tool-1.0.0.jar
```

> **Spring Boot 4.0.5 รองรับ Java 17–25** โดยไม่ต้องเปลี่ยน dependencies ใดๆ
> Virtual Threads ทำงานได้ดีขึ้นบน Java 25 (stable Loom implementation)

---

## Configuration

### application.properties
```properties
# เปลี่ยน folder ที่อ่านไฟล์ JSON
json.files.folder=./json-samples

# Virtual Threads (ทำงานบน Java 21+, ดีขึ้นบน Java 25)
spring.threads.virtual.enabled=true
```

### Override via command line
```bash
java -jar target/json-tool-1.0.0.jar \
  --server.port=9090 \
  --json.files.folder=/data/json
```

### Override via environment variable
```bash
export JSON_FOLDER=/data/json
java -jar target/json-tool-1.0.0.jar
```

---

## API Endpoints

| Method | Path                  | Description                        |
|--------|-----------------------|------------------------------------|
| `POST` | `/api/validate`       | Validate & format JSON string      |
| `GET`  | `/api/files`          | List all .json files in folder     |
| `GET`  | `/api/files/{name}`   | Read & validate a specific file    |
| `GET`  | `/api/health`         | Custom health check                |
| `GET`  | `/actuator/health`    | Spring Boot Actuator health        |
| `GET`  | `/actuator/info`      | App info (version, java, OS)       |

### POST /api/validate — Request / Response
```json
// Request body
{ "json": "{ \"name\": \"test\", \"value\": 42 }" }

// Response — valid
{
  "valid": true,
  "message": "Valid JSON",
  "formattedJson": "{\n  \"name\": \"test\",\n  \"value\": 42\n}",
  "lineCount": 4
}

// Response — invalid
{
  "valid": false,
  "message": "Invalid JSON: Unexpected character",
  "errorDetail": "Line 2, Column 12: Unexpected character ('\"')"
}
```

### Error format (RFC 9457 ProblemDetail — Spring Boot 4 standard)
```json
{
  "type": "https://jsontool.local/errors/internal",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "..."
}
```

---

## Project Structure

```
json-tool/
├── pom.xml                                          ← Spring Boot 4.0.5, Java 21
├── README.md
├── json-samples/                                    ← JSON files folder (configurable)
│   ├── products.json                               ← valid example
│   ├── users.json                                  ← valid example (ภาษาไทย)
│   └── invalid-config.json                         ← error detection example
└── src/
    ├── main/
    │   ├── java/com/jsontool/
    │   │   ├── JsonToolApplication.java            ← @SpringBootApplication
    │   │   ├── config/
    │   │   │   ├── AppProperties.java              ← @ConfigurationProperties
    │   │   │   └── WebConfig.java                  ← CORS (Spring Framework 7)
    │   │   ├── controller/
    │   │   │   ├── JsonController.java             ← REST endpoints
    │   │   │   └── GlobalExceptionHandler.java     ← ProblemDetail (RFC 9457)
    │   │   ├── service/
    │   │   │   └── JsonService.java                ← validate + file reading
    │   │   └── model/
    │   │       ├── ValidationResult.java           ← Java record
    │   │       ├── JsonFileInfo.java               ← Java record
    │   │       └── FileListResponse.java           ← Java record
    │   └── resources/
    │       ├── application.properties
    │       └── static/index.html                   ← Frontend (dark terminal UI)
    └── test/
        └── java/com/jsontool/
            └── JsonServiceTest.java                ← Unit tests (JUnit 5)
```

---

## Java 21 Features ที่ใช้ใน Codebase

| Feature | ใช้ที่ไหน |
|---------|----------|
| **Records** | `ValidationResult`, `JsonFileInfo`, `FileListResponse` |
| **Pattern matching for instanceof** | `JsonService.buildErrorDetail()` |
| **Switch expressions** | `JsonService.buildFileInfo()` |
| **Text blocks** | Error messages ใน service |
| **`String.formatted()`** | Error detail formatting |
| **`List.copyOf()`** | Immutable file list |
| **Virtual Threads** | Tomcat request handling (via config) |

## Java 25 Features ที่พร้อม Adopt หลัง Upgrade

| Feature (JEP) | สามารถ refactor ตรงไหน |
|---------------|------------------------|
| Primitive types in patterns (455) | `JsonService` switch expressions |
| Value Classes (401 preview) | `JsonFileInfo` record → value class |
| Module Import Declarations (476) | ถ้า modularize โปรเจกต์ |

---

## Security

- **Path Traversal Protection** — ป้องกัน `../` ใน file name
- **Directory Boundary Check** — ไฟล์ต้องอยู่ใน folder ที่กำหนดเท่านั้น
- **CORS** — กำหนดใน `WebConfig` (ปรับ `allowedOriginPatterns` สำหรับ production)
- **Maven Enforcer** — ป้องกัน build ด้วย JDK ต่ำกว่า Java 21
