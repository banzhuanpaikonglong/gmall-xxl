package com.littlestore.shoppingmall.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.littlestore.shoppingmall")
public class ShopItemWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopItemWebApplication.class, args);
	}

}
