package com.littlestore.shoppingmall.service;

import com.littlestore.shoppingmall.bean.PaymentInfo;

import java.util.Map;

/**
 * @author xxl
 * @create 2019-07-19 19:19
 */
public interface PaymentService {

    /**
     * 保存支付信息
     * @param paymentInfo
     */
    void savePaymentInfo(PaymentInfo paymentInfo);

    /**
     * 根据对象中的属性查询数据
     * @param paymentInfoQuery
     * @return
     */
    PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);

    /**
     *  更新数据
     * @param paymentInfoUPD
     * @param out_trade_no
     */
    void updatePaymentInfo(PaymentInfo paymentInfoUPD, String out_trade_no);

    /**
     *  退款接口
     * @param orderId
     * @return
     */
    boolean refund(String orderId);

    /**
     * 生产
     * @param orderId
     * @param total_fee
     * @return
     */
    Map createNative(String orderId, String total_fee);


    /**
     * 支付成功之后，将订单编号，支付结果发送给订单
     * @param paymentInfo
     * @param result
     */
    void sendPaymentResult(PaymentInfo paymentInfo, String result);



}
