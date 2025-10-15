package controller.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.JsonMapper;
import controller.util.abstracts.ResponseWriter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class JsonResponseWriter implements ResponseWriter {
    private final ObjectMapper objectMapper;

    public JsonResponseWriter() {
        this.objectMapper = JsonMapper.getObjectMapper();
    }

    @Override
    public void writeJsonResponse(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        String json = objectMapper.writeValueAsString(data);
        resp.getWriter().write(json);
    }

    @Override
    public void writeTextResponse(HttpServletResponse resp, String text) throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(text);
    }
}
