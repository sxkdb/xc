package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MediaProcessTask {

    @Value("${xc-service-manage-media.video-location}")
    private String video_location;

    @Value("${xc-service-manage-media.ffmpeg-path}")
    private String ffmpeg_path;

    @Autowired
    private MediaFileRepository mediaFileRepository;

    //视频处理
    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}",containerFactory = "customContainerFactory")
    public void receiveMediaProcessTask(String msg) {

        //解析消息 得到mediaId
        Map map = JSON.parseObject(msg, Map.class);
        String mediaId = (String) map.get("mediaId");
        if (mediaId == null) {
            return;
        }
        //根据mediaId去数据库查文件信息
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()) {
            return;
        }
        //得到文件信息
        MediaFile mediaFile = optional.get();

        //改变文件处理状态
        //判断文件是否为avi
        if (mediaFile.getFileType() == null || !mediaFile.getFileType().equals("avi")) {
            mediaFile.setProcessStatus("303004");//无需处理
            mediaFileRepository.save(mediaFile);
            return;
        }else {
            mediaFile.setProcessStatus("303001");//处理中
            mediaFileRepository.save(mediaFile);
        }

        String video_path = video_location + mediaFile.getFilePath() + mediaFile.getFileName();
        String mp4_name = mediaFile.getFileName() + ".mp4";
        String mp4folder_path = video_location + mediaFile.getFilePath();
        //将avi转mp4
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4folder_path);
        String result = mp4VideoUtil.generateMp4();
        if (result == null || !result.equals("success")) {
            //处理失败
            mediaFile.setProcessStatus("303003");
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            //保存错误信息到数据库
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }

        //将mp4转m3u8
        String m3u8_name = mediaFile.getFileName() + ".m3u8";
        String m3u8folder_path = video_location + mediaFile.getFilePath() + "hls/";
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path, video_path, m3u8_name, m3u8folder_path);
        String hlsResult = hlsVideoUtil.generateM3u8();
        if (hlsResult == null || !hlsResult.equals("success")) {
            //处理失败
            mediaFile.setProcessStatus("303003");
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            //保存错误信息到数据库
            mediaFileProcess_m3u8.setErrormsg(hlsResult);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }
        //转换成功
        //将数据记录到数据库
        mediaFile.setProcessStatus("303002");//处理状态为处理成功
        mediaFile.setFileUrl(mediaFile.getFilePath() + "hls/" + m3u8_name);//m3u8文件url

        //m3u8列表
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        MediaFileProcess_m3u8 process_m3u8 = new MediaFileProcess_m3u8();
        process_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(process_m3u8);
        mediaFileRepository.save(mediaFile);


    }


}
