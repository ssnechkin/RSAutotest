package plugins.executers.sql;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import modules.testExecutor.enums.StepStatus;
import modules.testExecutor.interfaces.CalledFromTest;
import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.RSTests;
import plugins.executers.sql.templates.CollectionsSql;
import plugins.executers.sql.templates.ColumnsSql;
import plugins.interfaces.TestExecutor;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

public class SQLExecuter implements TestExecutor {
    PreparedStatement statement = null;
    private SuiteDatas suite;
    private TestDatas test;
    private ConcurrentHashMap<String, String> stepsMap = new ConcurrentHashMap<>();// <Наименование шага, Описание шага>
    private ConcurrentHashMap<String, String> stringDataMap;
    private ConcurrentHashMap<String, byte[]> byteDataMap;
    private Logger logger;
    private Boolean threadSuspended = false;
    private ConcurrentHashMap<String, CalledFromTest> mapOfTestCalls;
    private String programFilesDirectory;
    private String driverName;
    private String host;
    private String port;
    private String dbName;
    private String user;
    private String password;
    private Map<String, Object> parameters = new HashMap<>();
    private Connection connection;
    private Map<String, List<Integer>> indexMap = new HashMap<>();

    @Override
    public String getPluginName() {
        return "SQL";
    }

    @Override
    public String getGroupName() {
        return "База данных";
    }

    @Override
    public ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getDefaultSettings() {
        return null;
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
    }

