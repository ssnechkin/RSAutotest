package plugins.reports.allure;

import modules.filesHandlers.RSMimeType;
import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.TestThread;
import plugins.interfaces.ReportWriter;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.events.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.zip.ZipOutputStream;

public class AllureReport implements ReportWriter {
    private String reportDirectory = "report";
    private String programFilesDirectory;
    private String suiteUid = UUID.randomUUID().toString();
    private ConcurrentHashMap<String, String> settings = new ConcurrentHashMap<>();
    private SuiteDatas suite;
    private TestDatas test;
    private AllureHelper allureHelper;

    @Override
    public String getPluginName() {
        return "Allure";
    }

    @Override
    public ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getDefaultSettings() {
        ConcurrentHashMap<String, ConcurrentHashMap<String, String>> properties = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> settings;

        settings = new ConcurrentHashMap<>();
        settings.put("ReportResultsDirectory", "allure-results");
        properties.put("Имя директория для записи не сгенерированных отчётов", settings);

        settings = new ConcurrentHashMap<>();
        settings.put("ReportFaceDirectory", "allure-report");
        properties.put("Имя директория для записи сформированных отчётов из результатов", settings);

        settings = new ConcurrentHashMap<>();
        settings.put("ReportRemoteDirectory", "");
        properties.put("Директория для копирования сгенерированного отчёта. Пример //10.0.2.85/share/report/allure2", settings);

        settings = new ConcurrentHashMap<>();
        settings.put("ClearReportResults", "true");
        properties.put("Удалять результаты предыдущих запусков", settings);

        settings = new ConcurrentHashMap<>();
        settings.put("ZipName", "allure");
        properties.put("Наименование архива с упакованным отчётом", settings);

        settings = new ConcurrentHashMap<>();
        settings.put("ReportUID", "");
        properties.put("Идентификатор полседнего сформированного отчёта", settings);

        return properties;
    }

