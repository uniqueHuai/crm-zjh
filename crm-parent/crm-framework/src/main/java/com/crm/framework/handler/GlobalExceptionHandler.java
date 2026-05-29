package com.crm.framework.handler;

import com.crm.common.enums.ResultCode;
import com.crm.common.exception.BizException;
import com.crm.common.model.R;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice(basePackages = "com.crm")
public class GlobalExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(BizException.class)
    public R<Void> handleBizException(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        return R.failed(e.getCode(), e.getMessage());
    }

    /** 参数校验异常 - @Valid 注解 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return R.failed(ResultCode.INVALID_PARAM, msg);
    }

    /** 参数绑定异常 */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBind(BindException e) {
        String msg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return R.failed(ResultCode.INVALID_PARAM, msg);
    }

    /** 单个参数校验 */
    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return R.failed(ResultCode.INVALID_PARAM, msg);
    }

    /** 缺少请求参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return R.failed(ResultCode.INVALID_PARAM, "缺少参数: " + e.getParameterName());
    }

    /** 参数类型转换错误 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return R.failed(ResultCode.INVALID_PARAM, "参数类型错误: " + e.getName());
    }

    /** 请求体格式错误 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleMessageNotReadable(HttpMessageNotReadableException e) {
        return R.failed(ResultCode.INVALID_PARAM, "请求体格式错误");
    }

    /** 请求方法不支持 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return R.failed(ResultCode.BAD_REQUEST, "请求方法不支持: " + e.getMethod());
    }

    /** 权限不足 */
    @ExceptionHandler(AccessDeniedException.class)
    public R<Void> handleAccessDenied(AccessDeniedException e) {
        return R.failed(ResultCode.FORBIDDEN);
    }

    /** 数据重复 */
    @ExceptionHandler(DuplicateKeyException.class)
    public R<Void> handleDuplicateKey(DuplicateKeyException e) {
        return R.failed(ResultCode.DUPLICATE_DATA, "数据重复，请检查");
    }

    /** 文件大小超限 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public R<Void> handleMaxUpload(MaxUploadSizeExceededException e) {
        return R.failed(ResultCode.FILE_TOO_LARGE, "文件大小超过限制");
    }

    /** 兜底异常 */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return R.failed(ResultCode.INTERNAL_ERROR, "系统繁忙，请稍后重试");
    }
}
