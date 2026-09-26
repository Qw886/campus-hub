package com.campushub.common.exception;

import com.campushub.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(
            BusinessException exception
    ) {
        return ResponseEntity
                .status(exception.getCode())
                .body(Result.error(
                        exception.getCode(),
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        FieldError fieldError =
                exception.getBindingResult().getFieldError();

        String message = fieldError == null
                ? "请求参数不合法"
                : fieldError.getDefaultMessage();

        return ResponseEntity
                .badRequest()
                .body(Result.error(400, message));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError == null ? "请求参数不合法" : fieldError.getDefaultMessage();
        return ResponseEntity.badRequest().body(Result.error(400, message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception
    ) {
        return ResponseEntity
                .badRequest()
                .body(Result.error(400, "请求体格式错误"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException exception
    ) {
        return ResponseEntity
                .badRequest()
                .body(Result.error(400, "请求参数类型错误"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Void>> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return ResponseEntity.status(409).body(Result.error(409, "请求与现有数据冲突"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(
            Exception exception
    ) {
        log.error("服务器内部异常", exception);

        return ResponseEntity
                .internalServerError()
                .body(Result.error(500, "服务器内部错误"));
    }
}
