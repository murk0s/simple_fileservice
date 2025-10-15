package controller.handlers;

import controller.handlers.abstracts.FileRequestHandler;
import controller.util.abstracts.ResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import service.FileStorageService;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class UpdateDownloadCountHandler implements FileRequestHandler {
    private final FileStorageService fileStorageService;
    private final ResponseWriter responseWriter;

    @Override
    public boolean canHandle(HttpServletRequest req) {
        return "POST".equalsIgnoreCase(req.getMethod()) &&
                "/update-download-count".equals(req.getPathInfo());
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uuid = req.getParameter("uuid");

        if (uuid == null || uuid.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseWriter.writeTextResponse(resp, "Missing or empty 'uuid' parameter");
            return;
        }

        try {
            fileStorageService.incrementDownloadCount(UUID.fromString(uuid));
            responseWriter.writeJsonResponse(resp, Map.of("status", "updated"));
            log.debug("Download count updated for UUID: {}", uuid);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format: {}", uuid);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseWriter.writeTextResponse(resp, "Invalid UUID format");
        } catch (Exception e) {
            log.error("Error updating download count for UUID: {}", uuid, e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseWriter.writeTextResponse(resp, "Error updating download count");
        }
    }
}
