package com.littlestore.shoppingmall.pay.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.littlestore.shoppingmall.bean.OrderInfo;
import com.littlestore.shoppingmall.bean.PaymentInfo;
import com.littlestore.shoppingmall.config.LoginRequire;
import com.littlestore.shoppingmall.enums.PaymentStatus;
import com.littlestore.shoppingmall.pay.config.AlipayConfig;
import com.littlestore.shoppingmall.service.OrderService;
import com.littlestore.shoppingmall.service.PaymentService;
import org.apache.ibatis.binding.MapperMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xxl
 * @create 2019-07-19 19:14
 */
@Controller
public class PayMentController {

    @Reference
    private OrderService orderService;
    @Autowired
    private AlipayClient alipayClient;
    @Reference
    private PaymentService paymentService;

    @RequestMapping("index")
    @LoginRequire
    public String index(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        //保存总金额
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        //保存订单Id
        request.setAttribute("orderId",orderId);
        return "index";
    }

    @RequestMapping("alipay/submit")
    @ResponseBody
    public String alipaySubmit(HttpServletRequest request, HttpServletResponse response){
        // 将支付信息保存到数据库 对应的数据库表：paymentInfo
        String orderId = request.getParameter("orderId");

        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        //创建交易对象
        PaymentInfo paymentInfo = new PaymentInfo();
        //给交易对象赋值
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
//        paymentInfo.setSubject(orderInfo.getTradeBody());
        paymentInfo.setSubject("啦啦啦啦啦");
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());

        //保存交易记录
        paymentService.savePaymentInfo(paymentInfo);

        // 支付 生成二维码
        // AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE); //获得初始化的AlipayClient
        // 将上述创建对象的方式放入spring 容器中！

        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        //同步回调路径
        alipayRequest.setReturnUrl(AlipayConfig.return_order_url);
        //异步回调路径  setNotifyUrl
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址
        // 封装参数
        // 使用map 记录数据
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("product_code","FAST_TNSTANT_TRADE_PAY");
        map.put("total_amount",paymentInfo.getTotalAmount());
        map.put("subject",paymentInfo.getSubject());

        alipayRequest.setBizContent(JSON.toJSONString(map));

//        alipayRequest.setBizContent("{" +
//                "    \"out_trade_no\":\"20150320010101001\"," +
//                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
//                "    \"total_amount\":88.88," +
//                "    \"subject\":\"Iphone6 16G\"," +
//                "    \"body\":\"Iphone6 16G\"," +
//                "    \"passback_params\":\"merchantBizType%3d3C%26merchantBizNo%3d2016010101111\"," +
//                "    \"extend_params\":{" +
//                "    \"sys_service_provider_id\":\"2088511833207846\"" +
//                "    }"+
//                "  }");//填充业务参数
                String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");
//        response.getWriter().write(form);//直接将完整的表单html输出到页面
//        response.getWriter().flush();
//        response.getWriter().close();
        return  form;

    }

    @RequestMapping("alipay/callback/return")
    public String callback(){
        //重定向到订单url
        return "redirect:"+AlipayConfig.return_order_url;
    }
    /**
     * 异步回调 通知商家支付结果！
     * https://商家网站通知地址?voucher_detail_list=[{"amount":"0.20","merchantContribute":"0.00","name":"5折券","otherContribute":"0.20","type":"ALIPAY_DISCOUNT_VOUCHER","voucherId":"2016101200073002586200003BQ4"}]&fund_bill_list=[{"amount":"0.80","fundChannel":"ALIPAYACCOUNT"},{"amount":"0.20","fundChannel":"MDISCOUNT"}]&subject=PC网站支付交易&trade_no=2016101221001004580200203978&gmt_create=2016-10-12 21:36:12&notify_type=trade_status_sync&total_amount=1.00&out_trade_no=mobile_rdm862016-10-12213600&invoice_amount=0.80&seller_id=2088201909970555&notify_time=2016-10-12 21:41:23&trade_status=TRADE_SUCCESS&gmt_payment=2016-10-12 21:37:19&receipt_amount=0.80&passback_params=passback_params123&buyer_id=2088102114562585&app_id=2016092101248425&notify_id=7676a2e1e4e737cff30015c4b7b55e3kh6& sign_type=RSA2&buyer_pay_amount=0.80&sign=***&point_amount=0.00
     */
    @RequestMapping("alipay/callback/notify")
    public String callbackNotify(@RequestParam Map<String,String> paramMap , HttpServletRequest request){

        boolean flag = false;

        try {
            flag = AlipaySignature.rsaCheckV1(paramMap,AlipayConfig.alipay_public_key,AlipayConfig.charset,AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        // 在支付宝中有该笔交易
        if (flag) {
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            // `只有交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED 时，支付宝才会认定为买家付款成功。
            String trade_status = paramMap.get("trade_status");
            if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
                // 付款成功之后 修改交易状态
                // 如果交易状态为 PAID 或者是 CLOSE
                // 查询当前交易支付状态 获取第三方交易编号，通过第三方交易编号，查询交易记录对象
                String out_trade_no = paramMap.get("out_reade_no");

                // select * from paymentInfo where out_trade_no=?
                PaymentInfo paymentInfoQuery = new PaymentInfo();
                paymentInfoQuery.setOutTradeNo(out_trade_no);
                // 通过第三方交易编号，查询交易记录对象.
                PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQuery);

                //判断状态
                if (paymentInfo.getPaymentStatus() == PaymentStatus.ClOSED ||
                        paymentInfo.getPaymentStatus() == PaymentStatus.PAID){
                    //异常
                    return "failure";

                }

                // 修改交易状态
                // update paymentInfo set PaymentStatus=PaymentStatus.IPAD where out_trade_no = ？
                // upadte paymentInfo set PaymentStatus=PaymentStatus.IPAD where id = paymentInfo.getId();

                // 调用更新
                PaymentInfo paymentInfoUPD = new PaymentInfo();
                paymentInfoUPD.setPaymentStatus(PaymentStatus.PAID);
                paymentInfoUPD.setCallbackTime(new Date());

                //更新
                paymentService.updatePaymentInfo(paymentInfoUPD,out_trade_no);

                //通知订单支付成功？ ActiveMQ 发送消息队列

                return "success";
            }
        }else {
            //TODO 验签失败则记录异常日志，并在response中返回failure。
            return "failure";
        }
        return "failure";
    }

    @RequestMapping("refund")
    @ResponseBody
    public boolean refund(String orderId){
        //直接调用退款接口
        boolean flag = paymentService.refund(orderId);
        return flag;
    }

    @RequestMapping("wx/submit")
    @ResponseBody
    public Map createNative(String orderId){
        //调用服务层数据
        //第一个参数是订单Id，第二个参数是多少钱，单位是分
        Map map = paymentService.createNative(orderId + "", "1");
        System.out.println(map.get("code_url"));
        //data = map
        return map;
    }

    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo,String result){
        paymentService.sendPaymentResult(paymentInfo,result);
        return "OK";
    }







}
