package config.web;

import config.web.abstracts.FilterConfigurator;
import config.web.abstracts.ServerInitializer;
import config.web.abstracts.ServletConfigurator;
import org.quartz.SchedulerException;
import repository.FileMetadataRepository;
import scheduler.FileCleanupScheduler;
import service.FileCleanupService;
import service.FileStorageService;

import java.util.List;

public class ApplicationFactory {
    public static ServerConfig createServerConfig() {
        return ServerConfig.createDefault();
    }

    public static FileMetadataRepository createFileMetadataRepository() {
        return new FileMetadataRepository();
    }

    public static FileStorageService createFileStorageService(ServerConfig config) {
        return new FileStorageService(config.getUploadPath(), config.getDownloadBaseUrl(), createFileMetadataRepository());
    }

    public static FileCleanupService createFileCleanupService(ServerConfig config) {
        return new FileCleanupService(config.getUploadPath(), createFileMetadataRepository());
    }

    public static FileCleanupScheduler createFileCleanupScheduler(ServerConfig config) {
        try {
            FileCleanupService cleanupService = createFileCleanupService(config);
            return new FileCleanupScheduler(cleanupService);
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to create file cleanup scheduler", e);
        }
    }

    public static CorsFilterConfigurator createCorsFilterConfigurator() {
        return new CorsFilterConfigurator();
    }

    public static FileServletConfigurator createFileServletConfigurator(FileStorageService fileStorageService,
                                                                    ServerConfig config) {
        return new FileServletConfigurator(fileStorageService, config.getTempDir());
    }

    public static ServerInitializer createServerInitializer(ServerConfig config) {
        List<FilterConfigurator> filterConfigurators = List.of(createCorsFilterConfigurator());

        FileStorageService fileStorageService = createFileStorageService(config);
        List<ServletConfigurator> servletConfigurators = List.of(
                createFileServletConfigurator(fileStorageService, config)
        );

        return new JettyServerInitializer(config, filterConfigurators, servletConfigurators);
    }
}
