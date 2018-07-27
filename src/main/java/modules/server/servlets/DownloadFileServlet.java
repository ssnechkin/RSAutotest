package modules.server.servlets;

import com.google.gson.Gson;
import modules.server.PreserverServerData;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

public class DownloadFileServlet extends HttpServlet {
    private PreserverServerData preserverServerData;

    public DownloadFileServlet(PreserverServerData preserverServerData) {
        this.preserverServerData = preserverServerData;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (preserverServerData.getRSTests(request.getParameter("fileName")) != null) {
            byte[] fileByte = new Gson().toJson(preserverServerData.getRSTests(request.getParameter("fileName")))

                    .replace("\"notRun\":false,", "")
                    .replace(",\"notRun\":false", "")
                    .replace("\"notRun\":false", "")

                    .replace("\"notShow\":false,", "")
                    .replace(",\"notShow\":false", "")
                    .replace("\"notShow\":false", "")

                    .replace("\"skipBagButTestFailed\":false,", "")
                    .replace(",\"skipBagButTestFailed\":false", "")
                    .replace("\"skipBagButTestFailed\":false", "")

                    .replace("\"skipBag\":false,", "")
                    .replace(",\"skipBag\":false", "")
                    .replace("\"skipBag\":false", "")

                    .replace("\"beginCycle\":false,", "")
                    .replace(",\"beginCycle\":false", "")
                    .replace("\"beginCycle\":false", "")

                    .replace("\"endCycle\":false,", "")
                    .replace(",\"endCycle\":false", "")
                    .replace("\"endCycle\":false", "")

                    .replace("\"dependingOnTheTestsList\":[],", "")
                    .replace(",\"dependingOnTheTestsList\":[]", "")
                    .replace("\"dependingOnTheTestsList\":[]", "")

                    .replace("\"errorMessage\":\"\",", "")
                    .replace(",\"errorMessage\":\"\"", "")
                    .replace("\"errorMessage\":\"\"", "")

                    .replace("\"attachments\":{},", "")
                    .replace(",\"attachments\":{}", "")
                    .replace("\"attachments\":{}", "")

                    .getBytes("UTF-8");

            response.setContentType("application/json");
            response.setContentLength(fileByte.length);

            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", URLEncoder.encode(request.getParameter("fileName"), "UTF-8"));
            response.setHeader(headerKey, headerValue);
            OutputStream outStream = response.getOutputStream();
            outStream.write(fileByte);
            outStream.close();
        }
    }
}