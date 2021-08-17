package com.heima.common.exception;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Log4j2
public class ExceptionCatch {

    //捕获异常
    @ExceptionHandler(Exception.class)
    public ResponseResult exception(Exception e){
        e.printStackTrace();
        //日志
        log.error("catch exception:{}",e.getMessage());
        //返回通用异常
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
    }

    //捕获CustonException异常
    @ExceptionHandler(CustomException.class)
    public ResponseResult exception2(CustomException e){
        e.printStackTrace();
        //日志
        log.error("catch exception:{}",e.getMessage());
        //返回通用异常
        return ResponseResult.errorResult(90999,e.getMessage());
    }

}
