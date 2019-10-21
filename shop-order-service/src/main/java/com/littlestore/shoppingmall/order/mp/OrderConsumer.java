package com.littlestore.shoppingmall.order.mp;

import com.alibaba.dubbo.config.annotation.Reference;
import com.littlestore.shoppingmall.enums.ProcessStatus;
import com.littlestore.shoppingmall.service.OrderService;
import org.apache.commons.codec.language.MatchRatingApproachEncoder;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * @author xxl
 * @create 2019-07-24 18:24
 */
@Component
public class OrderConsumer {

    @Reference
    private OrderService orderService;


    //destination 描述表示监听的队列
    //containerFactory 表示监听的工厂
    @JmsListener(destination = "PAYMENT_RESULT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(MapMessage mapMessage) throws JMSException {
        //获取消息队列中的数据
        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");

        //支付成功之后
        if ("success".equals(result)){
            //修改订单状态
            orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
            //发送消息给库存
            orderService.sendOrderStatus(orderId);
            //更新订单状态
            orderService.updateOrderStatus(orderId,ProcessStatus.NOTIFIED_WARE);
        }
    }

    @JmsListener(destination = "SKU_DEDUCT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
        //获取消息 队列中的数据
        String orderId = mapMessage.getString("orderId");
        String status = mapMessage.getString("status");
        //表示已经减库存成功
        if ("DEDUCTED".equals(status)){
            //更新订单状态
            orderService.updateOrderStatus(orderId,ProcessStatus.DELEVERED);
        }
    }



}
