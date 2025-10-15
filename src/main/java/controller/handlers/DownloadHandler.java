package controller.handlers;

import controller.handlers.abstracts.FileRequestHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.dto.FileDownloadResponse;
import service.FileStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class DownloadHandler implements FileRequestHandler {
    private final FileStorageService fileStorageService;

    @Override
    public boolean canHandle(HttpServletRequest req) {
        return "GET".equalsIgnoreCase(req.getMethod()) &&
                req.getPathInfo() != null &&
                req.getPathInfo().startsWith("/download");
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uuidStr = extractUuidFromPath(req.getPathInfo());

        try {
            UUID temporaryLinkId = UUID.fromString(uuidStr);
            log.debug("Processing download request for temporary link: {}", temporaryLinkId);

            FileDownloadResponse downloadResponse = fileStorageService.downloadFile(temporaryLinkId);
            setupDownloadResponse(resp, downloadResponse);

            Files.copy(downloadResponse.getFilePath(), resp.getOutputStream());
            resp.getOutputStream().flush();

            log.info("File downloaded successfully: {}", downloadResponse.getOriginalFileName());

        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID in download URL: {}", uuidStr);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid download URL format");
        } catch (Exception e) {
            log.error("Error during file download", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "File download failed");
        }
    }

    private String extractUuidFromPath(String pathInfo) {
        String[] pathParts = pathInfo.split("/");
        return pathParts.length > 1 ? pathParts[pathParts.length - 1] : "";
    }

    private void setupDownloadResponse(HttpServletResponse resp, FileDownloadResponse downloadResponse) {
        resp.setContentType(downloadResponse.getContentType());
        resp.setHeader("Content-Disposition",
                "attachment; filename=\"" + downloadResponse.getOriginalFileName() + "\"");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        resp.setContentLengthLong(downloadResponse.getFileSize());
    }
}
