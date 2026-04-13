package com.jsontool.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/**
 * Global error handler using ProblemDetail (RFC 9457)
 *
 * Spring Boot 4 / Spring Framework 7 ใช้ ProblemDetail เป็น standard error format
 * แทนที่ Map<String, Object> แบบ custom เหมือน Spring Boot 3.x
 *
 * Java 25 note: ProblemDetail เป็น Spring class — ไม่มี breaking change
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAll(Exception e) {
        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                        e.getMessage() != null ? e.getMessage() : "Unexpected error");
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://jsontool.local/errors/internal"));
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException e) {
        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setTitle("Bad Request");
        problem.setType(URI.create("https://jsontool.local/errors/bad-request"));
        return problem;
    }
}
