package com.qingcheng.controller.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.order.CategoryReportService;
import com.qingcheng.service.order.OrderService;
import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Component;

@Component
public class OrderTask {

    @Reference
    OrderService orderService;

    @Reference
    CategoryReportService categoryReportService;

    @Scheduled(cron = "2 0/2 * * * ? ")
    public void orderTimeOutLogic(){
            orderService.orderTimeOutLogic();
    }

    /*
        cron 表达式， 表示时间的一个字符串
         有两种方式，6个和7个，但是springtask仅支持6个
         Seconds    Minutes      Hours       DayofMonth        Month           DayofWeek
         秒:0-59   分钟：0-59    小时 0-23    这个月第几天 1-31  几月 1-12        这都第几天 1-7 1表示星期日
         * ：任意值
         / ：开始时间
         - ： 表示范围 5-20
         ：
    * */
    @Scheduled(cron = "0 0 1 * * ?")
    public void createCategoryService(){
        categoryReportService.createDate();
    }
}
