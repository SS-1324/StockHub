package com.kh.demo.community.service;

import com.kh.demo.common.util.FileUploadUtil;
import com.kh.demo.common.util.SavedFile;
import com.kh.demo.community.dto.BoardImageDto;
import com.kh.demo.community.mapper.BoardImageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class BoardImageServiceImpl implements BoardImageService {

    // 게시글당 첨부 가능한 최대 이미지 수
    private static final int MAX_IMAGE_COUNT = 5;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    // 실제 이미지 파일이 저장될 디스크 폴더(게시판 전용 하위 폴더), 웹에서 접근하는 경로 접두어
    private static final String BOARD_UPLOAD_SUB_DIR = "board";
    private static final String BOARD_WEB_PREFIX = "/uploads/board";

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private BoardImageMapper boardImageMapper;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Override
    public List<BoardImageDto> uploadImages(Long boardId, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        if (images.size() > MAX_IMAGE_COUNT) {
            throw new IllegalArgumentException("이미지는 최대 " + MAX_IMAGE_COUNT + "장까지 첨부할 수 있습니다.");
        }

        String boardUploadDir = uploadDir + "/" + BOARD_UPLOAD_SUB_DIR;
        List<SavedFile> savedFiles = new ArrayList<>();
        List<BoardImageDto> insertedImages = new ArrayList<>();
        try {
            int order = 0;
            for (MultipartFile image : images) {
                if (image == null || image.isEmpty()) {
                    continue;
                }
                validateImageFile(image);

                SavedFile savedFile = fileUploadUtil.save(image, boardUploadDir, BOARD_WEB_PREFIX);
                savedFiles.add(savedFile);

                BoardImageDto boardImageDto = new BoardImageDto();
                boardImageDto.setBoardId(boardId);
                boardImageDto.setOriginalName(savedFile.getOriginalName());
                boardImageDto.setSaveName(savedFile.getSaveName());
                boardImageDto.setImgPath(savedFile.getPath());
                boardImageDto.setImgOrder(order++);

                boardImageMapper.insertBoardImage(boardImageDto);
                insertedImages.add(boardImageDto);
            }
        } catch (IOException e) {
            // 업로드 도중 실패하면 이번 요청에서 이미 저장한 파일들을 정리하고 실패 처리(재시도는 사용자가 폼 재제출)
            for (SavedFile savedFile : savedFiles) {
                fileUploadUtil.delete(savedFile.getPath(), boardUploadDir);
            }
            throw new IllegalStateException("이미지 업로드에 실패했습니다: " + e.getMessage(), e);
        }

        return insertedImages;
    }

    @Override
    public List<BoardImageDto> getByBoardId(Long boardId) {
        return boardImageMapper.selectByBoardId(boardId);
    }

    @Override
    public void deleteByBoardId(Long boardId) {
        String boardUploadDir = uploadDir + "/" + BOARD_UPLOAD_SUB_DIR;
        List<BoardImageDto> images = boardImageMapper.selectByBoardId(boardId);
        for (BoardImageDto image : images) {
            fileUploadUtil.delete(image.getImgPath(), boardUploadDir);
        }
        boardImageMapper.deleteByBoardId(boardId);
    }

    private void validateImageFile(MultipartFile image) {
        String originalName = image.getOriginalFilename();
        String extension = extractExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다: " + originalName);
        }

        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다: " + originalName);
        }
    }

    private String extractExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return fileName.substring(dotIndex).toLowerCase(Locale.ROOT);
    }
}
