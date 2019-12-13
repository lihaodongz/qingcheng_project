package com.qingcheng.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PageResult<T> implements Serializable {
    private Long total;
    private List<T> rows;

}
