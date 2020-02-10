package com.xuecheng.learning.service;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.learning.response.GetMediaResult;
import com.xuecheng.framework.domain.learning.response.LearningCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.learning.client.CourseSearchClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LearningService {

    @Autowired
    private CourseSearchClient courseSearchClient;

    public GetMediaResult getMedia(String courseId, String teachplanId) {

        if (StringUtils.isBlank(teachplanId)) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        TeachplanMediaPub teachplanMediaPub = courseSearchClient.getmedia(teachplanId);

        if (StringUtils.isBlank(teachplanMediaPub.getMediaUrl())) {
            ExceptionCast.cast(LearningCode.LEARNING_GETMEDIA_ERROR);
        }

        return new GetMediaResult(CommonCode.SUCCESS, teachplanMediaPub.getMediaUrl());

    }


}
