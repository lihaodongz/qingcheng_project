package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.CategoryBrandMapper;
import com.qingcheng.dao.CategoryMapper;
import com.qingcheng.dao.SkuMapper;
import com.qingcheng.dao.SpuMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.*;
import com.qingcheng.service.goods.SpuService;
import com.qingcheng.util.IdWorker;
import org.apache.zookeeper.ZooDefs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.Weekend;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = SpuService.class)
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    IdWorker idWorker;
    @Autowired
    CategoryMapper categoryMapper;
    @Autowired
    CategoryBrandMapper categoryBrandMapper;

    /**
     * 返回全部记录
     * @return
     */
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Spu> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectAll();
        return new PageResult<Spu>(spus.getTotal(),spus.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Spu> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Spu> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectByExample(example);
        return new PageResult<Spu>(spus.getTotal(),spus.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Spu findById(String id) {
        return spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param spu
     */
    public void add(Spu spu) {
        spuMapper.insert(spu);
    }

    /**
     * 修改
     * @param spu
     */
    public void update(Spu spu) {
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     *  删除,物理删除
     *
     * @param id
     */
    public void delete(String id) {
      /*  先判断是否逻辑删除
        是：执行物理删除，记录日志
        否：返回信息不能删除*/
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (!"1".equals(spu.getIsDelete())) {
            throw new RuntimeException("当前商品不能删除");
        }
        spuMapper.deleteByPrimaryKey(id);
      /*  日志记录*/
    }

    @Transactional
    public void saveGoods(Goods goods) {
        // 保存一个spu，多个sku
        Spu spu = goods.getSpu();
        if (spu.getId()==null){
            long id = idWorker.nextId();
            spu.setId(id+"");
            spuMapper.insert(spu);
        }else{  /*修改 删除原来sku列表，执行spu修改*/
            Example example = new Example(Sku.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("spuId",spu.getId());
            skuMapper.deleteByExample(example);
            /*update operation*/
            spuMapper.updateByPrimaryKeySelective(spu);
        }
        Date date = new Date();
        List<Sku> skuList = goods.getSkuList();
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());
        for (Sku sku :skuList){
            if (sku.getId()==null){
                sku.setCreateTime(date);
                sku.setId(idWorker.nextId()+"");
            }
            sku.setSpuId(spu.getId());
          /*  不启用规格*/
            if (sku.getSpec()==null || "".equals(sku.getSpec())){
                sku.setSpec("{}");
            }
            //sku=spu+规格值
            String name = spu.getName();

            Map<String,String> spcmap = JSON.parseObject(sku.getSpec(), Map.class);
            for (String value: spcmap.values()){
                name+=" "+value;
            }
            sku.setName(name);
            sku.setUpdateTime(date);
            sku.setCategoryId(spu.getCategory3Id());
            sku.setCategoryName(category.getName());
            sku.setCommentNum(0);
            sku.setSaleNum(0);
            skuMapper.insert(sku);
        }


        //建立分类和品牌关联
        CategoryBrand categoryBrand = new CategoryBrand();
        categoryBrand.setBrandId(spu.getCategory3Id());
        categoryBrand.setCategoryId(spu.getBrandId());
        int count = categoryBrandMapper.selectCount(categoryBrand);
        if (count ==0){
            categoryBrandMapper.insert(categoryBrand);
        }
    }

    public Goods findGoodsById(String id) {
        //先查spu，再查sku
        Spu spu = spuMapper.selectByPrimaryKey(id);
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId",id);
        List<Sku> skus = skuMapper.selectByExample(example);
        Goods goods = new Goods();
        goods.setSpu(spu);
        goods.setSkuList(skus);
        return goods;
    }

    /*
    *  商品审核
    * */
    @Transactional
    public void audit(String id, String status, String message) {
      /*  1. 修改状态【审核状态和商家状态】
        2. 记录商品的审核记录
        3. 记录商品日志
     */
       /* // 先查后改，效率太差，不如直接new 对象 update
        Spu spu = spuMapper.selectByPrimaryKey(id);
        spu.setStatus(status);
        spuMapper.updateByPrimaryKey(spu);*/
       Spu spu = new Spu();
       spu.setId(id);
       spu.setStatus(status);
       if ("1".equals(status)){
            spu.setIsMarketable("1");
       }
       spuMapper.updateByPrimaryKeySelective(spu);
       // 日志 自己实现
    }

 /*   商品下架*/
    public void pull(String id) {
     /*   1. 修改商品上下架状态
        2. 记录日志*/
     Spu spu = new Spu();
     spu.setId(id);
     spu.setIsMarketable("0");
     spuMapper.updateByPrimaryKeySelective(spu);
    /* 2.记录日志*/
    }

 /*   验证商品是否审核通过
    修改商品为商家装填
    记录日志*/
    public  void put(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        /*判断是否通过审核*/
        if(!"1".equals(spu.getStatus())){
            throw new RuntimeException("此商品未通过审核");
        }
        /*设置上架状态*/
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);
        /*记录日志....*/
    }

    /*利用sql 中 in 关键字完成*/
    public int putMany(Long[] ids) {
        Spu spu = new Spu();
        spu.setIsMarketable("1");  /* 上架*/
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));
        criteria.andEqualTo("isMarketable","0");
        criteria.andEqualTo("status","1");
        criteria.andEqualTo("isDelete","0");
        return spuMapper.updateByExampleSelective(spu,example);
    }

    public int pullMany(Long[] ids) {
        Spu spu = new Spu();
        spu.setIsMarketable("0");
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("isMarketable","1");
        criteria.andEqualTo("status","1");
        criteria.andEqualTo("isDelete","0");
        return spuMapper.updateByExampleSelective(spu,example);
    }

    /**
     * 逻辑删除
     * isDelete =1
     * @param id
     */
    public void deleteLogic(String id) {
       /* 判断当前商品是否已经删除 [判断是版本1 ，最终直接修改，不判断。判断必须先查，效率不高，省去一个查询的sql时间]
        是: 返回 否 删除，记录日志*/
     Spu spu = new Spu();
     spu.setId(id);
     spu.setIsDelete("1");
     spuMapper.updateByPrimaryKeySelective(spu);

    }

    /**
     * 逻辑回收
     * isDelete =0
     * @param id
     */
    public void recycleLogic(String id) {
        Spu spu = new Spu();
        spu.setId(id);
        spu.setIsDelete("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 主键
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andLike("id","%"+searchMap.get("id")+"%");
            }
            // 货号
            if(searchMap.get("sn")!=null && !"".equals(searchMap.get("sn"))){
                criteria.andLike("sn","%"+searchMap.get("sn")+"%");
            }
            // SPU名
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
            }
            // 副标题
            if(searchMap.get("caption")!=null && !"".equals(searchMap.get("caption"))){
                criteria.andLike("caption","%"+searchMap.get("caption")+"%");
            }
            // 图片
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
            }
            // 图片列表
            if(searchMap.get("images")!=null && !"".equals(searchMap.get("images"))){
                criteria.andLike("images","%"+searchMap.get("images")+"%");
            }
            // 售后服务
            if(searchMap.get("saleService")!=null && !"".equals(searchMap.get("saleService"))){
                criteria.andLike("saleService","%"+searchMap.get("saleService")+"%");
            }
            // 介绍
            if(searchMap.get("introduction")!=null && !"".equals(searchMap.get("introduction"))){
                criteria.andLike("introduction","%"+searchMap.get("introduction")+"%");
            }
            // 规格列表
            if(searchMap.get("specItems")!=null && !"".equals(searchMap.get("specItems"))){
                criteria.andLike("specItems","%"+searchMap.get("specItems")+"%");
            }
            // 参数列表
            if(searchMap.get("paraItems")!=null && !"".equals(searchMap.get("paraItems"))){
                criteria.andLike("paraItems","%"+searchMap.get("paraItems")+"%");
            }
            // 是否上架
            if(searchMap.get("isMarketable")!=null && !"".equals(searchMap.get("isMarketable"))){
                criteria.andLike("isMarketable","%"+searchMap.get("isMarketable")+"%");
            }
            // 是否启用规格
            if(searchMap.get("isEnableSpec")!=null && !"".equals(searchMap.get("isEnableSpec"))){
                criteria.andLike("isEnableSpec","%"+searchMap.get("isEnableSpec")+"%");
            }
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andLike("isDelete","%"+searchMap.get("isDelete")+"%");
            }
            // 审核状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andLike("status","%"+searchMap.get("status")+"%");
            }

            // 品牌ID
            if(searchMap.get("brandId")!=null ){
                criteria.andEqualTo("brandId",searchMap.get("brandId"));
            }
            // 一级分类
            if(searchMap.get("category1Id")!=null ){
                criteria.andEqualTo("category1Id",searchMap.get("category1Id"));
            }
            // 二级分类
            if(searchMap.get("category2Id")!=null ){
                criteria.andEqualTo("category2Id",searchMap.get("category2Id"));
            }
            // 三级分类
            if(searchMap.get("category3Id")!=null ){
                criteria.andEqualTo("category3Id",searchMap.get("category3Id"));
            }
            // 模板ID
            if(searchMap.get("templateId")!=null ){
                criteria.andEqualTo("templateId",searchMap.get("templateId"));
            }
            // 运费模板id
            if(searchMap.get("freightId")!=null ){
                criteria.andEqualTo("freightId",searchMap.get("freightId"));
            }
            // 销量
            if(searchMap.get("saleNum")!=null ){
                criteria.andEqualTo("saleNum",searchMap.get("saleNum"));
            }
            // 评论数
            if(searchMap.get("commentNum")!=null ){
                criteria.andEqualTo("commentNum",searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
