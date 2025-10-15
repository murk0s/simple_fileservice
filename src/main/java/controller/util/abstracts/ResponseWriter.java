package controller.util.abstracts;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface ResponseWriter {
    void writeJsonResponse(HttpServletResponse resp, Object data) throws IOException;
    void writeTextResponse(HttpServletResponse resp, String text) throws IOException;
}