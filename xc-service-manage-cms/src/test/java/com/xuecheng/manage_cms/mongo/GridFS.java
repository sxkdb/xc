package com.xuecheng.manage_cms.mongo;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GridFS {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    //从mongodb中读取文件
    @Test
    public void testRead() throws Exception {
        //查询文件
        GridFSFile fsFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is("5a795bbcdd573c04508f3a59")));
        //打开下载流对象
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(fsFile.getObjectId());
        //创建GridFsResource 用于获取流对象
        GridFsResource gridFsResource = new GridFsResource(fsFile,gridFSDownloadStream);
        String content = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
        System.out.println(content);

    }

    //删除文件

    //上传文件
    @Test
    public void test() throws Exception {
        String htmlContent="<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Title</title>\n" +
                "    <link rel=\"stylesheet\" href=\"http://www.xuecheng.com/plugins/normalize-css/normalize.css\"/>\n" +
                "    <link rel=\"stylesheet\" href=\"http://www.xuecheng.com/plugins/bootstrap/dist/css/bootstrap.css\"/>\n" +
                "    <link rel=\"stylesheet\" href=\"http://www.xuecheng.com/css/page-learing-index.css\"/>\n" +
                "    <link rel=\"stylesheet\" href=\"http://www.xuecheng.com/css/page-header.css\"/>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"banner-roll\">\n" +
                "\n" +
                "    <div class=\"banner-item\">\n" +
                "        <#list model as m>\n" +
                "            <div class=\"item\" style=\"background-image: url(${m.value});\"></div>\n" +
                "        </#list>\n" +
                "    </div>\n" +
                "\n" +
                "    <div class=\"indicators\"></div>\n" +
                "</div>\n" +
                "<script type=\"text/javascript\" src=\"http://www.xuecheng.com/plugins/jquery/dist/jquery.js\"></script>\n" +
                "<script type=\"text/javascript\" src=\"http://www.xuecheng.com/plugins/bootstrap/dist/js/bootstrap.js\"></script>\n" +
                "\n" +
                "<script type=\"text/javascript\">\n" +
                "    var tg = $('.banner-item .item');\n" +
                "    var num = 0;\n" +
                "    for (i = 0; i < tg.length; i++) {\n" +
                "        $('.indicators').append('<span></span>');\n" +
                "        $('.indicators').find('span').eq(num).addClass('active');\n" +
                "    }\n" +
                "\n" +
                "    function roll() {\n" +
                "        tg.eq(num).animate({\n" +
                "            'opacity': '1',\n" +
                "            'z-index': num\n" +
                "        }, 1000).siblings().animate({\n" +
                "            'opacity': '0',\n" +
                "            'z-index': 0\n" +
                "        }, 1000);\n" +
                "        $('.indicators').find('span').eq(num).addClass('active').siblings().removeClass('active');\n" +
                "        if (num >= tg.length - 1) {\n" +
                "            num = 0;\n" +
                "        } else {\n" +
                "            num++;\n" +
                "        }\n" +
                "    }\n" +
                "    $('.indicators').find('span').click(function() {\n" +
                "        num = $(this).index();\n" +
                "        roll();\n" +
                "    });\n" +
                "    var timer = setInterval(roll, 3000);\n" +
                "    $('.banner-item').mouseover(function() {\n" +
                "        clearInterval(timer)\n" +
                "    });\n" +
                "    $('.banner-item').mouseout(function() {\n" +
                "        timer = setInterval(roll, 3000)\n" +
                "    });\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>";
        //保存html文件
        try {
            InputStream inputStream = IOUtils.toInputStream(htmlContent, "UTF-8");

            ObjectId id = gridFsTemplate.store(inputStream,"index_banner.html");
            //文件id
            String fileId = id.toString();
            System.out.println(fileId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //上传文件
    @Test
    public void testuploadFiles() throws Exception {

        FileInputStream fileInputStream = new FileInputStream(new File("C:\\Users\\ZSH\\Desktop\\course.ftl"));

        ObjectId objectId = gridFsTemplate.store(fileInputStream, "course.html");

        System.out.println(objectId);
    }

}
