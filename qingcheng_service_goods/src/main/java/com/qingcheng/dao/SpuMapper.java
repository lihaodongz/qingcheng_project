package com.qingcheng.dao;

import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.goods.Spu;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SpuMapper extends Mapper<Spu> {

}
