package modules.server.servlets.rstree;

import com.google.gson.Gson;
import modules.server.PreserverServerData;
import modules.testExecutor.templates.RSTests;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RemoveFileServlet extends HttpServlet {
    private PreserverServerData preserverServerData;

    public RemoveFileServlet(PreserverServerData preserverServerData) {
        this.preserverServerData = preserverServerData;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ConcurrentHashMap<String, String> requestDataMap = preserverServerData.getRequestData(request, getServletContext());
        SendJson sendJson = new SendJson();
        if (requestDataMap.get("fileName") == null && preserverServerData.getRSTests(requestDataMap.get("fileName")) == null) {
            for (Map.Entry<String, RSTests> entry : preserverServerData.getFilesWithTests().entrySet()) {
                sendJson.setFileValue(entry.getValue()); // отправить первый файл в массиве
                sendJson.setError(true);
                break;
            }
        } else {
            preserverServerData.deleteFileNameTests(requestDataMap.get("fileName"));
            sendJson.setError(false);
        }
        sendJson.setFileList(preserverServerData.getFileList());
        response.setHeader("Content-Type", "application/json; charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = response.getWriter();
        out.write(new Gson().toJson(sendJson));
        out.flush();
        out.close();
    }

    private class SendJson {
        CopyOnWriteArrayList<String> fileList;
        RSTests fileValue;
        boolean isError;

        void setFileList(CopyOnWriteArrayList<String> fileList) {
            this.fileList = fileList;
        }

        void setFileValue(RSTests fileValue) {
            this.fileValue = fileValue;
        }

        public void setError(boolean error) {
            isError = error;
        }
    }
}



