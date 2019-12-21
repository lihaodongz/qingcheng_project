package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.order.CategoryReport;
import com.qingcheng.service.order.CategoryReportService;
import javassist.LoaderClassPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/categoryReport")
public class CategoryReportController {
    @Reference
    CategoryReportService categoryReportService;


    @GetMapping("/yesterday")
    public List<CategoryReport> yesterday(){
      LocalDate localDate = LocalDate.now().minusDays(1);
        return categoryReportService.categoryReport(localDate);
    }


    @GetMapping("/category1Count")
    public List<Map> category1Count(String date1,String date2){
        return categoryReportService.category1Count(date1,date2);
    }

}
