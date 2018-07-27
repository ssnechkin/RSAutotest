package modules.testExecutor;

import modules.testExecutor.enums.StepStatus;
import modules.testExecutor.interfaces.CalledFromTest;
import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.RSTests;
import org.apache.commons.io.FilenameUtils;
import plugins.interfaces.DataHandler;
import plugins.interfaces.TestExecutor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class StepExecutor {
    private SuiteDatas suite;
    private TestDatas test;
    private Logger logger;
    private Boolean threadSuspended;
    private ConcurrentHashMap<String, String> stringDataMap;
    private ConcurrentHashMap<String, byte[]> byteDataMap;
    private CopyOnWriteArrayList<TestExecutor> testExecutorList = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<DataHandler> dataHandlers = new CopyOnWriteArrayList<>();

    /**
     * Конструктор
     *
     * @param testExecutorList Список исполнителей шагов
     * @param dataHandlers     Список обработчиков данных шага
     * @param suite            Данные набора
     * @param test             Данные теста
     * @param logger           Логирование теста
     * @param threadSuspended  Переменная для нотификации о возобновлении приостановленного потока
     * @param mapOfTestCalls   Список объектов для вызова из потока с тестом
     */
    public StepExecutor(CopyOnWriteArrayList<TestExecutor> testExecutorList, CopyOnWriteArrayList<DataHandler> dataHandlers, SuiteDatas suite, TestDatas test, Logger logger, Boolean threadSuspended, ConcurrentHashMap<String, CalledFromTest> mapOfTestCalls) {
        String thisJarFileName = "System";
        this.testExecutorList.addAll(testExecutorList);
        this.dataHandlers.addAll(dataHandlers);
        this.suite = suite;
        this.test = test;
        this.logger = logger;
        this.threadSuspended = threadSuspended;
        stringDataMap = test.getStringDataMap();
        byteDataMap = test.getByteDataMap();

        // Скопировать данные для теста из общих данных по набору
        stringDataMap.putAll(suite.getStringDataMap());
        byteDataMap.putAll(suite.getByteDataMap());

        try {/* Получить наименование текщего jar-файла */
            thisJarFileName = FilenameUtils.getBaseName(new File(main.Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getName());
        } catch (Exception e) {
        }

        // Всем исполнителям передать ссылки текущего класса
        for (TestExecutor testExecutor : testExecutorList) {
            ConcurrentHashMap<String, String> settings = new ConcurrentHashMap<>();

            try { // Прочитать настройки для плагина
                settings = test.getProgramSettings().readPropertiesFile(thisJarFileName + File.separator + testExecutor.getPluginName() + ".properties", testExecutor.getDefaultSettings(), false);
            } catch (IOException e) {
                //logger.info(e.getMessage());
            }

            testExecutor.set(suite, test, threadSuspended, mapOfTestCalls, thisJarFileName, settings);
        }

        for (DataHandler dataHandler : dataHandlers) {
            ConcurrentHashMap<String, String> settings = new ConcurrentHashMap<>();

            try { // Прочитать настройки для плагина
                settings = test.getProgramSettings().readPropertiesFile(thisJarFileName + File.separator + dataHandler.getPluginName() + ".properties", dataHandler.getDefaultSettings(), false);
            } catch (IOException e) {
                //logger.info(e.getMessage());
            }

            dataHandler.set(thisJarFileName, suite, test, stringDataMap, byteDataMap, settings);
        }
    }

    /**
     * Обработать данные шага
     *
     * @param step Ссылка на обрабатываемый шаг
     */
    public void processStep(RSTests step) {
        for (DataHandler dataHandler : dataHandlers) {
            try {
                dataHandler.processing(step);
            } catch (Throwable t) {
                test.printStackTrace(t);
            }
        }
    }

    /**
     * Выполняет шаг и сохраняет результаты в шаг (ErrorMessage, Status, Attachments)
     *
     * @param step Шаг для выполнения
     */
    public void execute(RSTests step) {
        boolean stepPressInExecuter = false;

        // Найти исполнителя с соответствующим шагом и передать шаг на выполнение
        for (TestExecutor testExecutor : testExecutorList) {
            try {
                if (testExecutor.getAllStepsMap().get(step.getStep()) != null) {
                    stepPressInExecuter = true;
                    try {
                        testExecutor.execute(step);
                    } catch (Throwable t) {
                        if (t.getMessage() == null) {
                            try {
                                if (t.getCause() != null && t.getCause().getMessage() != null) {
                                    step.setErrorMessage(t.getCause().getMessage());
                                }
                            } catch (Throwable w) {
                            }

                        } else {
                            step.setErrorMessage(t.getMessage());
                        }
                        step.setStatus(StepStatus.BROKEN);
                    }
                    break;
                }
            } catch (Throwable t2) {
                test.printStackTrace(t2);
            }
        }
        if (!stepPressInExecuter) {
            step.setErrorMessage("Шаг не определён. " + step.getStep());
            step.setStatus(StepStatus.BROKEN);
        } else {
            // Делать снимок экрана если есть флаг ToCaptureTheScreenAfterTheFailureStep
            if (test.getProgramSettings().getBoolean("ToCaptureTheScreenAfterTheFailureStep")) {
                if (step.getStatus().equals(StepStatus.FAILURE) || step.getStatus().equals(StepStatus.BROKEN)) {
                    byte[] screen = getScreenshot();
                    if (screen != null && screen.length > 6125)
                        step.getAttachments().put(step.getStep() + "_error.png", getScreenshot());
                }
            }
        }
    }

    /**
     * Возвращает снимок экрана
     *
     * @return снимок в байтах
     */
    private byte[] getScreenshot() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(grabScreen(), "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
        }
        return null;
    }

    private BufferedImage grabScreen() {
        try {
            return new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        } catch (SecurityException e) {
        } catch (AWTException e) {
        }
        return null;
    }

    /**
     * Завершает тест. Закрывает все соединения открытые для теста
     */
    public void close() {

        // У всех исполнителей вызвать закрытие
        for (TestExecutor testExecutor : testExecutorList) {
            try {
                testExecutor.close();
            } catch (Throwable t) {
                test.printStackTrace(t);
            }
        }

        // У всех обработчиков данных вызвать закрытие
        for (DataHandler dataHandler : dataHandlers) {
            try {
                dataHandler.close();
            } catch (Throwable t) {
                test.printStackTrace(t);
            }
        }

        // Закрыть лог текущего теста
        Handler[] handler = logger.getHandlers();
        for (Handler h : handler) {
            h.close();
            logger.removeHandler(h);
        }
    }
}
