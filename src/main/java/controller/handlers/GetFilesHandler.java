package controller.handlers;

import controller.handlers.abstracts.FileRequestHandler;
import controller.util.abstracts.ResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.dto.FileResponse;
import service.FileStorageService;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class GetFilesHandler implements FileRequestHandler {
    private final FileStorageService fileStorageService;
    private final ResponseWriter responseWriter;

    @Override
    public boolean canHandle(HttpServletRequest req) {
        return "GET".equalsIgnoreCase(req.getMethod()) &&
                req.getPathInfo() == null;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.debug("Processing get all files request");
        List<FileResponse> files = fileStorageService.getFiles();
        responseWriter.writeJsonResponse(resp, files);
    }
}