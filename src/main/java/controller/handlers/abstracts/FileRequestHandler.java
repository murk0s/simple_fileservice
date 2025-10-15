package controller.handlers.abstracts;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface FileRequestHandler {
    boolean canHandle(HttpServletRequest req);
    void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException;
}