    @Override
    public void close() {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                logger.info(e.getMessage());
            }
            statement = null;
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.info(e.getMessage());
            }
            connection = null;
        }
    }

    private boolean connectDB() {
        String message = "Отсутсвует значение:";
        if (driverName == null) message += " Имя драйвера;";
        if (host == null) message += " Хост;";
        if (port == null) message += " Порт;";
        if (dbName == null) message += " Имя базы данных;";
        if (user == null) message += " Имя пользователя;";
        if (password == null) message += " Пароль пользователя;";
        if (message.length() > 22) logger.info(message);

       /* log.info(" Имя драйвера = " + driverName);
        log.info(" Хост = " + host);
        log.info(" Порт = " + port);
        log.info(" Имя базы данных = " + dbName);
        log.info(" Имя пользователя = " + user);
        log.info(" Пароль пользователя = " + password);*/

        Map<String, String> driverNamesMap = new HashMap<>();
        driverNamesMap.put("com.mysql.cj.jdbc.Driver", "jdbc:mysql://" + host + ":" + port + "/" + dbName);
        driverNamesMap.put("org.postgresql.Driver", "jdbc:postgresql://" + host + ":" + port + "/" + dbName);
        driverNamesMap.put("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName);
        driverNamesMap.put("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://" + host + ":" + port + "/" + dbName);
        connection = null;
        for (Map.Entry<String, String> driver : driverNamesMap.entrySet()) {
            if (driver.getKey().toUpperCase().contains(driverName.toUpperCase())) {
                try {
                    Class.forName(driver.getKey());
                    connection = DriverManager.getConnection(driver.getValue(), user, password);
                    return true;
                } catch (Exception e) {
                    logger.info(e.getMessage());
                }
            }
        }
        return false;
    }

    /**
     * <li>Парсит строки запроса с параметрами вида :PARAM_NAME
     * <li>Очищает карту индексов параметров (indexMap) в классе
     * <li>Создает новую карту индексов параметров (indexMap) в классе
     * <li>Возвращает новую строку где :PARAM_NAME заменены на знаки ? - совместимые с JDBC параметрами
     *
     * @param sqlString Строка запроса
     * @return Новая строка запроса
     */
    private final String parse(String sqlString) {
        indexMap.clear();
        int length = sqlString.length();
        StringBuffer parsedQuery = new StringBuffer(length);
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int index = 1;

        for (int i = 0; i < length; i++) {
            char c = sqlString.charAt(i);
            if (inSingleQuote) {
                if (c == '\'') {
                    inSingleQuote = false;
                }
            } else if (inDoubleQuote) {
                if (c == '"') {
                    inDoubleQuote = false;
                }
            } else {
                if (c == '\'') {
                    inSingleQuote = true;
                } else if (c == '"') {
                    inDoubleQuote = true;
                } else if (c == ':' && i + 1 < length &&
                        Character.isJavaIdentifierStart(sqlString.charAt(i + 1))) {
                    int j = i + 2;
                    while (j < length && Character.isJavaIdentifierPart(sqlString.charAt(j))) {
                        j++;
                    }
                    String name = sqlString.substring(i + 1, j);
                    c = '?';
                    i += name.length();

                    List<Integer> indexList = indexMap.get(name);
                    if (indexList == null) {
                        indexList = new LinkedList<>();
                        indexMap.put(name, indexList);
                    }
                    indexList.add(new Integer(index));

                    index++;
                }
            }
            parsedQuery.append(c);
        }
        return parsedQuery.toString();
    }

    /**
     * Вернуть список индексов (порядковый номер местоположения в запросе) для указанного имени параметра.
     *
     * @param paramName Имя параметра
     * @return parameter Список индексов
     */
    private List<Integer> getIndexes(String paramName) {
        List<Integer> indexes = new LinkedList<>();
        if (indexMap != null && indexMap.size() > 0) {
            indexes = indexMap.get(paramName);
        }
        return indexes;
    }

    private CollectionsSql getCollectionSqlFromResultSet(ResultSet resultSet) throws Exception {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        CollectionsSql collectionsSql = null;
        CopyOnWriteArrayList<String> labels = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<ColumnsSql> collect = new CopyOnWriteArrayList<>();

        while (resultSet.next()) {
            CopyOnWriteArrayList<String> col = new CopyOnWriteArrayList<>();
            for (int i = 0; i < columnCount; i++) {
                try {
                    col.add(resultSet.getString(i + 1));
                } catch (Throwable t) {
                    //logger.info(t.getMessage());
                }
            }
            try {
                ColumnsSql columnsSql = new ColumnsSql(col);
                collect.add(columnsSql);
            } catch (Throwable t) {
                //logger.info(t.getMessage());
            }
        }

        try {
            collectionsSql = new CollectionsSql(collect);
        } catch (Throwable t) {
            //logger.info(t.getMessage());
        }

        for (int i = 0; i < columnCount; i++) {
            try {
                if (resultSetMetaData.getColumnLabel(i + 1) != null) {
                    labels.add(resultSetMetaData.getColumnLabel(i + 1));
                } else {
                    labels.add(resultSetMetaData.getColumnName(i + 1));
                }
            } catch (Throwable t) {
                //logger.info(t.getMessage());
            }
        }

        try {
            if (collectionsSql != null)
                collectionsSql.setColums(labels);
        } catch (Throwable t) {
            //logger.info(t.getMessage());
        }

        return collectionsSql;
    }

    /**
     * Вернуть список индексов (порядковый номер местоположения в запросе) для указанного имени параметра.
     *
     * @param resultSet результат выполнения statement.executeQuery()
     * @return parameter JSON сформированный из таблицы запроса
     */
    private String getJsonFromResultSet(ResultSet resultSet) throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String resultJson = "";
        try {
            resultJson = gson.toJson(getCollectionSqlFromResultSet(resultSet));
        } catch (Throwable t) {
        }

        if (resultJson != null && resultJson.length() > 2) {
            return resultJson;
        }
        return "";
    }

    @Override
    public void execute(RSTests step) {
        String name, description, stepName = "";
        boolean mapEmpty = stepsMap.size() == 0;

        if (step != null) stepName = step.getStep();

        name = "Задать Data Base Driver Name";
        if (mapEmpty) {
            description = "Сохранить в память Data Base Driver Name. Пример: Шаг = Задать Data Base Driver Name; Значение = Имя драйвера";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setDriverName(step);
            return;
        }

        name = "Задать Data Base host";
        if (mapEmpty) {
            description = "Сохранить в память Data Base host. Пример: Шаг = Задать Data Base Driver host; Значение = Хост";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setHostDB(step);
            return;
        }

        name = "Задать Data Base port";
        if (mapEmpty) {
            description = "Сохранить в память Data Base port. Пример: Шаг = Задать Data Base port; Значение = Номер порта";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setPortDB(step);
            return;
        }

        name = "Задать Data Base name";
        if (mapEmpty) {
            description = "Сохранить в память Data Base name. Пример: Шаг = Задать Data Base name; Значение = Имя базы данных";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setNameDB(step);
            return;
        }

        name = "Задать Data Base user";
        if (mapEmpty) {
            description = "Сохранить в память Data Base user. Пример: Шаг = Задать Data Base user; Значение = Имя пользователя";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setUserDB(step);
            return;
        }

        name = "Задать Data Base password";
        if (mapEmpty) {
            description = "Сохранить в память Data Base password. Пример: Шаг = Задать Data Base password; Значение = пароль";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setPasswordDB(step);
            return;
        }

        name = "Добавить Data Base параметр";
        if (mapEmpty) {
            description = "Сохранить в память Data Base параметр. Пример: Шаг = Добавить Data Base параметр; Значение = параметр";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            addDBParameter(step);
            return;
        }

        name = "Сохранить результат Data Base запроса в";
        if (mapEmpty) {
            description = "Сохранить в указанную переменную результат выполнения запроса в json-формате. Пример: Шаг = Сохранить результат Data Base запроса в; Ключ = Имя переменной; Значение = Переменная содержащая запрос. Пример: SELECT name FROM DUAL;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            saveRequestDBResult(step);
            return;
        }

        name = "Закрыть Data Base соединение";
        if (mapEmpty) {
            description = "Закрывает соединение. Пример: Шаг = Закрыть Data Base соединение;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            closeDB(step);
            return;
        }
    }

    private void closeDB(RSTests step) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                step.setErrorMessage(e.getMessage());
                step.setStatus(StepStatus.FAILURE);
            }
            statement = null;
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                step.setErrorMessage(e.getMessage());
                step.setStatus(StepStatus.FAILURE);
            }
            connection = null;
        }
    }

    private void saveRequestDBResult(RSTests step) {
        String sql = stringDataMap.get(step.getValue());
        if (sql != null) {

            if (!connectDB()) {
                step.setErrorMessage("Неудалось выполнить подключение к базе данных");
                step.setStatus(StepStatus.FAILURE);

            } else {

                try {
                    statement = connection.prepareStatement(parse(sql));
                    if (parameters != null && parameters.size() > 0) {
                        for (Map.Entry<String, Object> param : parameters.entrySet()) {
                            List<Integer> indexes = getIndexes(param.getKey());
                            for (int index : indexes) {
                                try {
                                    statement.setObject(index, param.getValue());
                                } catch (Exception e) {
                                    logger.info(e.getMessage());
                                }
                            }
                        }
                    }

                } catch (SQLException e) {
                    step.setErrorMessage("Неудалось выполнить предвыполнение запроса. " + e.getMessage());
                    step.setStatus(StepStatus.FAILURE);
                }

                ResultSet resultSet = null;

                try {
                    resultSet = statement.executeQuery();
                } catch (Exception e) {
                    step.setErrorMessage("Неудалось выполнить запрос. " + e.getMessage());
                    step.setStatus(StepStatus.FAILURE);
                }

                if (resultSet != null) {
                    try {
                        stringDataMap.put(step.getKey(), getJsonFromResultSet(resultSet));
                    } catch (Throwable t) {
                        test.printStackTrace(t);
                        step.setErrorMessage("Неудалось сформировать json результат. " + t.getMessage());
                        step.setStatus(StepStatus.FAILURE);
                    }
                }
            }
        } else {
            step.setErrorMessage("Запрос отсутсвует");
            step.setStatus(StepStatus.FAILURE);
        }
    }

    private void addDBParameter(RSTests step) {
        if (step.getValue() != null && step.getValue().indexOf("{") == 0 && step.getValue().lastIndexOf("}") == step.getValue().length() - 1) {
            step.setErrorMessage("Значение переменной не существует");
            step.setStatus(StepStatus.FAILURE);
        } else {
            String str = stringDataMap.get(step.getValue());
            if (str != null) {
                parameters.put(step.getKey(), str);
            } else {
                parameters.put(step.getKey(), step.getValue());
            }
        }
    }

    public void setDriverName(RSTests step) {
        this.driverName = step.getValue();
    }

    public void setHostDB(RSTests step) {
        this.host = step.getValue();
    }

    public void setPortDB(RSTests step) {
        this.port = step.getValue();
    }

    public void setNameDB(RSTests step) {
        this.dbName = step.getValue();
    }

    public void setUserDB(RSTests step) {
        this.user = step.getValue();
    }

    public void setPasswordDB(RSTests step) {
        this.password = step.getValue();
    }
}
