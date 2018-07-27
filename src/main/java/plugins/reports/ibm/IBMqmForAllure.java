package plugins.reports.ibm;

import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.TestThread;
import plugins.interfaces.ReportWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

public class IBMqmForAllure implements ReportWriter {
    private ConcurrentHashMap<String, String> settings = new ConcurrentHashMap<>();
    private SuiteDatas suite;
    private TestDatas test;
    private ConcurrentHashMap<Long, CopyOnWriteArrayList<TestThread>> testThreads;
    private String reportDirectory = "report";
    private String allurePluginName = "Allure";
    private String reportUID = "ReportUID";
    private String programFilesDirectory;
    private String progressFile = System.getenv("qm_ExecutionPropertiesFile");
    private String qm_AttachmentsFile = System.getenv("qm_AttachmentsFile");
    private Integer allSteps = 0;
    private ExecutorService executorService;

    @Override
    public String getPluginName() {
        return "IBM_QM_ALLURE";
    }

    @Override
    public ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getDefaultSettings() {
        ConcurrentHashMap<String, ConcurrentHashMap<String, String>> properties = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> settings;

        settings = new ConcurrentHashMap<>();
        settings.put("ReportWebPath", "");
        properties.put("URL-адрес на Allure-отчёт в IBM QM. Приммер: http://10.0.2.85/report/allure2/ ", settings);

        return properties;
    }

    @Override
    public void set(SuiteDatas suite, TestDatas test, ConcurrentHashMap<Long, CopyOnWriteArrayList<TestThread>> testThreads, String programFilesDirectory, String reportDirectory, ConcurrentHashMap<String, String> settings, ExecutorService executorService) {
        this.settings.clear();
        this.settings.putAll(settings);
        this.suite = suite;
        this.test = test;
        this.testThreads = testThreads;
        this.reportDirectory = reportDirectory;
        this.programFilesDirectory = programFilesDirectory;
        this.executorService = executorService;

        if (progressFile != null) {
            // Цикл по наборам <id набора, список тестов в наборе>
            for (Map.Entry<Long, CopyOnWriteArrayList<TestThread>> threads : testThreads.entrySet()) {
                // Цикл по тестам в наборе
                for (TestThread testThread : threads.getValue()) {
                    TestDatas testDatas = testThread.getSuitesMap().get(testThread.getSuiteId()).getTestsMap().get(testThread.getTestId());
                    allSteps += testDatas.getNumberOfSteps();
                }
            }
        }
    }

    @Override
    public void addAttachment(String name, byte[] attachment) {

    }

    @Override
    public void suiteStarted(String name) {

    }

    @Override
    public void suiteFailure() {

    }

    @Override
    public void suiteFinished() {

    }

    @Override
    public void testStarted(String name) {

    }

    @Override
    public void testCanceled(String message) {

    }

    @Override
    public void testBroken(String message) {

    }

    @Override
    public void testFailure(String message) {

    }

    @Override
    public void testFinished() {

    }

    @Override
    public void stepStarted(String name) {

    }

    @Override
    public void stepBroken(String message) {

    }

    @Override
    public void stepFailure(String message) {

    }

    @Override
    public void stepCanceled() {

    }

    @Override
    public void stepFinished() {
        threadStart();
    }

    @Override
    public void allSuiteFinished() {
        threadStart();
        if (new File(programFilesDirectory + File.separator + allurePluginName + ".properties").exists() && qm_AttachmentsFile != null) {
            suite.getProgramLogger().info("");
            suite.getProgramLogger().info("Формирование ссылок для IBM QM");
            ConcurrentHashMap<String, String> allureSettings = new ConcurrentHashMap<>();
            try {
                allureSettings = test.getProgramSettings().readPropertiesFile(programFilesDirectory + File.separator + allurePluginName + ".properties", null, false);
            } catch (IOException e) {
                //suite.printStackTrace(e);
            }
            try {
                reportUID = allureSettings.get(reportUID);

                if (reportUID != null) {
                    String[] reportIBMQM = {
                            System.getProperty("line.separator") + "ReportAllure=" + settings.get("ReportWebPath") + reportUID + "/index.html#suites",
                            System.getProperty("line.separator") + "Report.zip=" + settings.get("ReportWebPath") + reportUID + "/" + allureSettings.get("ZipName") + ".zip"
                    };
                    writeFile(qm_AttachmentsFile, reportIBMQM);
                }
                suite.getProgramLogger().info("Cсылки для IBM QM cормированы");

            } catch (Throwable t) {
                suite.printStackTrace(t);
            }
        }
    }

    private void threadStart() {
        if (progressFile != null) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    writeProgress();
                    try {
                        Thread.sleep(4320);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            executorService.execute(thread);
        }
    }

    private void writeProgress() {
        Integer progress = 0;
        // Цикл по наборам <id набора, список тестов в наборе>
        for (Map.Entry<Long, CopyOnWriteArrayList<TestThread>> threads : testThreads.entrySet()) {
            // Цикл по тестам в наборе
            for (TestThread testThread : threads.getValue()) {
                TestDatas testDatas = testThread.getSuitesMap().get(testThread.getSuiteId()).getTestsMap().get(testThread.getTestId());
                progress += testDatas.getNumberOfСompletedSteps();
            }
        }

        progress = Math.round((progress * 100) / allSteps);
        if (progress >= 100) progress = 99;

        try {
            Properties props = new Properties();
            props.setProperty("progress", progress + "");

            FileOutputStream out = new FileOutputStream(progressFile);
            props.store(out, progress + "");
            out.close();
        } catch (Exception e) {
            suite.printStackTrace(e);
        }
    }

    private void writeFile(String filePathAndName, String[] data) {
        OutputStream os = null;
        if (filePathAndName != null) {
            if (!new File(filePathAndName).exists()) {
                try {
                    new File(filePathAndName).createNewFile();
                } catch (Exception e) {
                    suite.printStackTrace(e);
                }
            }
            if (filePathAndName != null && new File(filePathAndName).exists()) {
                try {
                    os = new FileOutputStream(new File(filePathAndName));
                    for (String text : data) {
                        os.write((text + "\n").getBytes(), 0, text.length());
                    }
                } catch (IOException e) {
                    suite.printStackTrace(e);
                } finally {
                    try {
                        os.close();
                    } catch (IOException e) {
                        suite.printStackTrace(e);
                    }
                }
            }
        }
    }
}