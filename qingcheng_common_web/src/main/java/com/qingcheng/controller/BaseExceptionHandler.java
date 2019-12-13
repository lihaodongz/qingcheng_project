package com.qingcheng.controller;

import com.qingcheng.pojo.entity.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice
public class BaseExceptionHandler  {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        System.out.println("");
        return new Result(1,e.getMessage());
    }
}
