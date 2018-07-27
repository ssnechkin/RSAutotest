package plugins.executers.pdf.statusModel;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import modules.testExecutor.enums.StepStatus;
import modules.testExecutor.interfaces.CalledFromTest;
import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.RSTests;
import org.openqa.selenium.By;
import plugins.executers.pdf.statusModel.json.JsonDocument;
import plugins.executers.pdf.statusModel.templates.CollectionPDFSql;
import plugins.executers.pdf.statusModel.templates.ColumnPDFSql;
import plugins.interfaces.TestExecutor;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class PDFStatusModelExecutor implements TestExecutor {
    private SuiteDatas suite;
    private TestDatas test;
    private ConcurrentHashMap<String, String> stepsMap = new ConcurrentHashMap<>();// <Наименование шага, Описание шага>
    private ConcurrentHashMap<String, String> stringDataMap;
    private ConcurrentHashMap<String, byte[]> byteDataMap;
    private Logger logger;
    private Boolean threadSuspended = false;
    private ConcurrentHashMap<String, CalledFromTest> mapOfTestCalls;
    private String programFilesDirectory;

    @Override
    public String getPluginName() {
        return "PDFStatusModel";
    }

    @Override
    public String getGroupName() {
        return "PDF";
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
    }

    @Override
    public void execute(RSTests step) {
        String name, description, stepName = "";
        boolean mapEmpty = stepsMap.size() == 0;

        if (step != null) stepName = step.getStep();

        name = "Сохранить в переменную статусную модель документа со страници Заявления с ЕПГУ и МФЦ";
        if (mapEmpty) {
            description = "Сохраняет в переменную json полученный со страници Заявления с ЕПГУ и МФЦ. Пример: Шаг = Сохранить в переменную статусную модель документа со страници Заявления с ЕПГУ и МФЦ; Ключ = Имя переменной;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            getStatusModelFromEPGUandMFC(step);
            return;
        }

        name = "Из SQL-запроса получить и сохранить статусную модель в переменную";
        if (mapEmpty) {
            description = "Выполняет SQL-запрос и сохраняет в переменную полученный json отформатированный для статусной модели . Пример: Шаг = Из SQL-запроса получить и сохранить статусную модель в переменную; Ключ = Имя переменной; Значение = SQL-запрос или имя переменной содержащая SQL-запроса;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            getStatusModelFromSQL(step);
            return;
        }

        name = "Проверить статусную модель";
        if (mapEmpty) {
            description = "Выполняет выполняется сравнение переданных переменных с json отформатированных для статусной модели. В шаг добавляется сформированный pdf. Пример: Шаг = Проверить статусную модель; Ключ = Имя переменной с ожидаемым результатом; Значение = Имя переменной с актуальным результатом;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            checkStatusModelVioEhd(step);
            return;
        }
    }

    private void checkStatusModelVioEhd(RSTests step) {
        byte[] exFile = byteDataMap.get(step.getKey());
        JsonDocument jsonDocument = null;
        if (exFile == null) exFile = stringDataMap.get(step.getKey()).getBytes(StandardCharsets.UTF_8);

        if (stringDataMap.get(step.getValue()) != null && exFile != null) {
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                CollectionPDFSql collectionPDFSql = gson.fromJson(stringDataMap.get(step.getValue()), CollectionPDFSql.class);
                SQLIterator sqlIterator = new SQLIterator(collectionPDFSql);
                jsonDocument = new JsonDocument();

                // передаем ожидаемую модель
                logger.info("Ожидаемый набор статусов" + jsonDocument.fromJson(false, exFile));

                // передаем фактическую модель
                logger.info("Фактический набор статусов" + jsonDocument.from(true, getStatusModel(sqlIterator)));

                // объединяем статусные модели
                jsonDocument.mergeGroups();
            } catch (Exception e) {
                step.setErrorMessage(e.getMessage());
                test.printStackTrace(e);
            }

            try {
                // печатаем в файл
                step.getAttachments().put("StatusModelReport.pdf", jsonDocument.printToPdf(test.getName()));
            } catch (Exception e) {
                step.setErrorMessage(e.getMessage());
                test.printStackTrace(e);
            }
            if (jsonDocument.isFailed()) {
                step.setErrorMessage("Полученные данные не соответствуют ожидаемому результату");
                step.setStatus(StepStatus.FAILURE);
            }

        } else {
            step.setErrorMessage("Отсутсвуют данные для анализа");
            step.setStatus(StepStatus.FAILURE);
        }
    }

    private void getStatusModelFromSQL(RSTests step) {
        String value = step.getValue();
        if (stringDataMap.get(value) != null) value = stringDataMap.get(value);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (value == null) {
            step.setStatus(StepStatus.BROKEN);
            step.setErrorMessage("Результат запроса отсутсвует.");
        } else {
            CollectionPDFSql collectionPDFSql = gson.fromJson(value, CollectionPDFSql.class);

            SQLIterator sqlIterator = new SQLIterator(collectionPDFSql);
            JsonDocument jsonDocument = new JsonDocument();

            try {
                stringDataMap.put(step.getKey(), jsonDocument.from(true, getStatusModel(sqlIterator)));
            } catch (Exception e) {
                test.printStackTrace(e);
            }
        }
    }

    private void getStatusModelFromEPGUandMFC(RSTests step) {
        String numberRequest = $(By.xpath("//table/tbody/tr/td[text()=\"№ заявления:\"]/following-sibling::td")).innerText();
        ElementsCollection elements = $$(By.xpath("//table//tr/td/*[text()=\"Этапы исполнения\"]/parent::td/parent::tr/following-sibling::tr//table/tbody/tr"));

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        CollectionPDFSql collectionPDFSql;
        CopyOnWriteArrayList<String> labels = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<String> col;
        CopyOnWriteArrayList<ColumnPDFSql> collect = new CopyOnWriteArrayList<>();

        labels.add("DOC_ID");
        labels.add("DOC_TYP");
        labels.add("DOC_TYP_NAME");
        labels.add("DOC_STATUS");
        labels.add("DOC_STATUS_NAME");
        labels.add("DOC_STATUS_DATE");

        for (SelenideElement element : elements) {
            try {
                col = new CopyOnWriteArrayList<>();
                col.add(numberRequest);
                col.add(element.find("td", 0).innerText());
                col.add(element.find("td", 1).innerText());
                col.add("0");
                col.add(element.find("td", 2).innerText());
                col.add(element.find("td", 4).innerText());
                collect.add(new ColumnPDFSql(col));
            } catch (Throwable t) {
                logger.info(t.getMessage());
            }
        }
        String result = null;
        try {
            collectionPDFSql = new CollectionPDFSql(collect);
            collectionPDFSql.setColums(labels);
            result = gson.toJson(collectionPDFSql);
        } catch (Throwable t) {
            logger.info(t.getMessage());
        }

        if (result != null) {
            stringDataMap.put(step.getKey(), result);
        } else {
            step.setStatus(StepStatus.FAILURE);
            step.setErrorMessage("Не удалось сформировать модель");
        }
    }


    private List<Map<String, String>> getStatusModel(SQLIterator sqlIterator) {
        List<Map<String, String>> out = new CopyOnWriteArrayList<>();
        while (sqlIterator.next()) {
            ConcurrentHashMap<String, String> temp = new ConcurrentHashMap<>();
            temp.put("DOC_ID", sqlIterator.getString("DOC_ID"));
            temp.put("DOC_TYP", sqlIterator.getString("DOC_TYP"));
            temp.put("DOC_TYP_NAME", sqlIterator.getString("DOC_TYP_NAME"));
            temp.put("DOC_STATUS", sqlIterator.getString("DOC_STATUS"));
            temp.put("DOC_STATUS_NAME", sqlIterator.getString("DOC_STATUS_NAME"));
            temp.put("DOC_STATUS_DATE", sqlIterator.getString("DOC_STATUS_DATE"));
            out.add(temp);
        }
        return out;
    }
}
