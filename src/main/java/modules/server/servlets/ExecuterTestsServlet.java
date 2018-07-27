package modules.server.servlets;

import com.google.gson.Gson;
import controllers.TestExecutorThreadCtrl;
import modules.configuration.Option;
import modules.configuration.Settings;
import modules.configuration.interfaces.ProgramSettings;
import modules.logger.interfaces.RSLogger;
import modules.server.PreserverServerData;
import modules.testExecutor.testReaders.TestsReaderFromFile;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecuterTestsServlet extends HttpServlet {
    private PreserverServerData preserverServerData;
    private ProgramSettings programSettings = new Settings(new Option());
    private RSLogger rsLogger;
    private ExecutorService executorService;

    public ExecuterTestsServlet(PreserverServerData preserverServerData, ProgramSettings programSettings, RSLogger rsLogger, ExecutorService executorService) {
        this.preserverServerData = preserverServerData;
        this.programSettings.updateAll(programSettings.getAllSettings());
        this.rsLogger = rsLogger;
        this.executorService = executorService;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ConcurrentHashMap<String, String> requestDataMap = preserverServerData.getRequestData(request, getServletContext());
        String fileName, message = "Запрос принят на выполнение";
        CopyOnWriteArrayList<String> suites = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<String> tests = new CopyOnWriteArrayList<>();

        TestsReaderFromFile testsReaderFromFile = new TestsReaderFromFile();

        if (requestDataMap.get("fileName") != null) {
            fileName = requestDataMap.get("fileName");

            rsLogger.getLogger().info("POST-запрос: ");
            rsLogger.getLogger().info("Выполнить файл (" + fileName + ")");

            if (requestDataMap.get("suite") != null) {
                suites.add(requestDataMap.get("suite"));
                rsLogger.getLogger().info("Набор: " + requestDataMap.get("suite"));
            }

            if (requestDataMap.get("test") != null) {
                tests.add(requestDataMap.get("test"));
                rsLogger.getLogger().info("Тест: " + requestDataMap.get("test"));
            }

            if (executorService == null || executorService.isShutdown()) {
                executorService = Executors.newFixedThreadPool(programSettings.getInteger("MaximumThreads"));
                try {
                    new TestExecutorThreadCtrl(executorService, Executors.newFixedThreadPool(1), testsReaderFromFile.read(fileName), suites, tests, programSettings, false, rsLogger);

                } catch (Throwable throwable) {
                    message = "Ошибка чтения файла: " + fileName;
                    rsLogger.getLogger().info("Ошибка чтения файла: " + fileName);
                    rsLogger.printStackTrace(throwable);
                }

            } else {
                message = "Приложение занято выполнением тестов. Повторите попытку позже!";
            }
        }

        response.setHeader("Content-Type", "application/json; charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = response.getWriter();
        rsLogger.getLogger().info(message);

        out.write(new Gson().toJson(message));
        out.flush();
        out.close();
    }
    /*
    private Logger log = new RSLogger().getLogger();
    private volatile ExecutorService executorService;
    private volatile long idThread;
    private RSTestLinkAPI rsTestLinkAPI = new RSTestLinkAPI();
    //private PreserverTestData preserverTestData = new PreserverTestData();
    private PreserverServerData preserverServerData;

    public ExecuterTestsServlet(PreserverServerData preserverServerData) {
        this.preserverServerData = preserverServerData;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean startThread = false;
        idThread++;
        PreserverTestData preserverTestData = new PreserverTestData();

        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(Integer.parseInt(System.getProperty("MaximumThreads")));
        }

        if (System.getProperty("OutputFirstTestFallen").toUpperCase().equals("TRUE")) {
            preserverTestData.addOutputFirstTestFallen(idThread, true);
        } else {
            preserverTestData.addOutputFirstTestFallen(idThread, false);
        }

        HashMap<String, String> requestDataMap = preserverServerData.getRequestData(request, getServletContext());

        if (!ServletFileUpload.isMultipartContent(request)) {
            send(response, "Content type is not multipart/form-data");

        } else {
            if (requestDataMap.get("fileName") != null && requestDataMap.get("fileName").length() > 4) {
                startThread = true;
                try {
                    log.info(" Request: " + idThread + "	" + "Запуск тестов из переданного объекта");
                    ExecTreeSteps execTreeSteps = new ExecTreeSteps();
                    execTreeSteps.setThreadId(idThread);
                    execTreeSteps.setExecutorService(executorService);
                    execTreeSteps.setHttpServletResponse(response);
                    execTreeSteps.setPreserverTestData(preserverTestData);

                    if (requestDataMap.get("suite") != null) {
                        execTreeSteps.setExecuteSuiteNames(new String[]{requestDataMap.get("suite")});
                    }

                    if (requestDataMap.get("test") != null) {
                        execTreeSteps.setExecuteTestNames(new String[]{requestDataMap.get("test")});
                    }

                    execTreeSteps.execRSTests(preserverServerData.getRSTests(requestDataMap.get("fileName")).clone());
                } catch (Exception e) {
                    sendError(response, "Ошибка выполнения тестов из переданного объекта", e);
                }
            }
        }
        if (!startThread) send(response, "Запуск тестов не выполнен.");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PreserverTestData preserverTestData = new PreserverTestData();

        boolean startThread = false;
        idThread++;

        log.info(" Request: " + idThread + "	" + "Соединение открыто");

        RSConfig rsConfig = new RSConfig();
        rsConfig.readArguments(request.getQueryString());

        rsTestLinkAPI.setTestLinkURL(System.getProperty("TestLink_URL"));
        rsTestLinkAPI.setTestLinkKey(System.getProperty("TestLink_Key"));
        rsTestLinkAPI.setTestLinkProject(System.getProperty("TestLink_Project"));
        rsTestLinkAPI.setTestLinkPlan(System.getProperty("TestLink_Plan"));
        rsTestLinkAPI.setTestLinkBuild(System.getProperty("TestLink_Build"));
        rsTestLinkAPI.setTestLinkBuildNotes(System.getProperty("TestLink_BuildNotes"));

        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(Integer.parseInt(System.getProperty("MaximumThreads")));
        }

        if (System.getProperty("OutputFirstTestFallen").toUpperCase().equals("TRUE")) {
            preserverTestData.addOutputFirstTestFallen(idThread, true);
        } else {
            preserverTestData.addOutputFirstTestFallen(idThread, false);
        }

        if (rsConfig.isToRunTestsFromTestLink() && System.getProperty("TestLink_OneTest").equals("")) {
            startThread = true;
            try {
                log.info(" Request: " + idThread + "	" + "Запуск тестов из TestLink");
                ExecTreeSteps execTreeSteps = new ExecTreeSteps();
                execTreeSteps.setThreadId(idThread);
                execTreeSteps.setExecutorService(executorService);
                execTreeSteps.setHttpServletResponse(response);
                execTreeSteps.setPreserverTestData(preserverTestData);
                execTreeSteps.execFromTestLink(rsTestLinkAPI);
            } catch (Exception e) {
                sendError(response, "Ошибка выполнения тестов из TestLink", e);
            }
        }

        if (rsConfig.isToRunTestsFromTestLink() && !System.getProperty("TestLink_OneTest").equals("")) {
            startThread = true;
            try {
                log.info(" Request: " + idThread + "	" + "Запуск одного теста из TestLink");
                ExecTreeSteps execTreeSteps = new ExecTreeSteps();
                execTreeSteps.setThreadId(idThread);
                execTreeSteps.setExecutorService(executorService);
                execTreeSteps.setHttpServletResponse(response);
                execTreeSteps.setPreserverTestData(preserverTestData);
                execTreeSteps.execFromOneTestTestLink(rsTestLinkAPI, System.getProperty("TestLink_OneTest"));
            } catch (Exception e) {
                sendError(response, "Ошибка выполнения тестов из TestLink", e);
            }
        }

        for (String filePath : rsConfig.getFilesWithTestsArray()) {
            startThread = true;
            try {
                log.info(" Request: " + idThread + "	" + "Запуск тестов из файла");
                ExecTreeSteps execTreeSteps = new ExecTreeSteps();
                execTreeSteps.setThreadId(idThread);
                execTreeSteps.setExecutorService(executorService);
                execTreeSteps.setHttpServletResponse(response);
                execTreeSteps.setPreserverTestData(preserverTestData);
                execTreeSteps.execFromJsonFile(filePath);
            } catch (Exception e) {
                sendError(response, "Ошибка выполнения тестов из файла", e);
            }
        }
        if (!startThread) send(response, "Запуск тестов не выполнен.");
    }

    private void sendError(HttpServletResponse response, String errorMessage, Exception e) {
        errorMessage += " " + e.getMessage();
        send(response, errorMessage);
    }

    private void send(HttpServletResponse response, String message) {
        try {
            log.info(" Request: " + idThread + "	" + message);

            response.setContentType("text/html");
            response.setHeader("Content-Type", "text/html;; charset=UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "*");
            PrintWriter out = response.getWriter();
            out.write("<html><head></head><body>");
            out.write(message);
            out.write("</body></html>");
            out.flush();
            out.close();

            log.info(" Request: " + idThread + " " + "	Соединение закрыто.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
