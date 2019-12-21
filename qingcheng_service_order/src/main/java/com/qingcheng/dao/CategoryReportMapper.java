package com.qingcheng.dao;

import com.qingcheng.pojo.order.CategoryReport;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CategoryReportMapper  extends Mapper<CategoryReport> {


    @Select("SELECT\n" +
            "\tcategory_id1 categoryID1,\n" +
            "\tcategory_id2 categoryID2,\n" +
            "\tcategory_id3 categoryID3,\n" +
            "\tDATE_FORMAT( pay_time, '%Y-%m-%d' ) countDate,\n" +
            "\tSUM( oi.num ) num,\n" +
            "\tSUM( oi.money ) money \n" +
            "FROM\n" +
            "\ttb_order_item oi,\n" +
            "\ttb_order o \n" +
            "WHERE\n" +
            "\toi.id = o.id \n" +
            "\tAND o.pay_status = '1' \n" +
            "\tAND o.is_delete = '0' \n" +
            "\tAND o.is_delete = '0' \n" +
            "\tAND DATE_FORMAT( o.pay_time, '%Y-%m-%d' )=#{date} \n" +
            "GROUP BY\n" +
            "\toi.category_id1,\n" +
            "\toi.category_id2,\n" +
            "\toi.category_id3,\n" +
            "\tDATE_FORMAT(\n" +
            "\t\to.pay_time,\n" +
            "\t'%Y-%m-%d')")
    public List<CategoryReport> categoryReport(@Param("date") LocalDate date);


    @Select("SELECT\n" +
            "\tcategory_id1 categoryId1,c.`NAME` categoryName,\n" +
            "\tSUM( num ) num,\n" +
            "\tSUM( money ) money \n" +
            "FROM\n" +
            "\ttb_category_report r,\n" +
            "\tv_category c \n" +
            "WHERE\n" +
            "\tr.category_id1 = c.id \n" +
            "\tAND count_date >= #{date1} \n" +
            "\tAND count_date <= #{date2} \n" +
            "GROUP BY\n" +
            "\tcategory_id1,\n" +
            "\tc.`name`;")
    public List<Map> category1Count(@Param("date1") String date1,@Param("date2") String date2);


}
