package config.web;

import config.web.abstracts.ServletConfigurator;
import controller.FileController;
import jakarta.servlet.MultipartConfigElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import service.FileStorageService;

@Slf4j
@RequiredArgsConstructor
public class FileServletConfigurator implements ServletConfigurator {
    private final FileStorageService fileStorageService;
    private final String tempDir;

    public void configure(ServletContextHandler context) {
        MultipartConfigElement multipartConfig = new MultipartConfigElement(tempDir);

        ServletHolder servletHolder = new ServletHolder(new FileController(fileStorageService));
        servletHolder.getRegistration().setMultipartConfig(multipartConfig);

        context.addServlet(servletHolder, "/api/v1/files/*");
        log.debug("File servlet configured");
    }
}