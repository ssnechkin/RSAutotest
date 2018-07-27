package plugins.executers.net.ftp;

import modules.testExecutor.enums.StepStatus;
import modules.testExecutor.interfaces.CalledFromTest;
import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.RSTests;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import plugins.interfaces.TestExecutor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class FTPExecutor implements TestExecutor {
    private SuiteDatas suite;
    private TestDatas test;
    private ConcurrentHashMap<String, String> stepsMap = new ConcurrentHashMap<>();// <Наименование шага, Описание шага>
    private ConcurrentHashMap<String, String> stringDataMap;
    private ConcurrentHashMap<String, byte[]> byteDataMap;
    private Logger logger;
    private Boolean threadSuspended = false;
    private ConcurrentHashMap<String, CalledFromTest> mapOfTestCalls;
    private String programFilesDirectory;
    private ConcurrentHashMap<String, String> settings;

    private FTPClient ftpClient = new FTPClient();
    private String ftpHost = "";
    private String ftpPort = "";
    private String ftpUser = "";
    private String ftpPass = "";
    private String ftpFile = "";
    private Integer waitingFTPMillisecond = 0;

    @Override
    public String getPluginName() {
        return "FTP";
    }

    @Override
    public String getGroupName() {
        return "Сеть";
    }

    @Override
    public ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getDefaultSettings() {
        ConcurrentHashMap<String, ConcurrentHashMap<String, String>> properties = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> settings = new ConcurrentHashMap<>();

        settings.put("WaitingFTPMillisecond", "60000");
        properties.put("Время (в миллисекундах) на попытки выполнить запрос.", settings);

        return properties;
    }

    @Override
    public ConcurrentHashMap<String, String> getAllStepsMap() {
        if (stepsMap.size() == 0) execute(null);
        return stepsMap;
    }

    @Override
    public void set(SuiteDatas suite, TestDatas test, Boolean threadSuspended, ConcurrentHashMap<String, CalledFromTest> mapOfTestCalls, String programFilesDirectory, ConcurrentHashMap<String, String> settings) {
        this.suite = suite;
        this.test = test;
        this.stringDataMap = test.getStringDataMap();
        this.byteDataMap = test.getByteDataMap();
        this.threadSuspended = threadSuspended;
        this.mapOfTestCalls = mapOfTestCalls;
        this.programFilesDirectory = programFilesDirectory;
        this.logger = test.getLogger();
        this.settings = new ConcurrentHashMap<>();
        this.settings.putAll(settings);
    }


    @Override
    public void close() {
        try {
            ftpClient.disconnect();
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
    }

    @Override
    public void execute(RSTests step) {
        String name, description, stepName = "";
        boolean mapEmpty = stepsMap.size() == 0;

        if (step != null) {
            stepName = step.getStep();
            if (step.getTimeoutMilliseconds() != null) {
                waitingFTPMillisecond = Integer.valueOf(step.getTimeoutMilliseconds());
            } else {
                waitingFTPMillisecond = Integer.valueOf(settings.get("WaitingFTPMillisecond"));
            }
        }

        name = "Установить FTP host";
        if (mapEmpty) {
            description = "Сохраняет в память FTP host. Пример: Шаг = Установить FTP host; Значение = host;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setFtpHost(step);
            return;
        }

        name = "Установить FTP порт";
        if (mapEmpty) {
            description = "Сохраняет в память FTP порт. Пример: Шаг = Установить FTP порт; Значение = номер порта;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setFtpPort(step);
            return;
        }

        name = "Установить FTP пользователя";
        if (mapEmpty) {
            description = "Сохраняет в память FTP пользователя. Пример: Шаг = Установить FTP пользователя; Значение = Пользователь;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setFtpUser(step);
            return;
        }

        name = "Установить FTP пароль";
        if (mapEmpty) {
            description = "Сохраняет в память FTP пароль. Пример: Шаг = Установить FTP пароль; Значение = пароль;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setFtpPassword(step);
            return;
        }

        name = "Установить имя файла на FTP";
        if (mapEmpty) {
            description = "Сохраняет в память имя файла на FTP. Пример: Шаг = Установить имя файла на FTP; Значение = Имя файла;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setFtpFileName(step);
            return;
        }

        name = "На FTP загрузить файл с текстом";
        if (mapEmpty) {
            description = "Загружает на FTP текстовый файл с указанным содержанием. Пример: Шаг = На FTP загрузить файл с текстом; Значение = текст;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            uploadFileOnFTPwithText(step);
            return;
        }

        name = "На FTP удалить файл";
        if (mapEmpty) {
            description = "Удаляет на FTP файл с указанным именем. Пример: Шаг = На FTP удалить файл; Значение = Имя файла;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            deleteFileOnFTP(step);
            return;
        }

        name = "Отключиться от FTP";
        if (mapEmpty) {
            description = "Выполняется отключение от FTP. Пример: Шаг = Отключиться от FTP;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            disconnectFromTheFTP(step);
            return;
        }
    }

    private void disconnectFromTheFTP(RSTests step) {
        try {
            ftpClient.disconnect();
        } catch (IOException e) {
            step.setErrorMessage(e.getMessage());
            step.setStatus(StepStatus.FAILURE);
        }
    }

    private void deleteFileOnFTP(RSTests step) {
        boolean error = true;

        Calendar timeoutCalendar = Calendar.getInstance();    // Текущее время в календарь-таймаут
        timeoutCalendar.add(Calendar.MILLISECOND, waitingFTPMillisecond);   // Прибавить timeout

        while (Calendar.getInstance().before(timeoutCalendar)) {/*Пока не достигли времени таймаута*/
            try {
                ftpClient.connect(ftpHost);
                ftpClient.login(ftpUser, ftpPass);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                if (ftpClient.deleteFile(step.getValue())) {
                    error = false;
                    break;
                }
            } catch (Exception e) {
            }
        }
        if (error) {
            step.setErrorMessage("Ошибка удаления файла '" + step.getValue() + "'." + " Host= " + ftpHost + " User= " + ftpUser + " Password= " + ftpPass);
            step.setStatus(StepStatus.FAILURE);
        }
    }

    private void uploadFileOnFTPwithText(RSTests step) {
        InputStream fInput;
        boolean error = true;

        Calendar timeoutCalendar = Calendar.getInstance();    // Текущее время в календарь-таймаут
        timeoutCalendar.add(Calendar.MILLISECOND, waitingFTPMillisecond);   // Прибавить timeout

        while (Calendar.getInstance().before(timeoutCalendar)) {/*Пока не достигли времени таймаута*/
            try {
                ftpClient.connect(ftpHost);
                ftpClient.login(ftpUser, ftpPass);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                fInput = new ByteArrayInputStream(step.getValue().getBytes("UTF-8"));
                if (ftpClient.storeFile(ftpFile, fInput)) {
                    error = false;
                    fInput.close();
                    break;
                } else {
                    fInput.close();
                }
            } catch (Exception e) {
            }
        }
        if (error) {
            step.setErrorMessage("Ошибка загрузки файла '" + ftpFile + "'." + " Host= " + ftpHost + " User= " + ftpUser + " Password= " + ftpPass);
            step.setStatus(StepStatus.FAILURE);
        }
    }

    public void setFtpHost(RSTests step) {
        this.ftpHost = step.getValue();
    }

    public void setFtpPort(RSTests step) {
        this.ftpPort = step.getValue();
    }

    public void setFtpUser(RSTests step) {
        this.ftpUser = step.getValue();
    }

    public void setFtpPassword(RSTests step) {
        this.ftpPass = step.getValue();
    }

    public void setFtpFileName(RSTests step) {
        this.ftpFile = step.getValue();
    }
}
