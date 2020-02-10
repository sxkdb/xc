package com.xuecheng.manage_cms_client.dao;

import com.xuecheng.framework.domain.cms.CmsConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CmsConfigRespository extends MongoRepository<CmsConfig, String> {
}
