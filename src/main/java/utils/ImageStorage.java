package utils;

import org.apache.commons.fileupload.FileItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 업로드된 이미지 파일을 로컬 디스크에 저장하고,
 * 외부에서 접근 가능한 URL 경로를 반환합니다.
 */
public class ImageStorage {
    // 실제 파일이 저장될 디렉터리
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads";

    /**
     * FileItem(업로드된 파일)을 저장하고, 접근 가능한 URL을 반환
     * @param fileItem 업로드된 파일
     * @return public URL path (e.g. "/uploads/uuid.png")
     */
    public static String save(FileItem fileItem) throws IOException {
        // 원본 확장자 추출
        String originalName = fileItem.getName();
        String ext = "";
        int idx = originalName.lastIndexOf('.');
        if (idx >= 0) {
            ext = originalName.substring(idx);
        }
        // 고유 파일명 생성
        String newName = UUID.randomUUID() + ext;

        // 디렉터리 생성
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 파일 쓰기
        Path filePath = uploadPath.resolve(newName);
        Files.write(filePath, fileItem.get());

        // 클라이언트에서 접근 가능한 URL 경로 반환
        return "/uploads/" + newName;
    }
}