package com.xuecheng.api.fileSystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "系统文件管理接口", description = "系统文件理接口,提供类别的增、删、改、查")
public interface FileSystemControllerApi {

    @ApiOperation("上传文件")
    UploadFileResult upload(MultipartFile multipartFile, String businesskey, String filetag, String metadata);

}
