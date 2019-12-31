package com.qingcheng.dao;

import com.qingcheng.pojo.goods.Sku;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuMapper extends Mapper<Sku> {

    @Select("SELECT id,name,image,brand_name,category_name,price,create_time,sale_num,comment_num,spec FROM tb_sku limit 50 ")
    public List<Sku> find50();

}