    @Override
    public void set(SuiteDatas suite, TestDatas test, ConcurrentHashMap<Long, CopyOnWriteArrayList<TestThread>> testThreads, String programFilesDirectory, String reportDirectory, ConcurrentHashMap<String, String> settings, ExecutorService executorService) {
        this.settings.clear();
        this.settings.putAll(settings);
        this.reportDirectory = reportDirectory;
        this.programFilesDirectory = programFilesDirectory;
        this.suite = suite;
        this.test = test;

        allureHelper = new AllureHelper(suite);

        // Установить путь к файлам отчёта Allure. Для классов Allure.
        System.setProperty("allure.results.directory", reportDirectory + File.separator + settings.get("ReportResultsDirectory"));

        File reportResultsDirectory = new File(reportDirectory + File.separator + settings.get("ReportResultsDirectory"));
        File reportFaceDirectory = new File(reportDirectory + File.separator + settings.get("ReportFaceDirectory"));
        File zipfile = new File(reportDirectory + File.separator + settings.get("ZipName") + ".zip");


        if (zipfile.exists() || reportFaceDirectory.exists()) {
            if (settings.get("ClearReportResults").equals("true") && reportResultsDirectory.exists()) {
                allureHelper.deleteDirectory(reportResultsDirectory);
            }

            if (settings.get("ClearReportResults").equals("true") && reportResultsDirectory.exists()) {
                reportResultsDirectory.delete();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (settings.get("ClearReportResults").equals("true") && reportResultsDirectory.exists()) {
                allureHelper.deleteDirectory(reportResultsDirectory);
            }

            if (settings.get("ClearReportResults").equals("true") && reportResultsDirectory.exists()) {
                reportResultsDirectory.delete();
            }

            if (!reportResultsDirectory.exists()) reportResultsDirectory.mkdir();

            if (reportFaceDirectory.exists())
                allureHelper.deleteDirectory(reportFaceDirectory);

            if (zipfile.exists())
                allureHelper.deleteDirectory(zipfile);

            updatePropertiesSuiteUid("");
        }
    }

    private void updatePropertiesSuiteUid(String suiteUid) {
        ConcurrentHashMap<String, ConcurrentHashMap<String, String>> properties = getDefaultSettings();
        String key;
        for (Map.Entry<String, ConcurrentHashMap<String, String>> prop : properties.entrySet()) {
            key = "";
            for (Map.Entry<String, String> string : prop.getValue().entrySet()) {
                key = string.getKey();
                break;
            }
            if (key.length() > 0) {
                if (!key.equals("ReportUID") && settings.get(key) != null) {
                    prop.getValue().put(key, settings.get(key));
                } else {
                    prop.getValue().put(key, suiteUid);
                }
            }
        }

        try {
            test.getProgramSettings().readPropertiesFile(programFilesDirectory + File.separator + getPluginName() + ".properties", properties, true);
        } catch (IOException e) {
            //suite.printStackTrace(e);
        }
    }

    public void addAttachment(String name, byte[] attachment) {
        if (name != null && attachment != null && attachment.length > 0) {
            try {
                Allure.LIFECYCLE.fire(new MakeAttachmentEvent(attachment, name, new RSMimeType().getMimeType(name, attachment)));
            } catch (IOException e) {
            }
        }
    }

    public void suiteStarted(String name) {
        Allure.LIFECYCLE.fire(new TestSuiteStartedEvent(suiteUid, name));
    }

    public void suiteFailure() {
    }

    public void suiteFinished() {
        Allure.LIFECYCLE.fire(new TestSuiteFinishedEvent(suiteUid));
    }

    public void testStarted(String name) {
        Allure.LIFECYCLE.fire(new TestCaseStartedEvent(suiteUid, name));
    }

    public void testCanceled(String message) {
        Allure.LIFECYCLE.fire(new TestCaseCanceledEvent().withThrowable(new Exception(message)));
    }

    public void testBroken(String message) {
        Allure.LIFECYCLE.fire(new TestCaseFailureEvent().withThrowable(new Exception(message)));
    }

    public void testFailure(String message) {
        Allure.LIFECYCLE.fire(new TestCaseFailureEvent().withThrowable(new AssertionError(message)));
    }

    public void testFinished() {
        Allure.LIFECYCLE.fire(new TestCaseFinishedEvent());
    }

    public void stepStarted(String name) {
        Allure.LIFECYCLE.fire(new StepStartedEvent(name));
    }

    public void stepBroken(String message) {
        Allure.LIFECYCLE.fire(new StepFailureEvent().withThrowable(new Exception(message)));
    }

    public void stepFailure(String message) {
        Allure.LIFECYCLE.fire(new StepFailureEvent().withThrowable(new AssertionError(message)));
    }

    public void stepCanceled() {
        Allure.LIFECYCLE.fire(new StepCanceledEvent());
    }

    public void stepFinished() {
        Allure.LIFECYCLE.fire(new StepFinishedEvent());
    }

    @Override
    public void allSuiteFinished() {
        String results = null, resultsFace = null, outRemoteResults = null, offline = null;

        suite.getProgramLogger().info("");
        suite.getProgramLogger().info("Начало формирования Allure-отчёта.");

        try {
            results = reportDirectory + File.separator + settings.get("ReportResultsDirectory");
            resultsFace = reportDirectory + File.separator + settings.get("ReportFaceDirectory");
            outRemoteResults = settings.get("ReportRemoteDirectory") + File.separator + suiteUid;
            offline = reportDirectory + File.separator + "allure";
        } catch (Throwable t) {
            suite.getProgramLogger().info("Неудалось получить параметры для формирования отчёта");
            suite.printStackTrace(t);
        }

        try {
            if (results != null && resultsFace != null)
                allureHelper.generateAllureFace(results, resultsFace);
        } catch (Throwable t) {
            suite.getProgramLogger().info("Неудалось сгенерировать страницы отчёта");
            suite.printStackTrace(t);
        }

        try {
            if (new File(resultsFace + File.separator + "styles.css").exists())
                allureHelper.replaceTextInFile(resultsFace + File.separator + "styles.css", ".node__order{", "div[data-name='sorter.order']{display: none;}.node__order{display: none;");

            if (!new File(offline).exists()) new File(offline).mkdirs();
            allureHelper.copyDirectory(new File(resultsFace), new File(offline));
            allureHelper.unzipOneFile(reportDirectory, programFilesDirectory + ".jar", "report_viewer.jar");
            allureHelper.unzipOneFile(reportDirectory, programFilesDirectory + ".jar", "run.bat");

            FileOutputStream fos = new FileOutputStream(resultsFace + File.separator + settings.get("ZipName") + ".zip");
            ZipOutputStream zos = new ZipOutputStream(fos);
            allureHelper.addFileInZip(zos, new File(reportDirectory + File.separator + "report_viewer.jar"), "report_viewer.jar");
            allureHelper.addFileInZip(zos, new File(reportDirectory + File.separator + "run.bat"), "run.bat");
            allureHelper.addDirectoryInZip(zos, new File(offline));
            zos.close();

            new File(reportDirectory + File.separator + "report_viewer.jar").delete();
            new File(reportDirectory + File.separator + "run.bat").delete();
            allureHelper.deleteDirectory(new File(offline));

            if (settings.get("ReportRemoteDirectory") != null && settings.get("ReportRemoteDirectory").length() > 0) {
                if (!new File(outRemoteResults).exists()) new File(outRemoteResults).mkdirs();
                allureHelper.copyDirectory(new File(resultsFace), new File(outRemoteResults));
            }

            updatePropertiesSuiteUid(suiteUid);

            suite.getProgramLogger().info("Allure-отчёт сформирован.");

        } catch (Throwable t) {
            suite.getProgramLogger().info("Ошибка формирования Allure-отчёта.");
            suite.printStackTrace(t);
        }
    }
}