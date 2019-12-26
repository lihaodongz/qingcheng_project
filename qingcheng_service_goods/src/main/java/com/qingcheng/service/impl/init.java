package com.qingcheng.service.impl;

import com.qingcheng.service.goods.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class init implements InitializingBean {

    @Autowired
    CategoryService categoryService;


    public void afterPropertiesSet() throws Exception {
        log.info("缓存预热");
        categoryService.saveCategoryTreeToRedis();
    }


}
