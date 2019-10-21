package com.littlestore.shoppingmall.item.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.littlestore.shoppingmall.bean.SkuInfo;
import com.littlestore.shoppingmall.bean.SkuSaleAttrValue;
import com.littlestore.shoppingmall.bean.SpuSaleAttr;
import com.littlestore.shoppingmall.config.LoginRequire;
import com.littlestore.shoppingmall.service.ListService;
import com.littlestore.shoppingmall.service.ManageService;
import org.apache.http.impl.bootstrap.HttpServer;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

/**
 * @author xxl
 * @create 2019-07-08 19:34
 */
@Controller
public class ItemController {

    @Reference
    private ManageService manageService;

    @Reference
    private ListService listService;

    @RequestMapping("{skuId}.html")
//    @LoginRequire
    public String item(@PathVariable String skuId , HttpServletRequest request){
        //根据spuId查询页面需要的数据
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        //根据查询出来的skuInfo结果，调用其中的skuId，spuId查询销售属性集合，并锁定销售属性值*
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrListCheckBySku(skuInfo);

        // 根据spuId 查询与skuId 有关的销售属性值集合
        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu = manageService.getSkuSaleAttrValueListBySpu(skuId);
        //开始拼接json字符串
        String key = "";
        HashMap<Object, Object> map = new HashMap<>();
        for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);
            if (key.length() > 0){
                key += "|";
            }
            key += skuSaleAttrValue.getSaleAttrValueId();

            if (i+1 == skuSaleAttrValueListBySpu.size() ||
                    !skuSaleAttrValue.getId().equals(skuSaleAttrValueListBySpu.get(i+1))){
                map.put(key,skuSaleAttrValue.getSkuId());
                key = "";
            }
        }
        String valuesSkuJson = JSON.toJSONString(map);
        request.setAttribute("valuesSkuJson",valuesSkuJson);

        //保存锁定销售属性值
        request.setAttribute("spuSaleAttrList",spuSaleAttrList);
        //页面数据
        request.setAttribute("skuInfo",skuInfo);

        //调用热度排名
//        listService.

        return "item";
    }



}
