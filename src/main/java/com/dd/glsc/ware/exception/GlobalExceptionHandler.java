package com.dd.glsc.ware.exception;

import com.dd.common.common.BaseResponse;
import com.dd.common.common.BusinessException;
import com.dd.common.common.ErrorCode;
import com.dd.common.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 业务相关错误
     * @param e
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> exceptionHandler(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException", e);
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> map = new HashMap();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            // 获取到错误提示
            String message = fieldError.getDefaultMessage();
            // 获取到错误的属性名字
            String field = fieldError.getField();
            map.put(field, message);
        }
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, map);
    }

    /**
     * 其他错误
     * @param e
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}

