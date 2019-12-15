package com.qingcheng.pojo.goods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Goods implements Serializable {

    private Spu spu;
    private List<Sku> skuList;
}
