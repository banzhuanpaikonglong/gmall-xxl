package com.littlestore.shoppingmall.pay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan("com.littlestore.shoppingmall")
@MapperScan("com.littlestore.shoppingmall.pay.mapper")
public class ShopPaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopPaymentApplication.class, args);
	}

}
