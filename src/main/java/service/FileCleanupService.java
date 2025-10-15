package service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.entity.FileMetadata;
import repository.FileMetadataRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class FileCleanupService {

    private final Path uploadPath;
    private final FileMetadataRepository fileMetadataRepository;

    public void cleanupOldFiles() {
        log.info("Starting cleanup of old files...");

        try {
            List<FileMetadata> oldFiles = fileMetadataRepository.findFilesNotDownloadedForDays(30);

            log.info("Found {} files for cleanup", oldFiles.size());

            int deletedCount = 0;
            for (FileMetadata fileMetadata : oldFiles) {
                if (deleteFile(fileMetadata)) {
                    deletedCount++;
                }
            }

            log.info("Cleanup completed: {} files deleted", deletedCount);

        } catch (Exception e) {
            log.error("Error during file cleanup", e);
        }
    }

    private boolean deleteFile(FileMetadata fileMetadata) {
        try {
            Path filePath = uploadPath.resolve(fileMetadata.getStoredFileName());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.debug("Physical file deleted: {}", filePath);
            }

            fileMetadataRepository.delete(fileMetadata.getId());
            log.info("File metadata deleted from database: {} (ID: {})",
                    fileMetadata.getOriginalFileName(), fileMetadata.getId());

            return true;

        } catch (IOException e) {
            log.error("Failed to delete physical file: {}", fileMetadata.getStoredFileName(), e);
            return false;
        } catch (Exception e) {
            log.error("Failed to delete file metadata: {}", fileMetadata.getId(), e);
            return false;
        }
    }
}
