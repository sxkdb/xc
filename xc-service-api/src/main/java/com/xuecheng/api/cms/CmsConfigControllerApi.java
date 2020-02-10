package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

@Api(value = "cms配置管理接口", description = "cms配置管理接口,提供对数据模型的增、删、改、查")
public interface CmsConfigControllerApi {

    @ApiOperation("根据id查询cms配置信息")
    @ApiImplicitParam(name = "id",value = "cmsConfig的id",paramType = "path",required = true,dataType = "stirng")
    CmsConfig getModel(String id);



}
