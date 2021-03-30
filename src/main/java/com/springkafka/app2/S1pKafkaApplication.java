/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springkafka.app2;

import com.springkafka.CommonConfiguration;
import com.springkafka.ConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 发送一条消息并消费
 * @author Gary Russell
 *
 */
@SpringBootApplication
@Import({ CommonConfiguration.class, ConfigProperties.class })
@EnableKafka
public class S1pKafkaApplication {

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = new SpringApplicationBuilder(S1pKafkaApplication.class)
			.web(WebApplicationType.NONE)
			.run(args);
		TestBean testBean = context.getBean(TestBean.class);
		testBean.send("foo2");
		context.getBean(Listener.class).latch.await(60, TimeUnit.SECONDS);
		context.close();
	}

	@Bean
	public TestBean test() {
		return new TestBean();
	}

	@Bean
	public Listener listener() {
		return new Listener();
	}

	public static class TestBean {

		@Autowired
		private ConfigProperties configProperties;

		@Autowired
		private KafkaTemplate<String, String> template;

        /**
         * 往 kafka 的某 topic 发送一条消息
         * @param foo
         */
		public void send(String foo) {
			this.template.send(this.configProperties.getTopic(), foo);
		}

	}

	public static class Listener {

		private final CountDownLatch latch = new CountDownLatch(1);

        /**
         * 使用 @KafkaListener 注解来创建一个消费者,实现对 Kafka 消息的消费。
         * @param foo
         */
		@KafkaListener(topics = "${kafka.topic}")
		public void listen(String foo) {
			System.out.println("------------------ Received ------------------: " + foo);
			this.latch.countDown();
		}

	}

}
