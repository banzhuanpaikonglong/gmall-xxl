package com.littlestore.shoppingmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.littlestore.shoppingmall.bean.UserAddress;
import com.littlestore.shoppingmall.service.UserAddressService;
import com.littlestore.shoppingmall.user.mapper.UserAddressMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author shkstart
 * @create 2019-07-02 20:34
 */
@Service
public class UserAddressServiceImpl implements UserAddressService {


    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public List<UserAddress> getUserAddressList(String userId) {

        UserAddress userAddress = new UserAddress();
        userAddress.setId(userId);
        List<UserAddress> li = userAddressMapper.select(userAddress);
        return  li;
    }


}
