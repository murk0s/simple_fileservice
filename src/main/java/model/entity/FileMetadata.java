package model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "file_metadata")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "original_file_name", nullable = false)
    String originalFileName;

    @Column(name = "stored_file_name", nullable = false, unique = true)
    String storedFileName;

    @Column(name = "file_size", nullable = false)
    Long fileSize;

    @Column(name = "content_type",  nullable = false)
    String contentType;

    @Column(name = "upload_date", nullable = false)
    LocalDateTime uploadDate;

    @Column(name = "last_download_date")
    LocalDateTime lastDownloadDate;

    @Column(name = "download_count", nullable = false)
    Integer downloadCount;

    @PrePersist
    public void prePersist() {
        uploadDate = uploadDate == null ? LocalDateTime.now() : uploadDate;
        lastDownloadDate = lastDownloadDate == null ? LocalDateTime.now() : lastDownloadDate;
        downloadCount = downloadCount == null ? 0 : downloadCount+1;
    }
}
