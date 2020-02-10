package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.service.PageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class ConsumerPostPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerPostPage.class);


    @Autowired
    private PageService pageService;

    @Autowired
    private CmsPageRepository cmsPageRepository;

    /**
     * 监听两个队列
     * 执行将页面保存到对应的物理路径
     */
    @RabbitListener(queues = {"${xuecheng.mq.queue1}","${xuecheng.mq.queue2}"})
    public void postPage(String msg) {
        //解析消息
        Map map = JSON.parseObject(msg, Map.class);
        String pageId = (String) map.get("pageId");

        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        //如果页面不存在，就不执行保存了，且记录日志
        if (!optional.isPresent()) {
            LOGGER.error("receive cms post page,cmsPage is null:{}", msg.toString());
            return;
        }
        pageService.savePageToServerPath(pageId);
    }


}
