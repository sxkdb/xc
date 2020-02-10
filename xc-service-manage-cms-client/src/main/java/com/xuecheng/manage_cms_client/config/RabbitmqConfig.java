package com.xuecheng.manage_cms_client.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

    // 交换机的名称
    public static final String EX_ROUTING_CMS_POSTPAGE = "ex_routing_cms_postpage";

    //队列bean的名称
    public static final String QUEUE_CMS_POSTPAGE = "queue_cms_postpage";

    //队列bean的名称
    public static final String QUEUE_COURSEDETAILS_POSTPAGE = "queue_coursedetails_postpage";

    // 队列的名称
    @Value("${xuecheng.mq.queue1}")
    public String queue_cms_postpage_name;

    //routingKey 即站点Id
    @Value("${xuecheng.mq.routingKey1}")
    public String cms_routingKey;

    // 队列的名称
    @Value("${xuecheng.mq.queue2}")
    public String queue_coursedetails_postpage_name;

    //routingKey 即站点Id
    @Value("${xuecheng.mq.routingKey2}")
    public String course_routingKey;

    //声明交换机
    @Bean(EX_ROUTING_CMS_POSTPAGE)
    public Exchange EX_ROUTING_CMS_POSTPAGE() {
        return ExchangeBuilder.directExchange(EX_ROUTING_CMS_POSTPAGE).durable(true).build();
    }

    //声明队列
    @Bean(QUEUE_CMS_POSTPAGE)
    public Queue QUEUE_CMS_POSTPAGE() {
        return QueueBuilder.durable(queue_cms_postpage_name).build();
    }

    //声明队列
    @Bean(QUEUE_COURSEDETAILS_POSTPAGE)
    public Queue QUEUE_COURSEDETAILS_POSTPAGE(){
        return QueueBuilder.durable(queue_coursedetails_postpage_name).build();
    }

    //绑定队列
    @Bean
    public Binding BINDING_QUEUE_INFORM_SMS(@Qualifier(QUEUE_CMS_POSTPAGE) Queue queue, @Qualifier(EX_ROUTING_CMS_POSTPAGE) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(cms_routingKey).noargs();
    }

    //绑定队列
    @Bean
    public Binding BINDING_COURSEDETAILS_INFORM_SMS(@Qualifier(QUEUE_COURSEDETAILS_POSTPAGE) Queue queue,@Qualifier(EX_ROUTING_CMS_POSTPAGE) Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(course_routingKey).noargs();
    }

}
