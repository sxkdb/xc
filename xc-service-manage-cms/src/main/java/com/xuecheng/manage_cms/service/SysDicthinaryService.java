package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.dao.SysDictionaryRespository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class SysDicthinaryService {


    private static final Logger LOGGER = LoggerFactory.getLogger(SysDicthinaryService.class);

    @Autowired
    private SysDictionaryRespository sysDictionaryRespository;

    public SysDictionary getByType(String type) {
        SysDictionary sysDictionary = new SysDictionary();
        sysDictionary.setDType(type);

        if (StringUtils.isBlank(type)) {
            LOGGER.error("SysDicthinaryService getByType method param illegal");
        }
        //TODO 这里用条件查询怎么查不出来
        //ExampleMatcher matcher = ExampleMatcher.matching().withMatcher("d_type", ExampleMatcher.GenericPropertyMatchers.contains());
        //Example<SysDictionary> example = Example.of(sysDictionary, matcher);
        //Optional<SysDictionary> one = sysDictionaryRespository.findOne(example);
        SysDictionary sysDictionaryByDType = sysDictionaryRespository.findSysDictionaryByDType(type);

        if (sysDictionaryByDType == null) {
            LOGGER.error("数据字典中查询的数据为null");
            return null;
        }
        return sysDictionaryByDType;
    }
}
