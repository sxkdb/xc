package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class MediaUploadService {

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //文件上传路径
    @Value("${xc-service-manage-media.upload-location}")
    private String UPLOAD_LOCATION;


    //视频处理路由
    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    public  String routingkey_media_video;
    /**
     * 文件上传前的注册，检查文件是否存在
     * 根据文件md5得到文件路径
     * 规则：
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     * 文件名：md5+文件扩展名
     *
     * @param fileMd5 文件md5值
     * @param fileExt 文件扩展名
     * @return 文件路径
     */
    public ResponseResult register(String fileMd5, String fileName, String mimetype, String fileExt) {

        //1.检查文件在磁盘上是否存在
        //文件的目录
        String fileFolderPath = getFileFolderPath(fileMd5);
        //文件地址
        String filePath = getFilePath(fileMd5, fileExt);
        File file = new File(filePath);
        boolean exists = file.exists();//文件是否存在的标志

        //2.检查文件在mongodb中是否存在

        boolean existsById = mediaFileRepository.existsById(fileMd5);

        if (exists && existsById) {
            //如果文件在磁盘上存在且在mongodb上有记录信息，则不需要再上传
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }

        //文件不存在时准备工作
        //检查目录是否存在，如果不存在，则新建目录
        File file1 = new File(fileFolderPath);
        if (!file1.exists()) {
            file1.mkdirs();
        }

        return new ResponseResult(CommonCode.SUCCESS);

    }


    /**
     * 分块检查
     * 校验分块是否存在，如果存在，就不上传
     *
     * @param fileMd5
     * @param chunk
     * @param chunkSize
     * @return
     */
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File file = new File(chunkFileFolderPath + chunk);
        if (file.exists()) {
            //分块文件存在
            return new CheckChunkResult(CommonCode.SUCCESS, true);
        } else {
            //分块文件不存在
            return new CheckChunkResult(CommonCode.SUCCESS, false);
        }

    }

    /**
     * 上传分块
     *
     * @param file
     * @param chunk
     * @param fileMd5
     * @return
     */
    public ResponseResult uploadchunk(MultipartFile file, Integer chunk, String fileMd5) {

        if (file == null) {
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_ISNULL);
        }

        //创建块文件目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        if (!chunkFileFolder.exists()) {
            chunkFileFolder.mkdirs();
        }
        //上传快
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {

            inputStream = file.getInputStream();
            //块文件
            outputStream = new FileOutputStream(chunkFileFolderPath + chunk);
            //复制io流
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 合并分块文件
     *
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {

        //合并分块文件

        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);//块文件的文件夹
        if (!chunkFileFolder.exists()) {
//            chunkFileFolder.mkdirs();
            ExceptionCast.cast(MediaCode.MERGE_FILEFOLDER_ISNULL);
        }

        File mergeFile = new File(getFilePath(fileMd5, fileExt));
        //先删除合并文件再创建
        if (mergeFile.exists()) {
            mergeFile.delete();
        }

        //创建合并文件
        try {
            mergeFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //获取块文件的列表集合
        List<File> fileList = sortFileList(chunkFileFolder);

        //合并文件
        mergeFile = mergeFile(fileList, mergeFile);
        if (mergeFile == null) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }


        //校验合并文件的md5和原来的md5是否一致
        boolean b = checkFileMd5(mergeFile, fileMd5);
        if (!b) {//如果不一致
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }

        //将信息写入mongodb
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileName(fileMd5 + "." + fileExt);
        mediaFile.setFileOriginalName(fileName);
        //文件保存的相对路径
        mediaFile.setFilePath(getFileFolderRelativePath(fileMd5, fileExt));
        mediaFile.setFileSize(fileSize);
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        MediaFile save = mediaFileRepository.save(mediaFile);

        //像rabbitmq发送文件上传成功的消息
        sendProcessVideoMsg(mediaFile.getFileId());

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 向rabbitmq发送消息
     * @param mediaId
     * @return
     */
    public ResponseResult sendProcessVideoMsg(String mediaId){
        //检查mediaId在数据库中是否存在
        boolean exists = mediaFileRepository.existsById(mediaId);
        if(!exists){
            ExceptionCast.cast(CommonCode.FAIL);
        }

        Map<String ,String> map = new HashMap<>();
        map.put("mediaId", mediaId);

        String jsonString = JSON.toJSONString(map);
        //发送
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,routingkey_media_video , jsonString);
        } catch (AmqpException e) {
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }


    private List<File> sortFileList(File chunkFileFolder) {
        File[] files = chunkFileFolder.listFiles();
        //对块文件排序 从小到大排序
        List<File> fileList = new ArrayList<File>(Arrays.asList(files));
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName())) {
                    return -1;
                }
                return 1;
            }
        });
        return fileList;
    }


    /**
     * 合并文件
     * 将块文件合并
     *
     * @param fileList
     * @param mergeFile
     * @return
     */
    private File mergeFile(List<File> fileList, File mergeFile) {
        try {
            //创建写文件对象
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
            //遍历分块文件开始合并
            byte[] b = new byte[1024];
            for (File file : fileList) {
                RandomAccessFile raf_read = new RandomAccessFile(file, "r");
                int len = -1;
                while ((len = raf_read.read(b)) != -1) {
                    raf_write.write(b, 0, len);
                }
                raf_read.close();
            }
            raf_write.close();
            return mergeFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 文件校验md5
     * 合并后的文件和md5校验
     *
     * @param mergeFile
     * @param md5
     * @return
     */
    private boolean checkFileMd5(File mergeFile, String md5) {
        if (mergeFile == null || StringUtils.isEmpty(md5)) {
            return false;
        }
        //进行md5校验
        FileInputStream mergeFileInputstream = null;
        try {
            mergeFileInputstream = new FileInputStream(mergeFile);
            //得到文件的md5
            String mergeFileMd5 = DigestUtils.md5Hex(mergeFileInputstream);
            //比较md5
            if (md5.equalsIgnoreCase(mergeFileMd5)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                mergeFileInputstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 返回文件目录
     *
     * @return
     */
    private String getFileFolderPath(String fileMd5) {
        return UPLOAD_LOCATION + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5;
    }

    /**
     * 返回文件分块目录
     *
     * @return
     */
    private String getChunkFileFolderPath(String fileMd5) {
        return UPLOAD_LOCATION + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk/";
    }

    /**
     * 返回文件绝对路径
     *
     * @param fileMd5
     * @param fileExt
     * @return
     */
    private String getFilePath(String fileMd5, String fileExt) {
        return UPLOAD_LOCATION + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + "." + fileExt;
    }

    /**
     * 返回文件相对路径
     * @param fileMd5
     * @param fileExt
     * @return
     */
    private String getFileFolderRelativePath(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/";
    }


}
