package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsSite;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;

@Api(value = "cms页面站点管理接口", description = "cms页面站点管理接口,提供对站点的增、删、改、查")
public interface CmsSiteControllerApi {

    @ApiOperation("查询所有站点")
    List<CmsSite> findList();


}
