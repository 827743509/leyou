package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.domain.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class UploadServiceImpl implements  UploadService {
    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private ThumbImageConfig thumbImageConfig;
    public String uploadImage(MultipartFile file) {
        try {
            //校验文件类型
            String contentType = file.getContentType();
            if(!contentType.startsWith("image")){return  null;}
            //校验文件内容
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if(bufferedImage==null){return  null;}

            String originalFilename = file.getOriginalFilename();
              /*//生成图片路径并保存本地
            String suffix= "."+StringUtils.substringAfterLast(originalFilename,".");
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("/yyyy/MM/dd/");
            String format = simpleDateFormat.format(new Date());
            String dirPath="E:/leyouimage"+format;
            File dirFile=new File(dirPath);
            if(!dirFile.exists()){
                dirFile.mkdirs();
            }
            String  filePath=format+ UUID.randomUUID().toString()+suffix;
            file.transferTo(new File("E:/leyouimage"+filePath));
            return "http://image.leyou.com"+filePath;*/
            //生成图片并保存fastDFS
            StorePath storePath = this.storageClient.uploadFile(file.getInputStream(), file.getSize(), StringUtils.substringAfterLast(originalFilename, "."), null);
            return  "http://image.leyou.com/"+storePath.getFullPath();
        } catch (IOException e) {
            e.printStackTrace();
            return  null;
        }
        //返回url

    }
}
