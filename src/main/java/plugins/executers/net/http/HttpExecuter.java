package plugins.executers.net.http;

import com.google.gson.Gson;
import modules.testExecutor.enums.StepStatus;
import modules.testExecutor.interfaces.CalledFromTest;
import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.RSTests;
import plugins.interfaces.TestExecutor;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class HttpExecuter implements TestExecutor {
    private String urlString = "";
    private byte[] contentPost;
    private Map<String, String> postHeadersMap = new HashMap<>();
    private Map responseHeadersMap = new HashMap();
    private String jsonHeaders = "";
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

    private Integer waitingMessageMillisecond = 0;

    @Override
    public String getPluginName() {
        return "HTTP";
    }

    @Override
    public String getGroupName() {
        return "Сеть";
    }

    @Override
    public ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getDefaultSettings() {
        ConcurrentHashMap<String, ConcurrentHashMap<String, String>> properties = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> settings = new ConcurrentHashMap<>();

        settings.put("WaitingMessageMillisecond", "60000");
        properties.put("Время (в миллисекундах) на попытки получение сообщения", settings);

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

    }

    private byte[] downloadUrl(String urlString) {
        Calendar timeoutCalendar = Calendar.getInstance();    // Текущее время в календарь-таймаут
        timeoutCalendar.add(Calendar.MILLISECOND, waitingMessageMillisecond);   // Прибавить timeout

        boolean result = false;
        while (!result && (Calendar.getInstance().before(timeoutCalendar)/*Пока не достигли времени таймаута*/)) {

            try {
                URL url = new URL(urlString);
                URLConnection connection = url.openConnection();
                getResponseHeaders(connection);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                try {
                    byte[] chunk = new byte[4096];
                    int bytesRead;
                    InputStream stream = connection.getInputStream();//url.openStream();

                    while ((bytesRead = stream.read(chunk)) > 0) {
                        outputStream.write(chunk, 0, bytesRead);
                    }
                } catch (IOException e) {
                    //log.info(e.getMessage());
                    return null;
                }

                if (outputStream.toByteArray().length <= 22) {
                    return null;
                } else {
                    result = true;
                    return outputStream.toByteArray();
                }

            } catch (Exception e) {
            }
        }
        return null;
    }

    private void getResponseHeaders(URLConnection connection) throws IOException {
        responseHeadersMap = connection.getHeaderFields();
        jsonHeaders = new Gson().toJson(responseHeadersMap);
    }

    private byte[] sendFilePost(String requestType, String urlString, byte[] contentFile) {
        BufferedReader reader = null;

        Calendar timeoutCalendar = Calendar.getInstance();    // Текущее время в календарь-таймаут
        timeoutCalendar.add(Calendar.MILLISECOND, waitingMessageMillisecond);   // Прибавить timeout
        boolean result = false;
        while (!result && (Calendar.getInstance().before(timeoutCalendar)/*Пока не достигли времени таймаута*/)) {
            reader = null;
            try {
                /*URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                getResponseHeaders(connection);
                */
                URL url = new URL(urlString);
                //getResponseHeaders(url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                //заголовки (пока не использую)
              /*
               * //conn.setRequestProperty("Charsert", "UTF-8");
		  //conn.setRequestProperty("content-type","text/html");
		  //conn.setRequestProperty("Content-type", "application/x-java-serialized-
               *
               */
                //connection.setRequestProperty("Accept-Encoding","gzip,deflate");
                //connection.setRequestProperty("Content-Type","text/xml;charset=UTF-8");
                //connection.setRequestProperty("SOAPAction","EpguPrivatePerson");


                for (Map.Entry<String, String> stringStringEntry : postHeadersMap.entrySet()) {
                    connection.setRequestProperty(stringStringEntry.getKey(), stringStringEntry.getValue());
                }

                connection.setRequestMethod(requestType);
                connection.setReadTimeout(10000);
                connection.setDoOutput(true);
                OutputStream output = connection.getOutputStream();
                if (contentFile != null) output.write(contentFile);
                connection.connect();

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try {
                    byte[] chunk = new byte[4096];
                    int bytesRead;

                    InputStream stream = connection.getInputStream();
                    while ((bytesRead = stream.read(chunk)) > 0) {
                        outputStream.write(chunk, 0, bytesRead);
                    }
                } catch (IOException e) {
                    //log.info(e.getMessage());
                }

                if (outputStream.toByteArray().length <= 22) {
                    return null;
                } else {
                    result = true;
                    return outputStream.toByteArray();
                }

            } catch (ProtocolException e) {
                //LoggerInstance.getInstance().printStackTrace(e);
            } catch (MalformedURLException e) {
                //LoggerInstance.getInstance().printStackTrace(e);
            } catch (IOException e) {
                //LoggerInstance.getInstance().printStackTrace(e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        //LoggerInstance.getInstance().printStackTrace(e);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void execute(RSTests step) {
        String name, description, stepName = "";
        boolean mapEmpty = stepsMap.size() == 0;

        if (step != null) {
            stepName = step.getStep();
            if (step.getTimeoutMilliseconds() != null) {
                waitingMessageMillisecond = Integer.valueOf(step.getTimeoutMilliseconds());
            } else {
                waitingMessageMillisecond = Integer.valueOf(settings.get("WaitingMessageMillisecond"));
            }
        }

        name = "Заголовки ответа выполненного ранее запроса сохранить в переменную";
        if (mapEmpty) {
            description = "Выполняется сохранение заголовков (в json формате) в указанную переменную. Пример: Шаг = Заголовки ответа выполненного ранее запроса сохранить в переменную; Ключ = Имя переменной;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            getResponseHeadersToValue(step);
            return;
        }

        name = "По URL скачать файл в переменную";
        if (mapEmpty) {
            description = "Выполняется переход по указанной ссылке и сохранение полученного файла в указанную переменную. Пример: Шаг = По URL скачать файл в переменную; Ключ = Имя переменной; Значение = Ссылка Пример: http://site/dounload;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            urlToDownloadFile(step);
            return;
        }

        name = "Для POST-запроса установить адрес URL";
        if (mapEmpty) {
            description = "Выполняется сохранение в память url-ссылка. Пример: Шаг = Для POST-запроса установить адрес URL; Значение = Ссылка Пример: http://site/dounload;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setPostURL(step);
            return;
        }

        name = "Для POST-запроса установить отправляемый контент";
        if (mapEmpty) {
            description = "Выполняется сохранение в память url-ссылка. Пример: Шаг = Для POST-запроса установить отправляемый контент; Значение = Текст или имя переменной содержащая текст или файл;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setPostContent(step);
            return;
        }

        name = "Для POST-запроса добавить заголовок";
        if (mapEmpty) {
            description = "Выполняется сохранение в память строки заголовка для будущего запроса. Пример: Шаг = Для POST-запроса добавить заголовок; Ключ = Имя параметра; Значение = Значение параметра;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setPostHeader(step);
            return;
        }

        name = "Для POST-запроса очистить заголовоки";
        if (mapEmpty) {
            description = "Выполняется удаление из памяти сохранённых заголовков. Пример: Шаг = Для POST-запроса очистить заголовоки;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            cleanPostHeader(step);
            return;
        }

        name = "Отправить POST-запрос и сохранить результат в переменную";
        if (mapEmpty) {
            description = "Выполняется отправка запроса по ранее заданным параметрам. Результат сохраняется в указанную переменную. Пример: Шаг = Отправить POST-запрос и сохранить результат в переменную; Ключ = Имя переменной;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            sendPostRequest(step);
            return;
        }
    }

    private void sendPostRequest(RSTests step) {
        byte[] result = sendFilePost("POST", urlString, contentPost);

        if (result == null) {
            step.setErrorMessage("Не удалось отправить запрос");
            step.setStatus(StepStatus.FAILURE);
        } else {
            step.getAttachments().put(step.getKey(), result);
            byteDataMap.put(step.getKey(), result);
        }
    }

    private void cleanPostHeader(RSTests step) {
        postHeadersMap.clear();
    }

    private void urlToDownloadFile(RSTests step) {
        byte[] file = downloadUrl(step.getValue());

        if (file == null) {
            step.setErrorMessage("Не удалось скачать файл");
            step.setStatus(StepStatus.FAILURE);
        } else {
            byteDataMap.put(step.getKey(), file);
            step.getAttachments().put(step.getKey(), file);
        }
    }

    private void getResponseHeadersToValue(RSTests step) {
        stringDataMap.put(step.getKey(), jsonHeaders);
    }

    public void setPostURL(RSTests step) {
        urlString = step.getValue();
    }

    public void setPostContent(RSTests step) {
        String value = step.getValue();
        byte[] bt = byteDataMap.get(step.getValue());

        if (stringDataMap.get(value) != null) value = stringDataMap.get(value);

        if (bt == null) {
            bt = value.getBytes(StandardCharsets.UTF_8);
        }
        if (bt == null) {
            step.setErrorMessage("Отсутсвует контент " + step.getValue());
            step.setStatus(StepStatus.FAILURE);
        }
        contentPost = bt;
    }

    public void setPostHeader(RSTests step) {
        postHeadersMap.put(step.getKey(), step.getValue());
    }
}
