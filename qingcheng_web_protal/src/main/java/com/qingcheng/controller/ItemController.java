package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.Goods;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.goods.Spu;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SpuService;
import com.sun.corba.se.impl.protocol.SpecialMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/item")
public class ItemController {

    @Reference
    SpuService spuService;

    @Reference
    CategoryService categoryService;

    @Value("${pagePath}")
    private String pagePath;

    @Autowired
    private TemplateEngine templateEngine;

    @RequestMapping("/createPage")
    public void createPage(String spuId)   {
      /*  1.查询商品信息
        2.批量生成页面*/
        Goods goods = spuService.findGoodsById(spuId);
        Spu spu = goods.getSpu();
        List<Sku> skuList = goods.getSkuList();

        //查询商品分类
        List<String> categoryList = new ArrayList<>();
        categoryList.add(categoryService.findById(spu.getCategory1Id()).getName());
        categoryList.add(categoryService.findById(spu.getCategory2Id()).getName());
        categoryList.add(categoryService.findById(spu.getCategory3Id()).getName());
        Map<String,String> urlMap = new HashMap<>();
        /*地址列表*/
        for (Sku sku:skuList){
            if ("1".equals(sku.getStatus())){
                String jsonString = JSON.toJSONString(JSON.parseObject(sku.getSpec()), SerializerFeature.MapSortField);
                urlMap.put(jsonString,sku.getId()+".html");
            }
        }
        for (Sku sku :skuList){
            Context context = new Context();
            Map<String,Object> dateModel = new HashMap<>();
            dateModel.put("spu",spu);
            dateModel.put("sku",sku);
            dateModel.put("categoryList",categoryList);
            dateModel.put("skuImages",sku.getImages().split(","));  //sku图片列表
            dateModel.put("spuImages",spu.getImages().split(","));  //spu图片列表
            /*规格参数*/
            Map paraItems = JSON.parseObject(spu.getParaItems());
            Map<String,String> specItems =(Map)JSON.parseObject(sku.getSpec());
            dateModel.put("paraItems",paraItems);
            dateModel.put("specItems",specItems);
            Map<String,List> sepcMap= (Map)JSON.parseObject(spu.getSpecItems());
            for (String key:sepcMap.keySet()){
                List<String> list = sepcMap.get(key);
                List<Map> mapList = new ArrayList<>();
                for (String value:list){
                    Map map = new HashMap();
                    map.put("option",value);
                    if (specItems.get(key).equals(value)){
                        map.put("checked",true);   /*sku规格相同为true，否则为false*/
                    }else {
                        map.put("checked",false);
                    }
                    Map<String,String> spec = (Map)JSON.parseObject(sku.getSpec());  //当前的sku
                    spec.put(key,value);
                    String jsonString = JSON.toJSONString(spec, SerializerFeature.MapSortField);
                    map.put("url",urlMap.get(jsonString));
                    mapList.add(map);
                }
                sepcMap.put(key,mapList);  /*新的替换旧的*/
            }
            dateModel.put("specMap",sepcMap);
            context.setVariables(dateModel);
            File dir = new File(pagePath);
            if (!dir.exists()){
                dir.mkdirs();
            }
            File dest = new File(dir,sku.getId()+".html");
            /*3. 生成页面*/
            PrintWriter printWriter = null;
            try {
                printWriter = new PrintWriter(dest,"UTF-8");
                templateEngine.process("item",context, printWriter);
                log.info("页面生成中"+sku.getId()+".html");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

}
