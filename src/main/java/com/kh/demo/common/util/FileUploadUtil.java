package com.kh.demo.common.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

// 업로드 파일의 저장과 삭제를 처리
@Component
public class FileUploadUtil {

    // 파일을 고유한 이름으로 서버에 저장
    public SavedFile save(MultipartFile file, String uploadDir, String webPrefix) throws IOException {
        // 선택한 파일이 없으면 저장하지 않음
        if(file == null || file.isEmpty()){
            return null;
        }

        // 사용자가 올린 원본 파일명
        String originalName = file.getOriginalFilename();

        // 원본 파일명에서 확장자를 추출
        String ext = "";
        int dotIndex = originalName.lastIndexOf('.');
        if(dotIndex > -1) {
            ext = originalName.substring(dotIndex);
        }

        // UUID로 중복되지 않는 파일명을 생성
        String saveName = UUID.randomUUID() + ext;

        // 업로드 폴더의 실제 경로를 생성
        File dir = new File(uploadDir).getAbsoluteFile();
        if(!dir.exists()){
            dir.mkdirs();
        }

        // 업로드 파일을 서버 폴더에 저장
        File target = new File(dir, saveName);
        file.transferTo(target);

        // 브라우저에서 사용할 이미지 주소를 생성
        String path = webPrefix + "/" + saveName;
        return new SavedFile(originalName, saveName, path);
    }

    // 저장된 파일을 서버 폴더에서 삭제
    public boolean delete(String webPath, String uploadDir){
        // 저장 경로가 없으면 삭제하지 않음
        if(webPath == null || webPath.isBlank()){
            return false;
        }

        // 웹 경로에서 실제 파일명만 추출
        String fileName = webPath.substring(webPath.lastIndexOf("/") + 1);
        File target = new File(new File(uploadDir).getAbsoluteFile(), fileName);

        // 파일이 존재하면 삭제
        if(target.exists()){
            target.delete();
        }

        return true;
    }
}
