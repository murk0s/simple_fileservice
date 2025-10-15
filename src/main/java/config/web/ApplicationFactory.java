package config.web;

import config.web.abstracts.FilterConfigurator;
import config.web.abstracts.ServerInitializer;
import config.web.abstracts.ServletConfigurator;
import service.FileStorageService;

import java.util.List;

public class ApplicationFactory {
    public static ServerConfig createServerConfig() {
        return ServerConfig.createDefault();
    }

    public static FileStorageService createFileStorageService(ServerConfig config) {
        return new FileStorageService(config.getUploadPath(), config.getDownloadBaseUrl());
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
