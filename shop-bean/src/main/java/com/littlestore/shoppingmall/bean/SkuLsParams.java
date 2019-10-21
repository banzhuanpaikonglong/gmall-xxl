package com.littlestore.shoppingmall.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class SkuLsParams implements Serializable {

    // skuName = keyword
    String  keyword;

    String catalog3Id;

    String[] valueId;

    int pageNo=1;

    int pageSize=20;


}
