package com.qingcheng.controller.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private HttpServletRequest request;

    @PostMapping("/native")
    public String nativeUpload(@RequestParam("file") MultipartFile file){

            String path = request.getSession().getServletContext().getRealPath("img");
            String filePath = path+"/"+file.getOriginalFilename();
            File desFile = new File(filePath);
            if (!desFile.getParentFile().exists()) {
                desFile.mkdirs();
            }
            try{
                file.transferTo(desFile);
            }catch (IOException e){
                e.printStackTrace();
            }
            return "http://localhost:9101/img/"+file.getOriginalFilename();
    }
}
