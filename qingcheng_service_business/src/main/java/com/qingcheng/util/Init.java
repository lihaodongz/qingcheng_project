package com.qingcheng.util;

import com.qingcheng.service.business.AdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Init implements InitializingBean {


    @Autowired
    AdService adService;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("缓存预热");
        adService.saveAllToRedis();
    }
}
