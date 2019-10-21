package com.littlestore.shoppingmall.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;


/**
 * @author shkstart
 * @create 2019-07-01 20:50
 */
@Data
public class UserInfo implements Serializable {

    @Id
    @Column// mysql=IDENTITY 表示获取主键自增 oracle=AUTO
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column
    private String loginName;
    @Column
    private String nickName;
    @Column
    private String passwd;
    @Column
    private String name;
    @Column
    private String phoneNum;
    @Column
    private String email;
    @Column
    private String headImg;
    @Column
    private String userLevel;
}
