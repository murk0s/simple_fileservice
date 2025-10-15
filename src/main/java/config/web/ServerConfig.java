package config.web;

import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Builder
public class ServerConfig {
    private final int port;
    private final Path uploadPath;
    private final String downloadBaseUrl;
    private final String tempDir;
    private final String contextPath;

    public static ServerConfig createDefault() {
        return ServerConfig.builder()
                .port(8080)
                .uploadPath(Paths.get("uploads"))
                .downloadBaseUrl("http://localhost:8080/api/v1/files/download")
                .tempDir(System.getProperty("java.io.tmpdir"))
                .contextPath("/")
                .build();
    }
}
