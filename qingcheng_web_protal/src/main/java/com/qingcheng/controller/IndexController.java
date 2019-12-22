package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.business.Ad;
import com.qingcheng.service.business.AdService;
import com.qingcheng.service.goods.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Reference
    private  AdService adService;

    @Reference
    private CategoryService categoryService;

    @RequestMapping("/index")
    public String index(Model model){
        List<Ad> index_lb = adService.findByPosition("web_index_lb");
        List<Map> categoryList = categoryService.findCategoryTree();
        model.addAttribute("categoryList",categoryList);
        model.addAttribute("lbt",index_lb);
        return "index";
    }
}
