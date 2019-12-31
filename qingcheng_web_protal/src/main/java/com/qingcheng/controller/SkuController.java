package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.goods.SkuService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sku")
@CrossOrigin
public class SkuController {

    @Reference
    SkuService skuService;


    @GetMapping("/price")
    public Integer price(String id){
        return skuService.findPrice(id);
    }


    @GetMapping("/es")
    public String dbToEs(){
         return skuService.addDataToEs();
    }


}
