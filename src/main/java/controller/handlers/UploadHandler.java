package controller.handlers;

import controller.handlers.abstracts.FileRequestHandler;
import controller.util.abstracts.ResponseWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.dto.FileResponse;
import service.FileStorageService;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class UploadHandler implements FileRequestHandler {
    private final FileStorageService fileStorageService;
    private final ResponseWriter responseWriter;

    @Override
    public boolean canHandle(HttpServletRequest req) {
        return "POST".equalsIgnoreCase(req.getMethod()) &&
                "/upload".equals(req.getPathInfo());
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        validateMultipartRequest(req);

        Part filePart = req.getPart("file");
        validateFilePart(filePart);

        FileResponse response = fileStorageService.uploadFile(
                filePart.getInputStream(),
                getFileName(filePart),
                getContentType(filePart),
                filePart.getSize()
        );

        responseWriter.writeJsonResponse(resp, response);
        log.debug("File uploaded successfully: {} ({} bytes)",
                response.getOriginalFileName(), response.getFileSize());
    }

    private void validateMultipartRequest(HttpServletRequest req) {
        String contentType = req.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("multipart/form-data")) {
            throw new IllegalArgumentException("Content-Type must be multipart/form-data");
        }
    }

    private void validateFilePart(Part filePart) {
        if (filePart == null || filePart.getSize() == 0) {
            throw new IllegalArgumentException("File part is missing or empty");
        }
    }

    private String getFileName(Part filePart) {
        String fileName = filePart.getSubmittedFileName();
        return (fileName == null || fileName.isEmpty()) ?
                "uploaded_file_" + System.currentTimeMillis() : fileName;
    }

    private String getContentType(Part filePart) {
        String contentType = filePart.getContentType();
        return (contentType == null || contentType.isEmpty()) ?
                "application/octet-stream" : contentType;
    }
}