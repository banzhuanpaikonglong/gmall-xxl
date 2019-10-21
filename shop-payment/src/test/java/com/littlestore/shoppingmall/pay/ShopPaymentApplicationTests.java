package com.littlestore.shoppingmall.pay;

import com.littlestore.shoppingmall.pay.config.ActiveMQUtil;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ShopPaymentApplicationTests {

	@Autowired
	private ActiveMQUtil activeMQUtil;

	@Test
	public void contextLoads() {
	}

	@Test
	public void activeMq() throws JMSException {

//		ActiveMQConnectionFactory activeMQConnectionFactory =
//				new ActiveMQConnectionFactory("tcp://192.168.26.171:61616");
//		Connection connection = activeMQConnectionFactory.createConnection();

		Connection connection = activeMQUtil.getConnection();
		connection.start();
		//第一个参数表示是否开启事物
		//第二个参数表示签收方式跟第一个事物有关系

//		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Session session = connection.createSession(true,Session.SESSION_TRANSACTED);
		Queue hello = session.createQueue("hello");

		MessageProducer producer = session.createProducer(hello);
		ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
		activeMQTextMessage.setText("略略略略略！！！");

//		producer.setDeliveryMode(DeliveryMode.PERSISTENT);
		producer.send(activeMQTextMessage);

		//提交事务
		session.commit();

		producer.close();
		session.close();
		connection.close();

	}


}
