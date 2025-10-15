package service;

import lombok.extern.slf4j.Slf4j;
import model.dto.FileDownloadResponse;
import model.dto.FileResponse;
import model.entity.FileMetadata;
import repository.FileMetadataRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class FileStorageService {
    private final FileMetadataRepository fileMetadataRepository;
    private final Path uploadPath;
    private final String downloadBaseUrl;
    private final Map<String, UUID> temporaryLinks = new ConcurrentHashMap<>();

    public FileStorageService(Path uploadPath, String downloadBaseUrl) {
        this.fileMetadataRepository = new FileMetadataRepository();
        this.uploadPath = uploadPath;
        this.downloadBaseUrl = downloadBaseUrl;

        try {
            Files.createDirectories(uploadPath);
            log.info("Upload directory created: {}", uploadPath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Could not create upload directory: {}", uploadPath, e);
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public FileResponse uploadFile(InputStream fileStream, String fileName,
                                   String contentType, long fileSize) {
        log.debug("Starting file upload: {} ({} bytes, type: {})",
                fileName, fileSize, contentType);

        if (fileSize == 0) {
            log.warn("Attempt to upload empty file: {}", fileName);
            throw new IllegalArgumentException("File is empty");
        }

        try {
            String storedFileName = UUID.randomUUID().toString();
            Path targetLocation = uploadPath.resolve(storedFileName);
            Files.copy(fileStream, targetLocation);

            log.debug("File saved to: {}", targetLocation);

            FileMetadata fileMetadata = FileMetadata.builder()
                    .originalFileName(fileName)
                    .storedFileName(storedFileName)
                    .fileSize(fileSize)
                    .contentType(contentType)
                    .build();

            FileMetadata savedFile = fileMetadataRepository.save(fileMetadata);
            log.info("File uploaded successfully: {} -> {}", fileName, savedFile.getId());

            return toFileResponse(savedFile);

        } catch (IOException ex) {
            log.error("Failed to store file: {}", fileName, ex);
            throw new RuntimeException("Could not store file", ex);
        }
    }

    public List<FileResponse> getFiles() {
        log.debug("Retrieving all files metadata");
        List<FileResponse> files = fileMetadataRepository.findAll().stream()
                .map(this::toFileResponse)
                .collect(Collectors.toList());
        log.debug("Retrieved {} files", files.size());
        return files;
    }

    public String generateTemporaryDownloadUrl(UUID fileId) {
        log.debug("Generating temporary download URL for file: {}", fileId);

        fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> {
                    log.error("File not found for temporary link generation: {}", fileId);
                    return new RuntimeException("Could not find file id: " + fileId);
                });

        UUID token = UUID.randomUUID();
        temporaryLinks.put(token.toString(), fileId);

        String url = downloadBaseUrl + "/" + token;

        log.info("Temporary download URL generated: {} for file: {}", url, fileId);
        return url;
    }

    public FileDownloadResponse downloadFile(UUID temporaryLinkId) {
        log.debug("Processing file download with temporary link: {}", temporaryLinkId);

        UUID fileId = Optional.ofNullable(temporaryLinks.get(temporaryLinkId.toString()))
                .orElseThrow(() -> new IllegalArgumentException("Bad temporary link id: " + temporaryLinkId));

        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> {
                    log.error("File not found for temporary link: {}", temporaryLinkId);
                    return new RuntimeException("Could not find file");
                });

        Path filePath = getFilePath(fileMetadata);

        if (!Files.exists(filePath)) {
            log.error("File not found on disk: {}", filePath);
            throw new RuntimeException("Could not find file " + filePath);
        }

        log.info("File download prepared: {} via link: {}",
                fileMetadata.getOriginalFileName(), temporaryLinkId);

        return toFileDownloadResponse(fileMetadata, filePath);
    }

    public void incrementDownloadCount(UUID fileId) {
        log.debug("Incrementing download count for file: {}", fileId);
        fileMetadataRepository.incrementDownloadCount(fileId);
        log.debug("Download count incremented for file: {}", fileId);
    }

    private Path getFilePath(FileMetadata fileMetadata) {
        return uploadPath.resolve(fileMetadata.getStoredFileName()).normalize();
    }

    private FileResponse toFileResponse(FileMetadata metadata) {
        return FileResponse.builder()
                .id(metadata.getId())
                .originalFileName(metadata.getOriginalFileName())
                .fileSize(metadata.getFileSize())
                .contentType(metadata.getContentType())
                .uploadDate(metadata.getUploadDate())
                .build();
    }

    private FileDownloadResponse toFileDownloadResponse(FileMetadata metadata, Path filePath) {
        return FileDownloadResponse.builder()
                .filePath(filePath)
                .originalFileName(metadata.getOriginalFileName())
                .contentType(metadata.getContentType())
                .fileSize(metadata.getFileSize())
                .build();
    }
}