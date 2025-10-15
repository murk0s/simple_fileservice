package config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.entity.FileMetadata;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


@Slf4j
public class HibernateUtil {
    @Getter
    private static final SessionFactory sessionFactory;

    static {
        try {
            sessionFactory = new Configuration()
                    .addAnnotatedClass(FileMetadata.class)
                    .buildSessionFactory();
            log.info("Hibernate SessionFactory created successfully");
        } catch (Throwable ex) {
            log.error("Initial SessionFactory creation failed", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static void shutdown() {
        log.info("Closing Hibernate SessionFactory");
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
