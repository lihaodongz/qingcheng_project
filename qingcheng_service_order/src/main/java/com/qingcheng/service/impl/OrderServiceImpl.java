package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Preconditions;
import com.qingcheng.dao.OrderConfigMapper;
import com.qingcheng.dao.OrderItemMapper;
import com.qingcheng.dao.OrderLogMapper;
import com.qingcheng.dao.OrderMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.*;
import com.qingcheng.service.order.OrderService;
import com.qingcheng.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = OrderService.class)
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    OrderLogMapper orderLogMapper;
    @Autowired
    OrderConfigMapper orderConfigMapper;

    /**
     * 返回全部记录
     * @return
     */
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Order> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Order> orders = (Page<Order>) orderMapper.selectAll();
        return new PageResult<Order>(orders.getTotal(),orders.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Order> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return orderMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Order> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Order> orders = (Page<Order>) orderMapper.selectByExample(example);
        return new PageResult<Order>(orders.getTotal(),orders.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Order findById(String id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param order
     */
    public void add(Order order) {
        orderMapper.insert(order);
    }

    /**
     * 修改
     * @param order
     */
    public void update(Order order) {
        orderMapper.updateByPrimaryKeySelective(order);
    }

    /**
     *  删除
     * @param id
     */
    public void delete(String id) {
        orderMapper.deleteByPrimaryKey(id);
    }

    /**
     * id查询orders 对象
     * 查询对象，然后组合
     * @param id
     * @return
     */
    public Orders selectOrdersById(String id) {
        Orders orders = new Orders();
        Order order = orderMapper.selectByPrimaryKey(id);
        Example example = new Example(OrderItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderId",order.getId());
        List<OrderItem> orderItems = orderItemMapper.selectByExample(example);
        orders.setOrder(order);
        orders.setOrderItems(orderItems);
        return orders;
    }

    public List<Order> findByIds(Map<String,String[]> searchMap) {
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap.get("ids")!=null){
            criteria.andIn("id", Arrays.asList(searchMap.get("ids")));
            return orderMapper.selectByExample(example);
        }
         throw new  RuntimeException("传入参数不合法，或为空");
    }

    /*批量发货*/
    @Transactional
    public void betchSend(List<Order> orders) {
       /* 1. 参数校验[循环判断是否存在null]
          2. 根据参数修改db的状态
                订单状态，发货状态，发货时间
          3. 生成记录记录
       */
     /*  Order orderItem = null; 直接操作order对象即可，*/
        Preconditions.checkNotNull(orders);
       for (Order order :orders) {
           if (order.getShippingCode() == null || order.getShippingName() == null) {
               throw new RuntimeException("请填写快递单号和选择快递公司");
           }
       }
       //循环订单处理
       for(Order order:orders){
            order.setOrderStatus("3");
            order.setConsignStatus("2");
            order.setConsignTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
            OrderLog orderLog = new OrderLog();
            orderLog.setId(String.valueOf(IdWorker.getId()));
            orderLog.setConsignStatus("2");
            orderLog.setOperateTime(new Date());
            orderLog.setOperater("admin");
            orderLog.setOrderStatus("3");
            orderLog.setOrderId(order.getId());
            orderLogMapper.insert(orderLog);
       }
    }

   /* 订单超时处理,1.拿到配置的超时时间，2填充task，3完成逻辑判断*/
    public void orderTimeOutLogic() {
        OrderConfig orderConfig = orderConfigMapper.selectByPrimaryKey(1);
        Integer orderTimeout = orderConfig.getOrderTimeout();
        LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(orderTimeout);  //拿到超时时间点
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andLessThan("createTime",localDateTime);
        criteria.andEqualTo("orderStatus","0");
        criteria.andEqualTo("isDelete","0");
        List<Order> orders = orderMapper.selectByExample(example);
        for (Order order:orders){
            /*日志记录*/
            order.setOrderStatus("4");
            order.setCloseTime(new Date());
            generatorOrderLog(order,"systemadMin","订单超时，系统自动关闭");
            orderMapper.updateByPrimaryKeySelective(order);
        }

    }

    @Override
    public void mergeOrder(String order1, String order2) {
        if (order1 ==null || "".equals(order1) || order2==null || "".equals(order2)){
            throw new RuntimeException("订单号存在异常");
        }
        // zorder 主订单  forder 副订单  订单合并需要合并订单的数量和金额信息.优惠金额和实际支付金额
        Order zorder = orderMapper.selectByPrimaryKey(order1);
        Order forder = orderMapper.selectByPrimaryKey(order2);
        zorder.setTotalNum(zorder.getTotalNum()+forder.getTotalNum());
        zorder.setTotalMoney(zorder.getTotalMoney()+forder.getTotalMoney());
        zorder.setPayMoney(zorder.getPreMoney()+forder.getPreMoney());
        zorder.setPayMoney(zorder.getPayMoney()+forder.getPayMoney());
        forder.setIsDelete("1");
        orderMapper.updateByPrimaryKeySelective(zorder);
        //日志记录到orderlog中
        generatorOrderLog(zorder,"systenadMin","订单合并");
    }


    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 订单id
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andLike("id","%"+searchMap.get("id")+"%");
            }
            // 支付类型，1、在线支付、0 货到付款
            if(searchMap.get("payType")!=null && !"".equals(searchMap.get("payType"))){
                criteria.andLike("payType","%"+searchMap.get("payType")+"%");
            }
            // 物流名称
            if(searchMap.get("shippingName")!=null && !"".equals(searchMap.get("shippingName"))){
                criteria.andLike("shippingName","%"+searchMap.get("shippingName")+"%");
            }
            // 物流单号
            if(searchMap.get("shippingCode")!=null && !"".equals(searchMap.get("shippingCode"))){
                criteria.andLike("shippingCode","%"+searchMap.get("shippingCode")+"%");
            }
            // 用户名称
            if(searchMap.get("username")!=null && !"".equals(searchMap.get("username"))){
                criteria.andLike("username","%"+searchMap.get("username")+"%");
            }
            // 买家留言
            if(searchMap.get("buyerMessage")!=null && !"".equals(searchMap.get("buyerMessage"))){
                criteria.andLike("buyerMessage","%"+searchMap.get("buyerMessage")+"%");
            }
            // 是否评价
            if(searchMap.get("buyerRate")!=null && !"".equals(searchMap.get("buyerRate"))){
                criteria.andLike("buyerRate","%"+searchMap.get("buyerRate")+"%");
            }
            // 收货人
            if(searchMap.get("receiverContact")!=null && !"".equals(searchMap.get("receiverContact"))){
                criteria.andLike("receiverContact","%"+searchMap.get("receiverContact")+"%");
            }
            // 收货人手机
            if(searchMap.get("receiverMobile")!=null && !"".equals(searchMap.get("receiverMobile"))){
                criteria.andLike("receiverMobile","%"+searchMap.get("receiverMobile")+"%");
            }
            // 收货人地址
            if(searchMap.get("receiverAddress")!=null && !"".equals(searchMap.get("receiverAddress"))){
                criteria.andLike("receiverAddress","%"+searchMap.get("receiverAddress")+"%");
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if(searchMap.get("sourceType")!=null && !"".equals(searchMap.get("sourceType"))){
                criteria.andLike("sourceType","%"+searchMap.get("sourceType")+"%");
            }
            // 交易流水号
            if(searchMap.get("transactionId")!=null && !"".equals(searchMap.get("transactionId"))){
                criteria.andLike("transactionId","%"+searchMap.get("transactionId")+"%");
            }
            // 订单状态
            if(searchMap.get("orderStatus")!=null && !"".equals(searchMap.get("orderStatus"))){
                criteria.andLike("orderStatus","%"+searchMap.get("orderStatus")+"%");
            }
            // 支付状态
            if(searchMap.get("payStatus")!=null && !"".equals(searchMap.get("payStatus"))){
                criteria.andLike("payStatus","%"+searchMap.get("payStatus")+"%");
            }
            // 发货状态
            if(searchMap.get("consignStatus")!=null && !"".equals(searchMap.get("consignStatus"))){
                criteria.andLike("consignStatus","%"+searchMap.get("consignStatus")+"%");
            }
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andLike("isDelete","%"+searchMap.get("isDelete")+"%");
            }

            // 数量合计
            if(searchMap.get("totalNum")!=null ){
                criteria.andEqualTo("totalNum",searchMap.get("totalNum"));
            }
            // 金额合计
            if(searchMap.get("totalMoney")!=null ){
                criteria.andEqualTo("totalMoney",searchMap.get("totalMoney"));
            }
            // 优惠金额
            if(searchMap.get("preMoney")!=null ){
                criteria.andEqualTo("preMoney",searchMap.get("preMoney"));
            }
            // 邮费
            if(searchMap.get("postFee")!=null ){
                criteria.andEqualTo("postFee",searchMap.get("postFee"));
            }
            // 实付金额
            if(searchMap.get("payMoney")!=null ){
                criteria.andEqualTo("payMoney",searchMap.get("payMoney"));
            }

        }
        return example;
    }

    //记录订单修改的操作，order表属性赋值结束之后生成订单
    private void  generatorOrderLog(Order order, String opreator, String remark){
        OrderLog orderLog =new OrderLog();
        orderLog.setId(String.valueOf(IdWorker.getId()));
        orderLog.setOperater(opreator);
        orderLog.setOperateTime(new Date());
        orderLog.setOrderStatus(order.getOrderStatus());
        orderLog.setPayStatus(order.getPayStatus());
        orderLog.setConsignStatus(order.getConsignStatus());
        orderLog.setRemarks(remark);
        orderLog.setOrderId(order.getId());
        orderLogMapper.insert(orderLog);
    }
}


