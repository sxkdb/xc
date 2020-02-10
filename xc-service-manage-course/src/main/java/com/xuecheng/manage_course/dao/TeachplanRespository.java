package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Teachplan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeachplanRespository extends JpaRepository<Teachplan, String> {

    List<Teachplan> getTeachplansByCourseidAndParentid(String courseId,String parentId);

}
