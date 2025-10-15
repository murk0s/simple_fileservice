package controller;

import controller.handlers.DownloadHandler;
import controller.handlers.GetFilesHandler;
import controller.handlers.GetUrlHandler;
import controller.handlers.UpdateDownloadCountHandler;
import controller.handlers.UploadHandler;
import controller.handlers.abstracts.FileRequestHandler;
import controller.util.JsonResponseWriter;
import controller.util.abstracts.ResponseWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import service.FileStorageService;

import java.io.IOException;
import java.util.List;

@Slf4j
@MultipartConfig
public class FileController extends HttpServlet {

    private final FileStorageService fileStorageService;
    private final List<FileRequestHandler> requestHandlers;
    private final ResponseWriter responseWriter;

    public FileController(FileStorageService fileStorageService) {
        super();
        this.fileStorageService = fileStorageService;
        this.responseWriter = new JsonResponseWriter();
        this.requestHandlers = initializeHandlers();
    }
    private List<FileRequestHandler> initializeHandlers() {
        return List.of(
                new GetFilesHandler(fileStorageService, responseWriter),
                new DownloadHandler(fileStorageService),
                new UploadHandler(fileStorageService, responseWriter),
                new GetUrlHandler(fileStorageService, responseWriter),
                new UpdateDownloadCountHandler(fileStorageService, responseWriter)
        );
    }

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath();
        if (!path.equals("/api/v1/files") ) {
            return;
        }
        handleRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath();
        if (!path.equals("/api/v1/files") ) {
            return;
        }
        handleRequest(req, resp);
    }

    private void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            FileRequestHandler handler = findHandler(req);
            if (handler != null) {
                handler.handle(req, resp);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            log.debug("Bad request: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseWriter.writeTextResponse(resp, e.getMessage());
        } catch (ServletException e) {
            log.error("Servlet error", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseWriter.writeTextResponse(resp, "Error processing request");
        }
    }

    private FileRequestHandler findHandler(HttpServletRequest req) {
        return requestHandlers.stream()
                .filter(handler -> handler.canHandle(req))
                .findFirst()
                .orElse(null);
    }
}
