package com.littlestore.shoppingmall.shopmanageservice.mapper;

import com.littlestore.shoppingmall.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author shkstart
 * @create 2019-07-08 0:13
 */
public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {

    /**
     * 根据spuId查询skuId相关的销售属性集合
     * @param spuId
     * @return
     */
    List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu(String spuId);
}
