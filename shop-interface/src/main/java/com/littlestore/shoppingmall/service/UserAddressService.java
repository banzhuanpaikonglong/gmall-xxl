package com.littlestore.shoppingmall.service;

import com.littlestore.shoppingmall.bean.UserAddress;

import java.util.List;

/**
 * @author shkstart
 * @create 2019-07-02 20:33
 */
public interface UserAddressService {
    List<UserAddress> getUserAddressList(String userId);



}
