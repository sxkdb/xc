package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class MediaFileService {

    @Autowired
    private MediaFileRepository mediaFileRepository;


    public QueryResponseResult findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {

        MediaFile mediaFile = new MediaFile();

        if (queryMediaFileRequest == null) {
            queryMediaFileRequest  = new QueryMediaFileRequest();
        }

        if (StringUtils.isNotBlank(queryMediaFileRequest.getFileOriginalName())) {
            mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }

        if (StringUtils.isNotBlank(queryMediaFileRequest.getProcessStatus())) {
            mediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
        }

        if (StringUtils.isNotBlank(queryMediaFileRequest.getTag())) {
            mediaFile.setTag(queryMediaFileRequest.getTag());
        }


        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("tag", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("fileOriginalName", ExampleMatcher.GenericPropertyMatchers.contains());


        Example<MediaFile> example = Example.of(mediaFile, exampleMatcher);

        if (page <= 0) {
            page = 1;
        }

        page --;

        if (size <= 0) {
            size = 10;
        }

        //分页
        Pageable pageable = new PageRequest(page, size);

        Page<MediaFile> mediaFilePage = mediaFileRepository.findAll(example, pageable);

        QueryResult queryResult = new QueryResult();

        queryResult.setList(mediaFilePage.getContent());
        queryResult.setTotal(mediaFilePage.getTotalElements());
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);

        return queryResponseResult;
    }

}
