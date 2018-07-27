package modules.testExecutor;

import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.TestThread;
import org.apache.commons.io.FilenameUtils;
import plugins.interfaces.ReportWriter;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * Класс передаёт выполнение методам отчётов из списка добавленных отчётов (reports).
 *
 * @author nechkin.sergei.sergeevich
 */
public class Report implements ReportWriter {
    private CopyOnWriteArrayList<ReportWriter> reports = new CopyOnWriteArrayList<>();
    private SuiteDatas suite;
    private TestDatas test;

    /**
     * Конструктор
     *
     * @param reports Список отчётов
     */
    public Report(CopyOnWriteArrayList<ReportWriter> reports, SuiteDatas suite, TestDatas test, ConcurrentHashMap<Long, CopyOnWriteArrayList<TestThread>> testThreads, ExecutorService executorService) {
        this.suite = suite;
        this.test = test;
        this.reports.addAll(reports);

        String thisJarFileName = "System";
        String reportDirectory = test.getProgramSettings().get("ReportResultsDirectory");

        try {/* Получить наименование текщего jar-файла */
            thisJarFileName = FilenameUtils.getBaseName(new File(main.Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getName());
        } catch (Throwable t) {
        }

        // Всем отчетам передать настройки
        for (ReportWriter report : reports) {
            ConcurrentHashMap<String, String> settings = new ConcurrentHashMap<>();

            try { // Прочитать настройки для плагина
                settings = test.getProgramSettings().readPropertiesFile(thisJarFileName + File.separator + report.getPluginName() + ".properties", report.getDefaultSettings(), false);
            } catch (Throwable t) {
                //test.getLogger().info(e.getMessage());
            }
            report.set(suite, test, testThreads, thisJarFileName, reportDirectory, settings, executorService);
        }
    }

    @Override
    public String getPluginName() {
        return null;
    }

    @Override
    public ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getDefaultSettings() {
        return null;
    }

    @Override
    public void set(SuiteDatas suite, TestDatas test, ConcurrentHashMap<Long, CopyOnWriteArrayList<TestThread>> testThreads, String programFilesDirectory, String reportDirectory, ConcurrentHashMap<String, String> settings, ExecutorService executorService) {

    }

    @Override
    public void addAttachment(String name, byte[] attachment) {
        for (ReportWriter report : reports) {
            try {
                report.addAttachment(name, attachment);
            } catch (Throwable t) {
                test.printStackTrace(t);
            }
        }
    }

    @Override
    public void suiteStarted(String name) {
        for (ReportWriter report : reports) {
            try {
                report.suiteStarted(name);
            } catch (Throwable t) {
                test.printStackTrace(t);
            }
        }
    }

    @Override
    public void suiteFailure() {
        for (ReportWriter report : reports) {
            try {
                report.suiteFailure();
            } catch (Throwable t) {
                test.printStackTrace(t);
            }
        }
    }

    @Override
    public void suiteFinished() {
        for (ReportWriter report : reports) {
            try {
                report.suiteFinished();
            } catch (Throwable t) {
                test.printStackTrace(t);
            }
        }
    }

    @Override
    public void testStarted(String name) {
        for (ReportWriter report : reports) {
            try {
                report.testStarted(name);
            } catch (Throwable t) {
                test.printStackTrace(t);
            }
        }
    }

    @Override
    public void testCanceled(String message) {
        for (ReportWriter report : reports) {
            try {
                report.testCanceled(message);
            } catch (Throwable t) {
                test.printStackTrace(t);
            }
        }
    }

    @Override
    public void testBroken(String message) {
        for (ReportWriter report : reports) {
            try {
                report.testBroken(message);
            } catch (Throwable t) {
                test.printStackTrace(t);
            }
        }
    }

    @Override
    public void testFailure(String message) {
        for (ReportWriter report : reports) {
            try {
                report.testFailure(message);
            } catch (Throwable t) {
                test.printStackTrace(t);
            }
        }
    }

    @Override
    public void testFinished() {
        for (ReportWriter report : reports) {
            try {
                report.testFinished();
            } catch (Throwable t) {
                test.printStackTrace(t);
            }
        }
    }

    @Override
    public void stepStarted(String name) {
        for (ReportWriter report : reports) {
            try {
                report.stepStarted(name);
            } catch (Throwable t) {
                test.printStackTrace(t);
            }
        }
    }

    @Override
    public void stepBroken(String message) {
        for (ReportWriter report : reports) {
            try {
                report.stepBroken(message);
            } catch (Throwable t) {
                test.printStackTrace(t);
            }
        }
    }

    @Override
    public void stepFailure(String message) {
        for (ReportWriter report : reports) {
            try {
                report.stepFailure(message);
            } catch (Throwable t) {
                test.printStackTrace(t);
            }
        }
    }

    @Override
    public void stepCanceled() {
        for (ReportWriter report : reports) {
            try {
                report.stepCanceled();
            } catch (Throwable t) {
                test.printStackTrace(t);
            }
        }
    }

    @Override
    public void stepFinished() {
        for (ReportWriter report : reports) {
            try {
                report.stepFinished();
            } catch (Throwable t) {
                test.printStackTrace(t);
            }
        }
    }

    @Override
    public void allSuiteFinished() {
        for (ReportWriter report : reports) {
            try {
                report.allSuiteFinished();
            } catch (Throwable t) {
                test.printStackTrace(t);
            }
        }
    }
}
