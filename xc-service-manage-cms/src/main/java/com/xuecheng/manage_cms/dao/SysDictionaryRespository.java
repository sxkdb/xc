package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.system.SysDictionary;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SysDictionaryRespository extends MongoRepository<SysDictionary, String> {

    SysDictionary findSysDictionaryByDType(String type);
}
