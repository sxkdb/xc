package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.ext.CategoryNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "类别管理接口", description = "类别管理接口,提供类别的增、删、改、查")
public interface CategoryControllerApi {

    @ApiOperation("查询所有数据")
    CategoryNode findCategorys();

}
