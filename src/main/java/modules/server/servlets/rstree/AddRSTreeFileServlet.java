package modules.server.servlets.rstree;

import com.google.gson.Gson;
import modules.server.PreserverServerData;
import modules.testExecutor.templates.RSTests;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AddRSTreeFileServlet extends HttpServlet {
    private PreserverServerData preserverServerData;

    public AddRSTreeFileServlet(PreserverServerData preserverServerData) {
        this.preserverServerData = preserverServerData;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ConcurrentHashMap<String, String> requestDataMap = preserverServerData.getRequestData(request, getServletContext());

        SendJson sendJson = new SendJson();
        String message;

        if (!ServletFileUpload.isMultipartContent(request)) {
            message = "Content type is not multipart/form-data";

        } else {
            if (requestDataMap.get("fileName") != null
                    && requestDataMap.get("fileName").length() > 1
                    && requestDataMap.get("content") != null
                    && requestDataMap.get("content").length() > 4 ) {

                preserverServerData.addFileNameTests(requestDataMap.get("fileName"), requestDataMap.get("content"));

                sendJson.setFileValue(preserverServerData.getRSTests(requestDataMap.get("fileName")));
                sendJson.setFileList(preserverServerData.getFileList());
                message = new Gson().toJson(sendJson);

            } else {
                message = "Файл не передан или неверный json-формат";
            }
        }

        response.setHeader("Content-Type", "application/json; charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = response.getWriter();
        out.write(message);
        out.flush();
        out.close();
    }

    private class SendJson {
        CopyOnWriteArrayList<String> fileList;
        RSTests fileValue;

        void setFileList(CopyOnWriteArrayList<String> fileList) {
            this.fileList = fileList;
        }

        void setFileValue(RSTests fileValue) {
            this.fileValue = fileValue;
        }
    }
}



