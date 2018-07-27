package modules.server.servlets.rstree;


import com.google.gson.Gson;
import plugins.PluginReader;
import plugins.interfaces.TestExecutor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class GetRSMethodsServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Content-Type", "application/json; charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = response.getWriter();
        out.write(new Gson().toJson(getMethods()));
        out.flush();
        out.close();
    }

    private CopyOnWriteArrayList<RSGroupMethods> getMethods() {
        CopyOnWriteArrayList<RSGroupMethods> rsGroupMethodsList = new CopyOnWriteArrayList();
        PluginReader pluginReader = new PluginReader();

        for (TestExecutor testExecutor : pluginReader.getTestExecutors()) {
            RSGroupMethods addRSGroupMethods = new RSGroupMethods();
            addRSGroupMethods.setName(testExecutor.getGroupName()+" ("+testExecutor.getPluginName()+")");

            ArrayList<RSMethod> list = new ArrayList<>();
            for (Map.Entry<String, String> steps : testExecutor.getAllStepsMap().entrySet()) {
                RSMethod rsMethod = new RSMethod();
                rsMethod.setName(steps.getKey());
                rsMethod.setDescription(steps.getValue());
                list.add(rsMethod);
            }
            addRSGroupMethods.setList(list);
            rsGroupMethodsList.add(addRSGroupMethods);
        }
        return rsGroupMethodsList;
    }

    private class RSGroupMethods {
        String name = "";
        ArrayList<RSMethod> list = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ArrayList<RSMethod> getList() {
            return list;
        }

        public void setList(ArrayList<RSMethod> list) {
            this.list = list;
        }
    }

    private class RSMethod {
        String name, description;

        public void setName(String name) {
            this.name = name;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}