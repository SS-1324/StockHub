package com.kh.demo.community.service;

import com.kh.demo.common.util.FileUploadUtil;
import com.kh.demo.common.util.SavedFile;
import com.kh.demo.community.dto.BoardImageDto;
import com.kh.demo.community.mapper.BoardImageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class BoardImageServiceImpl implements BoardImageService {

    // 게시글당 첨부 가능한 최대 이미지 수 - JSP/JS는 이 값을 하드코딩하지 않고 컨트롤러가 모델로 내려주는 값을 씀
    private static final int MAX_IMAGE_COUNT = 8;

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
    public int getMaxImageCount() {
        return MAX_IMAGE_COUNT;
    }

    @Override
    public Map<Long, List<BoardImageDto>> getByBoardIds(List<Long> boardIds) {
        if (boardIds == null || boardIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<BoardImageDto>> imagesByBoardId = new LinkedHashMap<>();
        for (BoardImageDto image : boardImageMapper.selectByBoardIds(boardIds)) {
            imagesByBoardId.computeIfAbsent(image.getBoardId(), key -> new ArrayList<>()).add(image);
        }
        return imagesByBoardId;
    }

    @Override
    public List<BoardImageDto> uploadImages(Long boardId, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        if (images.size() > MAX_IMAGE_COUNT) {
            throw new IllegalArgumentException("이미지는 최대 " + MAX_IMAGE_COUNT + "장까지 첨부할 수 있습니다.");
        }
        return saveImages(boardId, images, 0);
    }

    @Override
    @Transactional
    public List<BoardImageDto> updateImages(Long boardId, List<Long> deleteImageIds, List<MultipartFile> newImages) {
        List<BoardImageDto> current = boardImageMapper.selectByBoardId(boardId);

        int deletedCount = 0;
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            // board_id로 다시 좁혀서 지우기 때문에, 다른 게시글의 이미지 id를 섞어 보내도 지워지지 않는다
            List<BoardImageDto> toDelete = new ArrayList<>();
            for (BoardImageDto image : current) {
                if (deleteImageIds.contains(image.getImgId())) {
                    toDelete.add(image);
                }
            }
            if (!toDelete.isEmpty()) {
                String boardUploadDir = uploadDir + "/" + BOARD_UPLOAD_SUB_DIR;
                for (BoardImageDto image : toDelete) {
                    fileUploadUtil.delete(image.getImgPath(), boardUploadDir);
                }
                boardImageMapper.deleteByIds(boardId, deleteImageIds);
                deletedCount = toDelete.size();
            }
        }

        int remainingCount = current.size() - deletedCount;
        int incomingCount = countNonEmpty(newImages);
        if (remainingCount + incomingCount > MAX_IMAGE_COUNT) {
            throw new IllegalArgumentException("이미지는 최대 " + MAX_IMAGE_COUNT + "장까지 첨부할 수 있습니다.");
        }

        return saveImages(boardId, newImages, remainingCount);
    }

    private int countNonEmpty(List<MultipartFile> images) {
        if (images == null) {
            return 0;
        }
        int count = 0;
        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    // startOrder: 새 이미지들의 img_order 시작값 - 새 글쓰기는 0부터, 기존 이미지가 남아있는 수정에서는 그 뒤부터 이어붙인다
    private List<BoardImageDto> saveImages(Long boardId, List<MultipartFile> images, int startOrder) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        String boardUploadDir = uploadDir + "/" + BOARD_UPLOAD_SUB_DIR;
        List<SavedFile> savedFiles = new ArrayList<>();
        List<BoardImageDto> insertedImages = new ArrayList<>();
        try {
            int order = startOrder;
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
            cleanupSavedFiles(savedFiles, boardUploadDir);
            throw new IllegalStateException("이미지 업로드에 실패했습니다: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            // 여러 장 중 중간 파일이 형식 검증에 걸려도, 그 앞에서 이미 디스크에 저장된 파일은 남으면 안 된다
            cleanupSavedFiles(savedFiles, boardUploadDir);
            throw e;
        }

        return insertedImages;
    }

    private void cleanupSavedFiles(List<SavedFile> savedFiles, String boardUploadDir) {
        for (SavedFile savedFile : savedFiles) {
            fileUploadUtil.delete(savedFile.getPath(), boardUploadDir);
        }
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

    // 확장자, Content-Type, 실제 파일 시그니처(매직바이트)까지 세 가지를 확인하는 이유: 확장자/Content-Type은
    // 파일명이나 요청 헤더만 조작하면 둘 다 속일 수 있는 값이라, 파일 내용 자체를 열어봐야만 진짜 이미지인지 알 수 있음
    // (예: 실행 파일의 확장자만 .jpg로 바꾸고 Content-Type도 image/jpeg로 조작해서 올리는 우회를 막는다).
    private void validateImageFile(MultipartFile image) throws IOException {
        String originalName = image.getOriginalFilename();
        String extension = extractExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다: " + originalName);
        }

        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다: " + originalName);
        }

        if (!hasKnownImageSignature(image)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다: " + originalName);
        }
    }

    // 실제 파일 앞부분 바이트(매직바이트)를 읽어 JPG/PNG/GIF/WEBP 중 하나가 맞는지 확인.
    // 게시판은 짤방 공유가 잦아 GIF/PNG를 계속 허용해야 해서, board 전용으로 여기 안에서만 검사한다.
    private boolean hasKnownImageSignature(MultipartFile image) throws IOException {
        try (InputStream in = image.getInputStream()) {
            byte[] header = in.readNBytes(12); // WEBP 판별에 12바이트까지 필요

            if (header.length >= 3
                    && (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
                return true; // JPEG
            }
            if (header.length >= 8
                    && (header[0] & 0xFF) == 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G') {
                return true; // PNG
            }
            if (header.length >= 6) {
                String ascii6 = new String(header, 0, 6, StandardCharsets.US_ASCII);
                if ("GIF87a".equals(ascii6) || "GIF89a".equals(ascii6)) {
                    return true; // GIF
                }
            }
            return header.length >= 12
                    && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                    && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P'; // WEBP
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
