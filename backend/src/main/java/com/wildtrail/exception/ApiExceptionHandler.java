package com.wildtrail.exception;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String,String>> methodNotAllowed() {
        return ResponseEntity.status(405).body(Map.of("error", "This action is not available"));
    }
    @ExceptionHandler({org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,org.springframework.web.bind.MissingServletRequestParameterException.class})
    public ResponseEntity<Map<String,String>> badParameter(){return ResponseEntity.badRequest().body(Map.of("error","Request parameter is invalid or missing"));}

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> unexpected(Exception exception){
        org.slf4j.LoggerFactory.getLogger(ApiExceptionHandler.class).error("Unexpected API error",exception);
        return ResponseEntity.status(500).body(Map.of("error","Unable to process this request. Please try again."));
    }
    @ExceptionHandler(value={ApiException.class})
    public ResponseEntity<Map<String, String>> handleApiException(ApiException exception) {
        return ResponseEntity.status((HttpStatusCode)exception.getStatus()).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(value={MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
        FieldError error = exception.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = error == null ? "Request validation failed" : error.getDefaultMessage();
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    @ExceptionHandler(value={HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, String>> handleUnreadableBody() {
        return ResponseEntity.badRequest().body(Map.of("error", "Request body must be valid JSON"));
    }

    @ExceptionHandler(value={DataIntegrityViolationException.class})
    public ResponseEntity<Map<String, String>> handleConflict() {
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CONFLICT).body(Map.of("error", "A record conflicts with existing data or is referenced by another record"));
    }

    @ExceptionHandler(value={AccessDeniedException.class})
    public ResponseEntity<Map<String, String>> handleAccessDenied() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean signedIn = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
        return ResponseEntity.status((HttpStatusCode)(signedIn ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED)).body(Map.of("error", signedIn ? "Administrator role required" : "Admin login required"));
    }
}
