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

    // 확장자와 Content-Type을 둘 다 확인하는 이유: 둘 중 하나만 검사하면 파일명만 바꾸거나
    // Content-Type 헤더만 조작해서 우회할 수 있음(예: 동영상 파일의 확장자만 .jpg로 변경).
    // 다만 이것도 완벽하진 않음 - 실제 파일 시그니처(매직바이트) 검증은 아님(TODO, #7 후속).
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
