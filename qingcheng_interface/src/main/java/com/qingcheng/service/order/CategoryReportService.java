package com.qingcheng.service.order;


import com.qingcheng.pojo.order.CategoryReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CategoryReportService {
    /*获得昨天的类目统计信息*/
    public List<CategoryReport> categoryReport(LocalDate date);
    /*
    定时任务，定时获得类目信息*/
    public void createDate();

    public List<Map> category1Count(String date1, String date2);

}
