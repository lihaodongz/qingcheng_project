package com.qingcheng.controller;

import com.alibaba.dubbo.common.utils.LogHelper;
import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.Util.WebUtil;
import com.qingcheng.service.goods.SkuSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Slf4j
@Controller
public class SearchController {

    @Reference
    SkuSearchService skuSearchService;


    @GetMapping("/search")
    public String search(Model model, @RequestParam Map<String,String> searchMap) throws Exception {
        searchMap = WebUtil.convertCharsetToUTF8(searchMap);
        try {
            Map result = skuSearchService.search(searchMap);
            model.addAttribute("result",result);
            return "search";
        }catch (NullPointerException e){
            log.info("result is null ");
        }
        return "search";
    }
}

