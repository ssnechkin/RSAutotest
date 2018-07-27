package plugins.executers.ibm.mq;

import modules.testExecutor.enums.StepStatus;
import modules.testExecutor.interfaces.CalledFromTest;
import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.RSTests;
import plugins.executers.ibm.mq.client.MqClient;
import plugins.executers.ibm.mq.client.MqConfig;
import plugins.executers.ibm.mq.xml.conditions.TextCondition;
import plugins.executers.ibm.mq.xml.conditions.XmlXpathTextCondition;
import plugins.interfaces.TestExecutor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class IBMmqExecuter implements TestExecutor {
    private SuiteDatas suite;
    private TestDatas test;
    private ConcurrentHashMap<String, String> stepsMap = new ConcurrentHashMap<>();// <Наименование шага, Описание шага>
    private ConcurrentHashMap<String, String> stringDataMap;
    private ConcurrentHashMap<String, byte[]> byteDataMap;
    private Logger logger;
    private Boolean threadSuspended = false;
    private ConcurrentHashMap<String, CalledFromTest> mapOfTestCalls;
    private String programFilesDirectory;
    private Integer waitingMessageIBMmqMillisecond = 0;
    private Integer delayBeforeRequest = 100;

    private MqConfig mqConfig;
    private int ibmMqPort;
    private String ibmMqHost,
            ibmMqChannelName,
            ibmMqManager,
            ibmMqUsername,
            ibmMqPassword,
            ibmMqQueueName,
            ibmMqMessageId;
    private List<TextCondition> xmlConditions = new LinkedList<>();
    private ConcurrentHashMap<String, String> settings;

    @Override
    public String getPluginName() {
        return "IBM_MQ";
    }

    @Override
    public String getGroupName() {
        return "IBM";
    }

    @Override
    public ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getDefaultSettings() {
        ConcurrentHashMap<String, ConcurrentHashMap<String, String>> properties = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> settings = new ConcurrentHashMap<>();

        settings = new ConcurrentHashMap<>();
        settings.put("WaitingMessageIBMmqMillisecond", "180000");
        properties.put("Время (в миллисекундах) на попытки получение сообщения из очереди IBM MQ", settings);

        settings = new ConcurrentHashMap<>();
        settings.put("DelayBeforeRequestMillisecond", "100");
        properties.put("Время (в миллисекундах) задержки между запросами", settings);

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

        waitingMessageIBMmqMillisecond = Integer.valueOf(settings.get("WaitingMessageIBMmqMillisecond"));
        delayBeforeRequest = Integer.valueOf(settings.get("DelayBeforeRequestMillisecond"));
    }

    @Override
    public void close() {

    }

    private void deleteMessageIBMmqToXMLConditions(List<TextCondition> xmlConditions) throws Exception {
        mqConfig = new MqConfig(ibmMqHost, ibmMqPort, ibmMqChannelName, ibmMqManager, ibmMqUsername, ibmMqPassword, ibmMqQueueName);
        MqClient mqClient = new MqClient(mqConfig);
        mqClient.getMessageBody(xmlConditions, delayBeforeRequest);
        mqClient.close();
    }

    private String sendMessageIBMmq(byte[] byteFile) throws Exception {
        mqConfig = new MqConfig(ibmMqHost, ibmMqPort, ibmMqChannelName, ibmMqManager, ibmMqUsername, ibmMqPassword, ibmMqQueueName);
        MqClient mqClient = new MqClient(mqConfig);
        ibmMqMessageId = mqClient.sendQueueMessage(byteFile);
        mqClient.close();
        return ibmMqMessageId;
    }

    private byte[] getIBMmqMessageXML(List<TextCondition> conditions) {
        mqConfig = new MqConfig(ibmMqHost, ibmMqPort, ibmMqChannelName, ibmMqManager, ibmMqUsername, ibmMqPassword, ibmMqQueueName);
        MqClient mqClient = new MqClient(mqConfig);
        Calendar timeoutCalendar = Calendar.getInstance(); // Текущее время в календарь-таймаут
        timeoutCalendar.add(Calendar.MILLISECOND, waitingMessageIBMmqMillisecond); // Прибавить timeout
        byte[] result;
        do {
            result = mqClient.getMessageBody(conditions, delayBeforeRequest);
            if (result != null) break;
        } while (Calendar.getInstance().before(timeoutCalendar));    // Пока не достигли времени таймаута
        mqClient.close();
        return result;
    }

    private ByteArrayOutputStream createZipByte(ArrayList<ByteArrayInputStream> fileList) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zout = new ZipOutputStream(bos);
        //zout.setEncoding("CP866");
        int x = 0;
        for (ByteArrayInputStream file : fileList) {
            x++;
            addFile(zout, file, x + File.separator + x + ".txt");
        }
        zout.close();
        return bos;
    }

    private void addFile(ZipOutputStream zout, ByteArrayInputStream fis, String fileName) throws Exception {
        zout.putNextEntry(new ZipEntry(fileName));

        byte[] buffer = new byte[4048];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            zout.write(buffer, 0, length);
        }
        zout.closeEntry();
        fis.close();
    }

    @Override
    public void execute(RSTests step) {
        String name, description, stepName = "";
        boolean mapEmpty = stepsMap.size() == 0;

        if (step != null) {
            stepName = step.getStep();
            if (step.getTimeoutMilliseconds() != null) {
                waitingMessageIBMmqMillisecond = Integer.valueOf(step.getTimeoutMilliseconds());
            } else {
                waitingMessageIBMmqMillisecond = Integer.valueOf(settings.get("WaitingMessageIBMmqMillisecond"));
            }
        }

        name = "Установить IBM MQ Порт";
        if (mapEmpty) {
            description = "Устанавливает порт для подключения к IBM MQ. Пример: Шаг = Установить IBM MQ Порт; Значение = Номер порта";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setIbmMqPort(step);
            return;
        }

        name = "Установить IBM MQ Хост";
        if (mapEmpty) {
            description = "Устанавливает хост для подключения к IBM MQ. Пример: Шаг = Установить IBM MQ Хост; Значение = Хост;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setIbmMqHost(step);
            return;
        }

        name = "Установить IBM MQ Имя канала";
        if (mapEmpty) {
            description = "Устанавливает Имя канала для подключения к IBM MQ. Пример: Шаг = Установить IBM MQ Имя канала; Значение = Имя канала;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setIbmMqChannelName(step);
            return;
        }

        name = "Установить IBM MQ Менеджера очередей";
        if (mapEmpty) {
            description = "Устанавливает Менеджера очередей для подключения к IBM MQ. Пример: Шаг = Установить IBM MQ Менеджера очередей; Значение = Менеджер очередей;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setIbmMqManager(step);
            return;
        }

        name = "Установить IBM MQ Пользователя";
        if (mapEmpty) {
            description = "Устанавливает Пользователя для подключения к IBM MQ. Пример: Шаг = Установить IBM MQ Пользователя; Значение = Пользователь;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setIbmMqUsername(step);
            return;
        }

        name = "Установить IBM MQ Пароль";
        if (mapEmpty) {
            description = "Устанавливает Пароль для подключения к IBM MQ. Пример: Шаг = Установить IBM MQ Пароль; Значение = Пароль;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setIbmMqPassword(step);
            return;
        }

        name = "Установить IBM MQ Имя очереди";
        if (mapEmpty) {
            description = "Устанавливает Имя очереди для подключения к IBM MQ. Пример: Шаг = Установить IBM MQ Имя очереди; Значение = Имя очереди;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setIbmMqQueueName(step);
            return;
        }

        name = "В IBM MQ с текущей конфигурацией очистить очередь";
        if (mapEmpty) {
            description = "Выполянет очистку очереди в IBM MQ. Пример: Шаг = В IBM MQ с текущей конфигурацией очистить очередь;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            cleanIBMmqAllMessages(step);
            return;
        }

        name = "В IBM MQ с текущей конфигурацией отправить файл";
        if (mapEmpty) {
            description = "Выполянет отправку файла в IBM MQ. Пример: Шаг = В IBM MQ с текущей конфигурацией отправить файл; Значение = Имя переменной содержащая файл;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            sendMessageIBMmqThisConfig(step);
            return;
        }

        name = "Сохранить идентификатор отправленного сообщения IBM MQ в переменную";
        if (mapEmpty) {
            description = "Сохраняет идентификатор отправленного сообщения IBM MQ в указанную переменную. Пример: Шаг = Сохранить идентификатор отправленного сообщения IBM MQ в переменную; Ключ = Имя переменной;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            saveIBMMQMessageId(step);
            return;
        }

        name = "В IBM MQ отправить файл с конфигурацией";
        if (mapEmpty) {
            description = "В IBM MQ отправить файл с конфигурацией Хост=192.168.0.1 Порт=8080 Пользователь=User Пароль=Password Канал=ChanalName Очередь=QueueName Менеджер=QueueMqManager. Пример: Шаг = В IBM MQ отправить файл с конфигурацией; Ключ = Строка с перечислением переметров; Значение = Имя переменной содержащая файл;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            sendMessageIBMmqConfigInField(step);
            return;
        }

        name = "В IBM MQ найти XML файлы и удалить их из очереди";
        if (mapEmpty) {
            description = "С заданными ранее параметрами поиска найти и удалить файлы из очереди. Пример: Шаг = В IBM MQ найти XML файлы и удалить их из очереди;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            delteIBMmqMessageXMLXPath(step);
            return;
        }

        name = "Для поиска XML файла в IBM MQ добавить искомое значение по XPath селектору";
        if (mapEmpty) {
            description = "Сохраняется в память значение для поиска по указанному селектору. Пример: Шаг = Для поиска XML файла в IBM MQ добавить искомое значение по XPath селектору; Ключ = Селектор; Значение = Искомое значение;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setIBMmqMessageXMLXPath(step);
            return;
        }

        name = "Для поиска XML файла в IBM MQ удалить все XPath селекторы";
        if (mapEmpty) {
            description = "Очищает из памяти сохранённые ранее селекторы. Пример: Шаг = Для поиска XML файла в IBM MQ удалить все XPath селекторы;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            cleanIBMmqXMLXPathSelectors(step);
            return;
        }

        name = "В IBM MQ найти XML файл и сохранить его в переменную";
        if (mapEmpty) {
            description = "Выполняется поиск файла по заданным ранее параметрам. Результат сохраняется в указанную переменную. Пример: Шаг = В IBM MQ найти XML файл и сохранить его в переменную; Ключ = Имя переменной для файла;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            getIBMmqMessageXMLNotValue(step);
            return;
        }

        name = "В IBM MQ найти XML файлы и сохранить в ZIP переменную";
        if (mapEmpty) {
            description = "Выполняется поиск файла по заданным ранее параметрам. Результат сохраняется в указанную переменную. Пример: Шаг = В IBM MQ найти XML файлы и сохранить в ZIP переменную; Ключ = Имя переменной для zip-файла;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            getIBMmqMessagesXMLNotValue(step);
            return;
        }

        name = "Изменить конфигурационный параметр";
        if (mapEmpty) {
            description = "Изменит значение указанного параметра для IBMmq. Пример: Шаг = Изменить конфигурационный параметр; Ключ = Имя параметра или имя переменной содержащая наименование параметра; Значение = Новое значение или имя переменной содержащая значение параметра;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            newValueToParameter(step);
            return;
        }

    }

    private void newValueToParameter(RSTests step) {
        if ((byteDataMap.get(step.getValue()) == null && stringDataMap.get(step.getValue()) == null) || step.getValue() == null) {
            step.setErrorMessage("Значение переменной не существует");
            step.setStatus(StepStatus.FAILURE);
        } else {
            String key = stringDataMap.get(step.getKey()) != null ? stringDataMap.get(step.getKey()) : step.getKey();
            String value = stringDataMap.get(step.getValue()) != null ? stringDataMap.get(step.getValue()) : step.getValue();
            settings.put(key, value);
        }
    }

    private void getIBMmqMessagesXMLNotValue(RSTests step) {
        ArrayList<ByteArrayInputStream> fileListArray = new ArrayList<>();
        byte[] byteFile;
        int x = 0;
        while (true) {
            byteFile = null;
            try {
                byteFile = getIBMmqMessageXML(xmlConditions);
            } catch (Exception e) {
            }

            if (byteFile == null) {
                break;
            } else {
                if (byteFile.length > 22) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(byteFile);
                    fileListArray.add(bis);

                    step.getAttachments().put("file_" + x++ + ".txt", byteFile);
                }
            }
        }
        if (fileListArray.size() > 0) {
            byte[] zipArchive = new byte[0];
            try {
                zipArchive = createZipByte(fileListArray).toByteArray();
                byteDataMap.put(step.getKey() + ".zip", zipArchive);
                step.getAttachments().put(step.getKey() + ".zip", zipArchive);
            } catch (Exception e) {
                step.setErrorMessage(e.getMessage());
                step.setStatus(StepStatus.BROKEN);
            }

        } else {
            step.setErrorMessage("Не удалось скачать ниодного файла");
            step.setStatus(StepStatus.FAILURE);
        }
    }

    private void getIBMmqMessageXMLNotValue(RSTests step) {
        byte[] byteFile = getIBMmqMessageXML(xmlConditions);
        if (byteFile == null) {
            step.setErrorMessage("Файл не найден");
            step.setStatus(StepStatus.FAILURE);
        } else {
            byteDataMap.put(step.getKey(), byteFile);
            step.getAttachments().put(step.getKey(), byteFile);
        }
    }

    private void cleanIBMmqXMLXPathSelectors(RSTests step) {
        xmlConditions.clear();
    }

    private void delteIBMmqMessageXMLXPath(RSTests step) {
        try {
            deleteMessageIBMmqToXMLConditions(xmlConditions);
        } catch (Exception e) {
            step.setErrorMessage(e.getMessage());
            step.setStatus(StepStatus.BROKEN);
        }
    }

    private void sendMessageIBMmqConfigInField(RSTests step) {
        byte[] byteFile = byteDataMap.get(step.getValue());

        if (byteFile == null) {
            step.setErrorMessage("Отсутсвует файл " + step.getValue());
            step.setStatus(StepStatus.FAILURE);
        }

        String[] configArray = step.getKey().split(" ");

        for (String argument : configArray) {
            switch (argument.split("=")[0]) {
                case "Хост":
                    ibmMqHost = argument.split("=")[1];
                    break;
                case "Порт":
                    ibmMqPort = Integer.parseInt(argument.split("=")[1]);
                    break;
                case "Пользователь":
                    ibmMqUsername = argument.split("=")[1];
                    break;
                case "Пароль":
                    ibmMqPassword = argument.split("=")[1];
                    break;
                case "Канал":
                    ibmMqChannelName = argument.split("=")[1];
                    break;
                case "Очередь":
                    ibmMqQueueName = argument.split("=")[1];
                    break;
                case "Менеджер":
                    ibmMqManager = argument.split("=")[1];
                    break;
            }
        }
        try {
            step.getAttachments().put("Идентификатор отправленного сообщения в IBM MQ", new String("" + sendMessageIBMmq(byteFile)).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            step.setErrorMessage(e.getMessage());
            step.setStatus(StepStatus.BROKEN);
        }
    }

    private void saveIBMMQMessageId(RSTests step) {
        stringDataMap.put(step.getKey(), ibmMqMessageId);
    }

    private void sendMessageIBMmqThisConfig(RSTests step) {
        byte[] byteFile = byteDataMap.get(step.getValue());
        if (byteFile == null) {
            step.setErrorMessage("Отсутсвует файл " + step.getValue());
            step.setStatus(StepStatus.FAILURE);
        }
        try {
            step.getAttachments().put("Идентификатор отправленного сообщения в IBM MQ", new String("" + sendMessageIBMmq(byteFile)).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            step.setErrorMessage(e.getMessage());
            step.setStatus(StepStatus.BROKEN);
        }
    }

    private void cleanIBMmqAllMessages(RSTests step) {
        mqConfig = new MqConfig(ibmMqHost, ibmMqPort, ibmMqChannelName, ibmMqManager, ibmMqUsername, ibmMqPassword, ibmMqQueueName);
        MqClient mqClient = new MqClient(mqConfig);
        logger.info("Количество удалённых сообщений: " + mqClient.deleteAllMessage(0));
        step.getAttachments().put("Количество удалённых сообщений", new String("" + mqClient.deleteAllMessage(0)).getBytes(StandardCharsets.UTF_8));
    }

    public void setIbmMqPort(RSTests step) {
        this.ibmMqPort = Integer.valueOf(step.getValue());
    }

    public void setIbmMqHost(RSTests step) {
        this.ibmMqHost = step.getValue();
    }

    public void setIbmMqChannelName(RSTests step) {
        this.ibmMqChannelName = step.getValue();
    }

    public void setIbmMqManager(RSTests step) {
        this.ibmMqManager = step.getValue();
    }

    public void setIbmMqUsername(RSTests step) {
        this.ibmMqUsername = step.getValue();
    }

    public void setIbmMqPassword(RSTests step) {
        this.ibmMqPassword = step.getValue();
    }

    public void setIbmMqQueueName(RSTests step) {
        this.ibmMqQueueName = step.getValue();
    }

    public void setIBMmqMessageXMLXPath(RSTests step) {
        xmlConditions.add(new XmlXpathTextCondition(step.getKey(), step.getValue()));
    }
}
