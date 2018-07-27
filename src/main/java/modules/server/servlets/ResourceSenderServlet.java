package modules.server.servlets;

import modules.filesHandlers.RSMimeType;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceSenderServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (path.equals("/")) path = "/index.html";

        Path fPath = Paths.get("RSAutotest/web"+path);
        byte[] fileByte = Files.readAllBytes(fPath);

        if (fileByte != null) {
            response.setHeader("Content-Type", new RSMimeType().getMimeType(path, fileByte) + "; charset=UTF-8");
            response.setContentLength(fileByte.length);
            ServletOutputStream stream = response.getOutputStream();
            stream.write(fileByte);
            stream.flush();
        }
    }
}
