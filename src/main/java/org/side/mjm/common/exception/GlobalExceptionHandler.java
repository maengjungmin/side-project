package org.side.mjm.common.exception;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.side.mjm.common.exception.code.CommonErrorCode;
import org.side.mjm.common.exception.code.ErrorCode;
import org.side.mjm.common.exception.custom.ServiceException;
import org.side.mjm.common.exception.custom.UnauthorizedException;
import org.side.mjm.common.response.structure.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.SQLDataException;
import java.util.List;
import java.util.StringJoiner;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @Value("${spring.profiles.active}")
    private String active;

    @ExceptionHandler(value = {ServiceException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) //추가 시 Swagger Response 에 등록됨.
    public ResponseEntity<ErrorResponse> handleCheckedException(ServiceException e) {
        return handleExceptionInternal(e.errorCode, e);
    }

    @ExceptionHandler(value = {IOException.class, IllegalArgumentException.class, NullPointerException.class})
    public ResponseEntity<ErrorResponse> handleUncheckedException(Exception e) {
        CommonErrorCode errorCode = CommonErrorCode.SERVICE_ERROR;
        return handleExceptionInternal(errorCode, e);
    }

    @ExceptionHandler(value = {EntityExistsException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleEntityExistsException(EntityExistsException e) {
        CommonErrorCode errorCode = CommonErrorCode.EXISTING_DATA;
        return handleExceptionInternal(errorCode, e);
    }

    @ExceptionHandler(value = EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException e) {
        CommonErrorCode errorCode = CommonErrorCode.NO_DATA;
        return handleExceptionInternal(errorCode, e);
    }

    @ExceptionHandler(value = UnauthorizedException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException e) {
        return handleExceptionInternal(e.code(), e);
    }

    @ExceptionHandler(value = SQLDataException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ErrorResponse> handleSQLDataException(SQLDataException e) {
        CommonErrorCode errorCode = CommonErrorCode.INVALID_PARAMETER;
        return handleExceptionInternal(errorCode, e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        StringBuilder stringBuilder = new StringBuilder();
        StringJoiner stringJoiner = new StringJoiner(", ");
        LOGGER.error("======================@Valid Exception START======================");
        LOGGER.error("object : {}", e.getBindingResult().getObjectName());
        List<FieldError> fieldList = e.getFieldErrors();
        for (FieldError field : fieldList) {
            stringJoiner.add(field.getField() + ": " + field.getDefaultMessage());
        }
        stringBuilder.append(stringJoiner);
        LOGGER.error(stringBuilder.toString());
        LOGGER.error("======================@Valid Exception End========================");
        ErrorCode errorCode = CommonErrorCode.INVALID_PARAMETER;
        return handleExceptionInternal(errorCode, new InvalidParameterException(stringJoiner.toString()));
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        StringJoiner stringJoiner = new StringJoiner(", ");
        e.getConstraintViolations().forEach(constraintViolation -> {
            constraintViolation.getPropertyPath().forEach(node -> {
                if (node.getKind().name().equals("PROPERTY")) {
                    stringJoiner.add(node.getName());
                    return;
                }
            });
        });
        ErrorCode errorCode = CommonErrorCode.REQUIRED_PARAMETER;
        return handleExceptionInternal(errorCode, new ServiceException(CommonErrorCode.REQUIRED_PARAMETER, stringJoiner.toString()));
    }

    private ResponseEntity<ErrorResponse> handleExceptionInternal(ErrorCode errorCode, Exception e) {
        /* 모든 HTTP Status 코드는 200으로 전달하고 내부 코드를 상세히 전달 */
        return ResponseEntity.ok()
                .header("Content-type", String.valueOf(MediaType.APPLICATION_JSON))
                .body(ErrorResponse.builder()
                        .status(errorCode.getResultCode())
                        .message(errorCode.getResultMsg())
                        .build());
    }
}
