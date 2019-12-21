package com.qingcheng.service.order;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.pojo.order.Orders;

import java.rmi.MarshalledObject;
import java.util.*;

/**
 * order业务逻辑层
 */
public interface OrderService {


    public List<Order> findAll();


    public PageResult<Order> findPage(int page, int size);


    public List<Order> findList(Map<String,Object> searchMap);


    public PageResult<Order> findPage(Map<String,Object> searchMap,int page, int size);


    public Order findById(String id);

    public void add(Order order);


    public void update(Order order);


    public void delete(String id);


    /*订单信息查询 包括去order 表和 orderitem表分别查询，组装Vo信息*/
    public Orders selectOrdersById(String id);


    public List<Order> findByIds(Map<String,String[]> searchMap);


    /*批量发货*/
    public void  betchSend(List<Order> orders);

    /*订单超时自动处理*/
    public void orderTimeOutLogic();

    /* 订单合并*/
    public void mergeOrder(String order1,String order2);


}
