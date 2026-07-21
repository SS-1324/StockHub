package com.kh.demo.common.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/*
* MultipartFile을 서버 디스크에 실제로 저장해주는 유틸
*
* 이미지업로드 :
* 1. 브라우저에서 form enctype="multipart/form-data"로 파일을 전송
* 2. SrpingMVC가 객체로 받아서 Controller전달
* 3. FileUploadUtil 클래스가 실제 바이트를 uploads/ 폴더 아래에 파일로 저장
* 4. DB에는 파일 자체가 아니라 저장된 경로만 저장.
*
* 파일을명을 그대로 저장하지 않음.
* - 각각 다른회원이 동일한 이름의 파일을 저장하면 기존파일을 덮어써버리는 문제 발생할 수 있음.
* */

@Component
public class FileUploadUtil {
    public SavedFile save(MultipartFile file, String uploadDir, String webPrefix) throws IOException {
        if(file == null || file.isEmpty()){
            return null;
        }

        //원본파일명 : 파일명 + 확장자명
        String originalName = file.getOriginalFilename();

        //확장자만 따로 뽑아서 새로운 파일명 + 확장자
        String ext = "";
        int dotIndex = originalName.lastIndexOf('.');
        if(dotIndex > -1) {
            ext = originalName.substring(dotIndex); // .부터 전부다 추출
        }

        //UUID라고 하면 겹치지않게 만든 고유한 값
        String saveName = UUID.randomUUID() + ext;

        //저장경로가 없다면 생성
        File dir = new File(uploadDir).getAbsoluteFile();
        System.out.println(dir);
        if(dir.exists()){
            dir.mkdirs();
        }

        File target = new File(dir, saveName);
        file.transferTo(target); // MultipartFile형태로 전달파일 파일을 실제 target정보로 저장

        String path = webPrefix + "/" + saveName;
        return new SavedFile(originalName, saveName, path);
    }

    /*
        webPath : 저장 당시 만들었던 웹의 경로 (/uploads/profile/xxx.png)
        uploadDir : 경로에 실제 디스크 폴더 (uploads/profle)
     */
    public boolean delete(String webPath, String uploadDir){
        if(webPath == null || webPath.isBlank()){
            return false;
        }
        String fileName = webPath.substring(webPath.lastIndexOf("/") + 1);
        File target = new File(new File(uploadDir).getAbsoluteFile(), fileName);
        if(target.exists()){
            target.delete();
        }

        return true;
    }
}
