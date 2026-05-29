package com.dashboard.backend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        String msg = e.getMessage();
        HttpStatus status = resolveStatus(msg);
        return ResponseEntity.status(status).body(Map.of("message", msg != null ? msg : "잘못된 요청입니다."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst().orElse("입력값을 확인해주세요.");
        return ResponseEntity.badRequest().body(Map.of("message", msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "서버 오류가 발생했습니다."));
    }

    // 예외 메시지로 적절한 HTTP 상태코드 결정
    private HttpStatus resolveStatus(String msg) {
        if (msg == null) return HttpStatus.BAD_REQUEST;
        if (msg.contains("찾을 수 없습니다")) return HttpStatus.NOT_FOUND;
        if (msg.contains("권한이 없습니다")) return HttpStatus.FORBIDDEN;
        if (msg.contains("이미 사용 중")) return HttpStatus.CONFLICT;
        if (msg.contains("비밀번호") || msg.contains("이메일 또는")) return HttpStatus.UNAUTHORIZED;
        return HttpStatus.BAD_REQUEST;
    }
}
