package plugins.executers.datas;

import com.google.gson.Gson;
import modules.testExecutor.enums.StepStatus;
import modules.testExecutor.interfaces.CalledFromTest;
import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.RSTests;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import plugins.interfaces.TestExecutor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ValueExecuter implements TestExecutor {
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

    private String valueReplace;
    private Map<String, Object> criteriasSearchArrayLine = new HashMap<>();
    private Integer numberInArrayList = 0;

    @Override
    public String getPluginName() {
        return "Переменные";
    }

    @Override
    public String getGroupName() {
        return "Данные";
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
        this.settings = settings;
    }

    @Override
    public void close() {

    }

    @Override
    public void execute(RSTests step) {
        String name, description, stepName = "";
        boolean mapEmpty = stepsMap.size() == 0;
        if (step != null) stepName = step.getStep();

        name = "Сравнить значение переменной";
        if (mapEmpty) {
            description = "Сравнивается значение перемнной с переданным значеним или переменной. Пример: Шаг = Сравнить значение переменной; Ключ = Имя переменной; Значение = Значение или имя переменной;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            checkValueData(step);
            return;
        }

        name = "Проверить заполненность переменной";
        if (mapEmpty) {
            description = "Выполняется проверка заполненности переменной. Ключ или значение. Пример: Шаг = Проверить заполненность переменной; Ключ = Имя переменной; Значение = Имя переменной;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            checkValueIsNotNull(step);
            return;
        }

        name = "Проверить отсутсвие значения в переменной";
        if (mapEmpty) {
            description = "Выполняется проверка отсутсвие значения в переменной. Ключ или значение. Пример: Шаг = Проверить отсутсвие значения в переменной; Ключ = Имя переменной; Значение = Имя переменной;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            checkValueIsNull(step);
            return;
        }

        name = "Удалить переменную";
        if (mapEmpty) {
            description = "Выполняется удаление переменной из памяти. Пример: Шаг = Удалить переменную; Ключ = Имя переменной;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            deleteValue(step);
            return;
        }

        name = "Сохранить новое значение переменной";
        if (mapEmpty) {
            description = "Выполняется сохранение значения в заданной переменной. Пример: Шаг = Сохранить новое значение переменной; Ключ = Имя переменной; Значение = значение или имя переменной;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            addValueItem(step);
            return;
        }

        name = "Задать переменную для замены в ней текста";
        if (mapEmpty) {
            description = "Сохранить в памяти имя переменной. Пример: Шаг = Задать переменную для замены в ней текста; Ключ = Имя переменной;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setValueForReplace(step);
            return;
        }

        name = "В заданной переменной найти и заменить текст на значение из value. Искомый текст";
        if (mapEmpty) {
            description = "Сохранить в памяти имя переменной. Пример: Шаг = В заданной переменной найти и заменить текст на значение из value. Искомый текст; Ключ = Искомый текст; Значение = Новый текст;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            replaceValue(step);
            return;
        }

        name = "Копировать значение для всех тестов в переменную";
        if (mapEmpty) {
            description = "Сохранить переменную в памяти для текущего теста и для тестов в наборе переменную с указанным значением. Пример: Шаг = Копировать значение для всех тестов в переменную; Ключ = Имя переменной для тестов в наборе; Значение = Имя переменной или значение;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            addGlobalValueItem(step);
            return;
        }

        name = "Печать значения переменной";
        if (mapEmpty) {
            description = "Печатает содержимое переменной. В отчёт прикладывает текстовый файл с содержимым. Пример: Шаг = Печать значения переменной; Ключ = Имя переменной;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            printValueItem(step);
            return;
        }

        name = "К отчёту приложить файл с именем";
        if (mapEmpty) {
            description = "В отчёт прикладывает файл. Пример: Шаг = К отчёту приложить файл с именем; Ключ = имя файла в отчёте; Значение = Имя переменной содержащая файл;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            addAttachment(step);
            return;
        }

        name = "Установить критерий поиска строки массива для переменной массива";
        if (mapEmpty) {
            description = "Сохраняет в память критерии поиска в массиве (json). Пример: Шаг = Установить критерий поиска строки массива для переменной массива; Ключ = имя атрибута в массиве; Значение = значение атрибута;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setSearchCriteriaRowOfArray(step);
            return;
        }

        name = "Очистить критерий поиска строки массива";
        if (mapEmpty) {
            description = "Очищает из памяти критерии поиска в массиве. Пример: Шаг = Очистить критерий поиска строки массива;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            clearSearchCriteriaRowOfArray(step);
            return;
        }

        name = "Из массива получить значение переменной";
        if (mapEmpty) {
            description = "Сохраняет в переменную с (именем атрибут) значение из массива по имени атрибута. Критерии поиска строки в массиве устанвливаются отдельным шагом. Пример: Шаг = Из массива получить значение переменной; Ключ = имя атрибута; Значение = Имя переменной содержащая массив (json)";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            getValueFromArray(step);
            return;
        }

        name = "Установить номер элемента списка для получения значения";
        if (mapEmpty) {
            description = "Сохраняет в память номер строки. Пример: Шаг = Установить номер элемента списка для получения значения; Значение = Номер строки";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setNumberValueFromArrayList(step);
            return;
        }

        name = "Из списка получить значение с ранее заданным номером в списке";
        if (mapEmpty) {
            description = "Сохраняет в переменную значение из строки списка/массива. Пример: Шаг = Из списка получить значение с ранее заданным номером в списке; Ключ = Имя переменной; Значение = имя переменной содержащая список/массив";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            getValueFromArrayList(step);
            return;
        }

        name = "Вычислить математическое выражение и сохранить результат в переменную";
        if (mapEmpty) {
            description = "Сохраняет в переменную результат вычисления. Пример: Шаг = Вычислить математическое выражение и сохранить результат в переменную; Ключ = Имя переменной; Значение = математическое выражение. Пример: 5/1-(2^4+3)*2;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            mathSpel(step);
            return;
        }
    }

    private void mathSpel(RSTests step) {
        String value = step.getValue();
        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(value);
        stringDataMap.put(step.getKey(), expression.getValue().toString());
        logger.info(stringDataMap.get(step.getKey()));
    }

    private void getValueFromArrayList(RSTests step) {
        boolean pressCriterias;
        Gson gson = new Gson();
        String result1, result2;
        String dataValue = stringDataMap.get(step.getValue());
        String result;

        if (dataValue != null) {
            ArrayList<Object> arrayList = gson.fromJson(dataValue, ArrayList.class);
            int x = 0;
            for (Object list : arrayList) {
                if (x == numberInArrayList) {
                    result = list.toString();
                    if (result.lastIndexOf(".0") > 0)
                        result = result.substring(0, result.length() - 2);

                    stringDataMap.put(step.getKey(), result);
                    step.getAttachments().put(step.getKey(), result.getBytes(StandardCharsets.UTF_8));
                    logger.info(result);
                    break;
                }
                x++;
            }
            if (stringDataMap.get(step.getKey()) == null) {
                step.setErrorMessage("Значение не найдено");
                step.setStatus(StepStatus.FAILURE);
            }
        } else {
            step.setErrorMessage("Список/Массив отсутсвет");
            step.setStatus(StepStatus.FAILURE);
        }
    }

    private void getValueFromArray(RSTests step) {
        boolean pressCriterias;
        Gson gson = new Gson();
        String result1, result2;
        String dataValue = stringDataMap.get(step.getValue());

        if (dataValue != null) {
            ArrayList<Object> arrayList = gson.fromJson(dataValue, ArrayList.class);
            for (Object list : arrayList) {
                Map<String, Object> map = gson.fromJson(list.toString(), Map.class);
                pressCriterias = true;

                for (Map.Entry<String, Object> stringEntry : criteriasSearchArrayLine.entrySet()) {
                    if (map.get(stringEntry.getKey()) != null && stringEntry.getValue() != null) {
                        result1 = stringEntry.getValue().toString();
                        result2 = map.get(stringEntry.getKey()).toString();

                        if (result1.lastIndexOf(".0") > 0 && result2.lastIndexOf(".0") < 0) {
                            result2 += ".0";
                        }
                        if (result2.lastIndexOf(".0") > 0 && result1.lastIndexOf(".0") < 0) {
                            result1 += ".0";
                        }
                        if (!result1.equals(result2)) {
                            pressCriterias = false;
                            break;
                        }
                    }
                }
                if (pressCriterias && map.get(step.getKey()) != null) {
                    stringDataMap.put(step.getKey(), map.get(step.getKey()).toString());
                    break;
                }
            }
            if (stringDataMap.get(step.getKey()) == null) {
                step.setErrorMessage("Значение не найдено");
                step.setStatus(StepStatus.FAILURE);
            }
        } else {
            step.setErrorMessage("Массив отсутсвет");
            step.setStatus(StepStatus.FAILURE);
        }
    }

    private void clearSearchCriteriaRowOfArray(RSTests step) {
        criteriasSearchArrayLine.clear();
    }

    private void addAttachment(RSTests step) {
        step.getAttachments().put(step.getKey(), byteDataMap.get(step.getValue()));
    }

    private void printValueItem(RSTests step) {
        String str = stringDataMap.get(step.getKey());
        if (str != null) {
            logger.info(str);
            step.getAttachments().put(step.getKey(), str.getBytes(StandardCharsets.UTF_8));
        } else {
            byte[] bt = byteDataMap.get(step.getKey());
            if (bt != null) {
                str = new String(bt, StandardCharsets.UTF_8);
                logger.info(str);
                step.getAttachments().put(step.getKey(), str.getBytes(StandardCharsets.UTF_8));
            } else {
                logger.info("null");
                step.getAttachments().put(step.getKey(), "null".getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private void addGlobalValueItem(RSTests step) {
        byte[] bt = byteDataMap.get(step.getValue());
        String str = stringDataMap.get(step.getValue());
        if (str == null) str = step.getValue();

        if (bt == null && str == null) {
            step.setErrorMessage("Копируемое значение отсутсвует");
            step.setStatus(StepStatus.FAILURE);
        } else {
            if (bt != null) {
                suite.getByteDataMap().put(step.getKey(), bt);
                //byteDataMap.put(step.getKey(), bt);

                // Добавить данные всем тестам в наборе
                for (Map.Entry<Long, TestDatas> tests : suite.getTestsMap().entrySet()) {
                    tests.getValue().getByteDataMap().put(step.getKey(), bt);
                }
            }
            if (str != null) {
                suite.getStringDataMap().put(step.getKey(), str);
                //stringDataMap.put(step.getKey(), str);

                // Добавить данные всем тестам в наборе
                for (Map.Entry<Long, TestDatas> tests : suite.getTestsMap().entrySet()) {
                    tests.getValue().getStringDataMap().put(step.getKey(), str);
                }
            }
        }
    }

    private void replaceValue(RSTests step) {
        String result = stringDataMap.get(valueReplace);
        result = result.replace(step.getKey(), step.getValue());

        try {
            if (stringDataMap.containsKey(step.getKey())) stringDataMap.remove(step.getKey());
        } catch (Exception e) {
        }

        stringDataMap.put(valueReplace, result);
    }

    private void addValueItem(RSTests step) {
        if (step.getKey() == null) {
            step.setErrorMessage("Наименование переменной не существует");
            step.setStatus(StepStatus.BROKEN);
        } else {
            if (step.getValue() != null && step.getValue().indexOf("{") == 0 && step.getValue().lastIndexOf("}") == step.getValue().length() - 1) {
                step.setErrorMessage("Значение переменной не существует");
                step.setStatus(StepStatus.BROKEN);
            } else {
                if (step.getValue() != null && byteDataMap.get(step.getValue()) != null) {
                    byte[] bt = byteDataMap.get(step.getValue());
                    byteDataMap.put(step.getKey(), bt);
                } else {
                    if (step.getValue() != null && stringDataMap.get(step.getValue()) != null) {
                        String str = stringDataMap.get(step.getValue());
                        stringDataMap.put(step.getKey(), str);
                    } else {
                        if(step.getValue()!=null) {
                            stringDataMap.put(step.getKey(), step.getValue());
                        } else {
                            step.setErrorMessage("Значение переменной не существует");
                            step.setStatus(StepStatus.BROKEN);
                        }
                    }
                }
            }
        }
    }

    private void deleteValue(RSTests step) {
        try {
            if (stringDataMap.containsKey(step.getKey())) stringDataMap.remove(step.getKey());
        } catch (Exception e) {
        }

        try {
            if (byteDataMap.containsKey(step.getKey())) byteDataMap.remove(step.getKey());
        } catch (Exception e) {
        }

        try {
            if (suite.getStringDataMap().containsKey(step.getKey())) suite.getStringDataMap().remove(step.getKey());
        } catch (Exception e) {
        }

        try {
            if (suite.getByteDataMap().containsKey(step.getKey())) suite.getByteDataMap().remove(step.getKey());
        } catch (Exception e) {
        }
    }

    private void checkValueIsNull(RSTests step) {
        String str = stringDataMap.get(step.getValue() == null ? (step.getKey() == null ? "" : step.getKey()) : step.getValue());
        byte[] bt = byteDataMap.get(step.getValue() == null ? (step.getKey() == null ? "" : step.getKey()) : step.getValue());

        if ((str != null && str.length() > 0) || (bt != null && bt.length > 0)) {
            step.setErrorMessage("Значение переменной существует.");
            step.setStatus(StepStatus.FAILURE);
        }
    }

    private void checkValueIsNotNull(RSTests step) {
        String str = stringDataMap.get(step.getValue() == null ? (step.getKey() == null ? "" : step.getKey()) : step.getValue());
        byte[] bt = byteDataMap.get(step.getValue() == null ? (step.getKey() == null ? "" : step.getKey()) : step.getValue());

        if (str == null && bt == null) {
            step.setErrorMessage("Переменная не существует");
            step.setStatus(StepStatus.FAILURE);

        } else {
            if (str != null && str.length() == 0) {
                step.setErrorMessage("Значение переменной пусто");
                step.setStatus(StepStatus.FAILURE);
            }

            if (bt != null && bt.length == 0) {
                step.setErrorMessage("Значение переменной пусто");
                step.setStatus(StepStatus.FAILURE);
            }
        }
    }

    private void checkValueData(RSTests step) {
        boolean validation = false;
        try {
            if (byteDataMap.get(step.getValue()) != null && byteDataMap.get(step.getKey()) != null) {
                validation = !byteDataMap.get(step.getValue()).equals(byteDataMap.get(step.getKey()));

            } else if (stringDataMap.get(step.getValue()) != null && stringDataMap.get(step.getKey()) != null) {
                logger.info("|" + stringDataMap.get(step.getValue()) + "| == |" + stringDataMap.get(step.getKey()) + "|");
                validation = !stringDataMap.get(step.getKey()).equals(stringDataMap.get(step.getValue()));

            } else if (stringDataMap.get(step.getValue()) != null) {
                logger.info("|" + step.getKey() + "| == |" + stringDataMap.get(step.getValue()) + "|");
                validation = !stringDataMap.get(step.getValue()).equals(step.getKey());

            } else if (stringDataMap.get(step.getKey()) != null) {
                logger.info("|" + stringDataMap.get(step.getKey()) + "| == |" + step.getValue() + "|");
                validation = !stringDataMap.get(step.getKey()).equals(step.getValue());
            }

            if (step.getValue() == null || step.getKey() == null) {
                step.setErrorMessage("Данные для сравнения отсутсуют");
                step.setStatus(StepStatus.FAILURE);
            }

        } catch (Exception e) {
            step.setErrorMessage("Значения не совпадают.");
            step.setStatus(StepStatus.FAILURE);
        }

        if (validation) {
            step.setErrorMessage("Значения не совпадают.");
            step.setStatus(StepStatus.FAILURE);
        }
    }

    public void setValueForReplace(RSTests step) {
        valueReplace = step.getKey();
    }

    public void setSearchCriteriaRowOfArray(RSTests step) {
        criteriasSearchArrayLine.put(step.getKey(), step.getValue());
    }

    public void setNumberValueFromArrayList(RSTests step) {
        this.numberInArrayList = Integer.valueOf(step.getValue());
    }
}
