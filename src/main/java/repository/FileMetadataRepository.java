package repository;

import config.HibernateUtil;
import lombok.extern.slf4j.Slf4j;
import model.entity.FileMetadata;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class FileMetadataRepository {

    private final SessionFactory sessionFactory;

    public FileMetadataRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public List<FileMetadata> findAll() {
        log.debug("Finding all file metadata");

        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();

            List<FileMetadata> files = session.createQuery(
                            "FROM FileMetadata ORDER BY uploadDate DESC", FileMetadata.class)
                    .getResultList();

            session.getTransaction().commit();
            log.debug("Found {} files", files.size());

            return files;
        } catch (Exception e) {
            session.getTransaction().rollback();
            log.error("Error finding all file metadata", e);
            throw new RuntimeException("Database error", e);
        }
    }

    public FileMetadata save(FileMetadata fileMetadata) {
        log.debug("Saving file metadata: {}", fileMetadata.getOriginalFileName());

        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();

            if (fileMetadata.getId() == null) {
                session.persist(fileMetadata);
            } else {
                fileMetadata = session.merge(fileMetadata);
            }

            session.getTransaction().commit();
            log.debug("File metadata saved with ID: {}", fileMetadata.getId());

            return fileMetadata;
        } catch (Exception e) {
            session.getTransaction().rollback();
            log.error("Error saving file metadata", e);
            throw new RuntimeException("Database error", e);
        }
    }

    public Optional<FileMetadata> findById(UUID id) {
        log.debug("Finding file metadata by ID: {}", id);

        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();

            FileMetadata fileMetadata = session.get(FileMetadata.class, id);

            session.getTransaction().commit();

            if (fileMetadata != null) {
                log.debug("Found file: {}", fileMetadata.getOriginalFileName());
            } else {
                log.debug("File not found with ID: {}", id);
            }

            return Optional.ofNullable(fileMetadata);
        } catch (Exception e) {
            session.getTransaction().rollback();
            log.error("Error finding file metadata by ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }


    public void incrementDownloadCount(UUID id) {
        log.debug("Incrementing download count for file ID: {}", id);

        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();

            FileMetadata file = session.get(FileMetadata.class, id);
            if (file == null) {
                session.getTransaction().rollback();
                log.warn("File not found with ID: {}, cannot increment download count", id);
                return ;
            }

            session.createMutationQuery(
                            "UPDATE FileMetadata fm SET fm.downloadCount = fm.downloadCount + 1, " +
                                    "fm.lastDownloadDate = CURRENT_TIMESTAMP WHERE fm.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            session.getTransaction().commit();

            log.debug("Successfully incremented download count for file: {} (new count: {})",
                    file.getOriginalFileName(), file.getDownloadCount() + 1);

        } catch (Exception e) {
            session.getTransaction().rollback();
            log.error("Error incrementing download count for file ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<FileMetadata> findFilesNotDownloadedForDays(int days) {
        log.debug("Finding files not downloaded for {} days", days);

        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();

            List<FileMetadata> files = session.createQuery(
                            "FROM FileMetadata fm WHERE " +
                                    "(fm.lastDownloadDate IS NULL AND fm.uploadDate < :dateThreshold) OR " +
                                    "(fm.lastDownloadDate IS NOT NULL AND fm.lastDownloadDate < :dateThreshold) " +
                                    "ORDER BY fm.uploadDate", FileMetadata.class)
                    .setParameter("dateThreshold",
                            LocalDateTime.now().minusDays(days))
                    .getResultList();

            session.getTransaction().commit();
            log.debug("Found {} old files", files.size());

            return files;
        } catch (Exception e) {
            session.getTransaction().rollback();
            log.error("Error finding old files", e);
            throw new RuntimeException("Database error", e);
        }
    }

    public void delete(UUID id) {
        log.debug("Deleting file metadata with ID: {}", id);

        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();

            FileMetadata fileMetadata = session.get(FileMetadata.class, id);
            if (fileMetadata != null) {
                session.remove(fileMetadata);
                log.debug("File metadata deleted: {}", id);
            } else {
                log.warn("File metadata not found for deletion: {}", id);
            }

            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            log.error("Error deleting file metadata with ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }
}
