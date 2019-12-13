package com.qingcheng.pojo.entity;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Result implements Serializable {
    private int code;
    private String message;

    public Result(){
        this.code = 0;
        this.message = "success";
    }
}
