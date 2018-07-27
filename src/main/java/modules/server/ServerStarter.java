package modules.server;

import modules.commandLine.interfaces.CommandLlineExecuted;
import modules.configuration.interfaces.ProgramSettings;
import modules.logger.interfaces.RSLogger;
import modules.server.servlets.DownloadFileServlet;
import modules.server.servlets.ExecuterTestsServlet;
import modules.server.servlets.ResourceSenderServlet;
import modules.server.servlets.rstree.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.StdErrLog;

import java.util.concurrent.ExecutorService;

/**
 * Класс для запуска сервера.
 *
 * @author nechkin.sergei.sergeevich
 */
public class ServerStarter implements CommandLlineExecuted {

    @Override
    public void execute(ProgramSettings programSettings, RSLogger rsLogger) {

        /* Отключить логирование Jetty */
        StdErrLog stdErrLog = new StdErrLog();
        stdErrLog.setLevel(10);
        Log.setLog(stdErrLog);
        /******************************/

        PreserverServerData preserverServerData = new PreserverServerData(rsLogger);

        // Загрузить ранее редактируемые файлы из директории
        preserverServerData.readFiles("edit_web_files");

        Server server = new Server(Integer.valueOf(programSettings.get("ServerPort")));
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        server.setHandler(context);

        ExecutorService executorService = null;

        context.addServlet(new ServletHolder(new ResourceSenderServlet()), "/");
        context.addServlet(new ServletHolder(new ExecuterTestsServlet(preserverServerData, programSettings, rsLogger, executorService)), "/execute");
        context.addServlet(new ServletHolder(new AddRSTreeFileServlet(preserverServerData)), "/rstree/addFile");
        context.addServlet(new ServletHolder(new GetRSTreeFileServlet(preserverServerData)), "/rstree/getFile");
        context.addServlet(new ServletHolder(new RSTreeServlet(preserverServerData)), "/rstree/tree");
        context.addServlet(new ServletHolder(new RemoveFileServlet(preserverServerData)), "/removeFile");
        context.addServlet(new ServletHolder(new DownloadFileServlet(preserverServerData)), "/downloadFile");
        context.addServlet(new ServletHolder(new GetRSMethodsServlet()), "/getRSMethods");

        try {
            rsLogger.getLogger().info("Server started");
            rsLogger.getLogger().info("ServerPort: " + programSettings.get("ServerPort"));
            rsLogger.getLogger().info("URL: "
                    + server.getURI().toString().substring(0, server.getURI().toString().length() - 1)
                    + ":" + programSettings.get("ServerPort")
                    + "/");
            rsLogger.getLogger().info("Press to exit Ctrl+C");

            server.start();
            server.join();

        } catch (Throwable t) {
            rsLogger.getLogger().info(t.getMessage());
            rsLogger.getLogger().info("Ошибка запуска сервера. Проверьте порт: " + programSettings.get("ServerPort"));
            rsLogger.getLogger().info("Для изменения порта используйте команду SERVER ServerPort=8080");
            System.exit(1);
        }
    }
}
