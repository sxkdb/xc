package com.xuecheng.filesystem.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class FileSystemService {

    @Autowired
    private FileSystemRepository fileSystemRepository;

//    @Value("${xuecheng.fastdfs.tracker_servers}")
//    private String tracker_servers;
//
//    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
//    private int connect_timeout_in_seconds;
//
//    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
//    private int network_timeout_in_seconds;
//
//    @Value("${xuecheng.fastdfs.charset}")
//    private String charset;

    //上传图片
    public UploadFileResult upload(MultipartFile multipartFile, String businesskey, String filetag, String metadata) {

        if (multipartFile == null) {
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
        }

        //上传图片到fastDFS
        String fileId = this.uploadFastDFS(multipartFile);
        //将图片信息保存到mongodb中
        FileSystem fileSystem = new FileSystem();
        //文件id
        fileSystem.setFileId(fileId);
        //业务标识
        fileSystem.setBusinesskey(businesskey);
        //标签
        fileSystem.setFiletag(filetag);
        //元数据
        if (StringUtils.isNotBlank(metadata)) {
            try {
                Map map = JSON.parseObject(metadata, Map.class);
                fileSystem.setMetadata(map);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //名字
        fileSystem.setFileName(multipartFile.getOriginalFilename());
        //大小
        fileSystem.setFileSize(multipartFile.getSize());
        //类型
        fileSystem.setFileType(multipartFile.getContentType());
        //执行保存
        fileSystemRepository.save(fileSystem);

        UploadFileResult uploadFileResult = new UploadFileResult(CommonCode.SUCCESS,fileSystem);

        return uploadFileResult;
    }


    private String uploadFastDFS(MultipartFile multipartFile) {
        try {
            //初始化fastDFS配置
            initFastDFSConfig();
            //创建tracker client
            TrackerClient trackerClient = new TrackerClient();
            //获取trackerServer
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取storage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建storage client
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storeStorage);

            //获取文件后缀
            //得到文件原始名称
            String originalFilename = multipartFile.getOriginalFilename();
            //得到后缀的索引值
            int index = originalFilename.lastIndexOf(".") + 1;
            //得到后缀
            String prefix = originalFilename.substring(index);
            String fileId = storageClient1.upload_file1(multipartFile.getBytes(), prefix, null);
            return fileId;
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_SERVERFAIL);
        }
        return null;
    }


    private void initFastDFSConfig() {
        try {
            //这种初始化获取不到连接
//            ClientGlobal.initByTrackers(tracker_servers);
//            ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
//            ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
//            ClientGlobal.setG_charset(charset);
            ClientGlobal.initByProperties("fastdfs-client.properties");
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionCast.cast(FileSystemCode.FS_INITFDFSERROR);
        }

    }


}
