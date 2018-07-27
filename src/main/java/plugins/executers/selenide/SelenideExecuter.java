package plugins.executers.selenide;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import modules.testExecutor.enums.StepStatus;
import modules.testExecutor.interfaces.CalledFromTest;
import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.RSTests;
import org.apache.commons.lang3.math.NumberUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import plugins.interfaces.TestExecutor;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import static com.codeborne.selenide.Selenide.*;

public class SelenideExecuter implements TestExecutor {
    private SuiteDatas suite;
    private TestDatas test;
    private ConcurrentHashMap<String, String> stepsMap = new ConcurrentHashMap<>();// <Наименование шага, Описание шага>
    private ConcurrentHashMap<String, String> stringDataMap;
    private ConcurrentHashMap<String, byte[]> byteDataMap;
    private Logger logger;
    private Boolean threadSuspended = false;
    private ConcurrentHashMap<String, CalledFromTest> mapOfTestCalls;
    private SelenideHelper selenideHelper;
    private String programFilesDirectory;
    private ConcurrentHashMap<String, String> settings;

    @Override
    public String getPluginName() {
        return "Selenide";
    }

    @Override
    public String getGroupName() {
        return "WEB";
    }

    @Override
    public ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getDefaultSettings() {
        ConcurrentHashMap<String, ConcurrentHashMap<String, String>> properties = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> settings;

        settings = new ConcurrentHashMap<>();
        settings.put("SearchForElementsInFrames", "true");
        properties.put("Выполнять переключение фреймов при поиске элемента на странице", settings);

        settings = new ConcurrentHashMap<>();
        settings.put("WaitingElementMillisecond", "120000");
        properties.put("Время ожидания элемента на странице. В милисекундах", settings);

        settings = new ConcurrentHashMap<>();
        settings.put("WaitingPageLoadingMillisecond", "360000");
        properties.put("Время ожидания загрузки страницы. В милисекундах", settings);

        settings = new ConcurrentHashMap<>();
        settings.put("XPathCSSParameterName", "{parameter}");
        properties.put("Устанавливает текстовый блок в строке селектора.\n#Необходим для замены в строке селектора на значение из шага", settings);

        settings = new ConcurrentHashMap<>();
        settings.put("ToCaptureTheScreenBrowserAfterTheFailureStep", "true");
        properties.put("Делать снимок браузера после поломки шага", settings);

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

        selenideHelper = new SelenideHelper(test.getProgramSettings().get("ReportResultsDirectory"));

        // Установить настройки полученные из файла
        selenideHelper.setSearchForElementsInFrames(Boolean.valueOf(settings.get("SearchForElementsInFrames")));
        selenideHelper.setWaitingElementMillisecond(Integer.valueOf(settings.get("WaitingElementMillisecond")));
        selenideHelper.setWaitingPageLoadingMillisecond(Integer.valueOf(settings.get("WaitingPageLoadingMillisecond")));
        selenideHelper.setXPathCSSParameterName(settings.get("XPathCSSParameterName"));

        try {
            selenideHelper.readAllSelectors(programFilesDirectory + File.separator + getPluginName() + ".json");
        } catch (Exception e) {
            test.printStackTrace(e);
        }
    }


    @Override
    public void close() {
        selenideHelper.closeDriver();
    }

    @Override
    public void execute(RSTests step) {
        try {
            String name, description, stepName = "";
            boolean mapEmpty = stepsMap.size() == 0;

            if (step != null) {
                stepName = step.getStep();
                if (step.getTimeoutMilliseconds() != null) {
                    selenideHelper.setWaitingElementMillisecond(Integer.valueOf(step.getTimeoutMilliseconds()));
                } else {
                    selenideHelper.setWaitingElementMillisecond(Integer.valueOf(settings.get("WaitingElementMillisecond")));
                }
            }

            name = "Открыть страницу";
            if (mapEmpty) {
                description = "Открывает страницу браузера. Пример: Шаг = Открыть страницу; Значение = http://yandex.ru";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                openPage(step);
                return;
            }

            name = "Закрыть окно браузера";
            if (mapEmpty) {
                description = "Закрывает страницу браузера. Пример: Шаг = Закрыть окно браузера;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                closePage(step);
                return;
            }

            name = "Сохранить значение элемента";
            if (mapEmpty) {
                description = "Сохраняет значение элемента в переменную. Пример: Шаг = Сохранить значение элемента; Ключ = Имя ярлыка на элемент или селектор; Значение = Имя переменной для значения";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                preserveValueItem(step);
                return;
            }

            name = "Проверить наличие элемента";
            if (mapEmpty) {
                description = "Проверяет наличие элемента на странице. Пример: Шаг = Проверить наличие элемента; Ключ = Имя ярлыка на элемент или селектор;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                checkAvailabilityOfElement(step);
                return;
            }

            name = "Проверить отсутсвие элемента";
            if (mapEmpty) {
                description = "Проверяет отсутсвие элемента на странице. Пример: Шаг = Проверить отсутсвие элемента; Ключ = Имя ярлыка на элемент или селектор;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                checkLackOfElement(step);
                return;
            }

            name = "Сделать активным окно с именем";
            if (mapEmpty) {
                description = "Кликает на окно браузера с заголовком из шага. Пример: Шаг = Сделать активным окно с именем; Значение = Имя окна браузера;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                clickWindow(step);
                return;
            }

            name = "Сделать активным окно с индексом";
            if (mapEmpty) {
                description = "Кликает на окно браузера с индексом из шага. Пример: Шаг = Сделать активным окно с индексом; Значение = Номер окна;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                clickWindowNumber(step);
                return;
            }

            name = "Закрыть окно с именем";
            if (mapEmpty) {
                description = "Закрывает окно браузера с именем из шага. Пример: Шаг = Закрыть окно с именем; Значение = Имя окна браузера;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                closeWindow(step);
                return;
            }

            name = "Закрыть окно с индексом";
            if (mapEmpty) {
                description = "Закрывает окно браузера с индексом из шага. Пример: Шаг = Закрыть окно с индексом; Значение = Номер окна;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                closeWindowNumber(step);
                return;
            }

            name = "Перейти на основной фрейм";
            if (mapEmpty) {
                description = "Переключает курсор поиска на корень документа. Пример: Шаг = Перейти на основной фрейм;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                clickDefaultFrame(step);
                return;
            }

            name = "Перейти на фрейм";
            if (mapEmpty) {
                description = "Переключает курсор поиска на указанный фрейм. Пример: Шаг = Перейти на фрейм; Ключ = Имя ярлыка на фрейм или селектор;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                clickFrame(step);
                return;
            }

            name = "Подтвердить всплывающее окно alert";
            if (mapEmpty) {
                description = "Нажимает на кнопку подтверждения/согласия на сплывающем окне браузера. Пример: Шаг = Подтвердить всплывающее окно alert;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                popUpWindowConfirm(step);
                return;
            }

            name = "Отменить всплывающее окно alert";
            if (mapEmpty) {
                description = "Нажимает на кнопку отмены на сплывающем окне браузера. Пример: Шаг = Отменить всплывающее окно alert;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                cancelPopUpWindow(step);
                return;
            }

            name = "Текущее значение адресной строки браузера сохранить в переменную";
            if (mapEmpty) {
                description = "Сохраняет значение адресной строки в переменную. Пример: Шаг = Текущее значение адресной строки браузера сохранить в переменную; Ключ = Имя переменной;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                getCurrentURL(step);
                return;
            }

            name = "Сохранить доменное имя из адресной строки браузера в переменную";
            if (mapEmpty) {
                description = "Сохраняет доменное имя адресной строки в переменную. Пример: Шаг = Сохранить доменное имя из адресной строки браузера в переменную; Ключ = Имя переменной;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                getCurrentDomain(step);
                return;
            }

            name = "Сделать снимок браузера";
            if (mapEmpty) {
                description = "Добавляет к шагу в отчёте снимок браузера. Пример: Шаг = Сделать снимок браузера; Ключ = Имя снимка; Значение = Имя снимка;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                screenshot(step);
                return;
            }

            name = "Сделать снимок экрана";
            if (mapEmpty) {
                description = "Добавляет к шагу в отчёте снимок экрана. Пример: Шаг = Сделать снимок экрана; Ключ = Имя снимка; Значение = Имя снимка;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                fullScreenshot(step);
                return;
            }

            name = "Вызвать контектсное меню на";
            if (mapEmpty) {
                description = "Вызывает контекстное меню на элементе. Пример: Шаг = Вызвать контектсное меню на; Ключ = Имя ярлыка на элемент или селектор;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                сlickContext(step);
                return;
            }

            name = "Из тега <a/> получить значение атрибута href и сохранить в переменную";
            if (mapEmpty) {
                description = "Сохраняет в переменную ссылку. Пример: Шаг = Из тега <a/> получить значение атрибута href и сохранить в переменную; Ключ = Имя переменной; Значение = Имя переменной содержащая строку тега с ссылкой (<a href=\"/hub/secure/in_messages/target_request/0068935901a00784c12b1ce001\">Текст</a>);";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                getHrefFromTagA(step);
                return;
            }

            name = "Перетащить элемент в";
            if (mapEmpty) {
                description = "Перетаскивает элемент. Пример: Шаг = Перетащить элемент в; Ключ = Имя ярлыка на элемент или селектор эелемента который будет перетащен; Значение = Имя ярлыка на элемент или селектор элемента куда будет перетаскиваться;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                dragAndDrop(step);
                return;
            }

            name = "Перейти в пункт меню";
            if (mapEmpty) {
                description = "Выполняется клик по элементу. Пример: Шаг = Перейти в пункт меню; Ключ = Имя ярлыка на элемент или селектор;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                сlick(step);
                return;
            }

            name = "Перейти на вкладку";
            if (mapEmpty) {
                description = "Выполняется клик по элементу. Пример: Шаг = Перейти на вкладку; Ключ = Имя ярлыка на элемент или селектор;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                сlick(step);
                return;
            }

            name = "Нажать на кнопку";
            if (mapEmpty) {
                description = "Выполняется клик по элементу. Пример: Шаг = Нажать на кнопку; Ключ = Имя ярлыка на элемент или селектор;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                сlick(step);
                return;
            }

            name = "Открыть список поля";
            if (mapEmpty) {
                description = "Выполняется клик по элементу. Пример: Шаг = Открыть список поля; Ключ = Имя ярлыка на элемент или селектор;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                сlick(step);
                return;
            }

            name = "Заполнить поле";
            if (mapEmpty) {
                description = "Выполняется заполнение поле такстом. Пример: Шаг = Заполнить поле; Ключ = Имя ярлыка на элемент или селектор; Значение = Текст для заполнения;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                fillField(step);
                return;
            }

            name = "Заполнить выпадающий список";
            if (mapEmpty) {
                description = "Выполняется заполнение выпадающего списка такстом. Пример: Шаг = Заполнить выпадающий список; Ключ = Имя ярлыка на элемент или селектор; Значение = Текст для заполнения;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                fillFieldDropdownList(step);
                return;
            }

            name = "Очистить поле";
            if (mapEmpty) {
                description = "Очищает поле на странице. Пример: Шаг = Очистить поле; Ключ = Имя ярлыка на элемент или селектор;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                toClearTheField(step);
                return;
            }

            name = "Загрузить файл в поле";
            if (mapEmpty) {
                description = "Загружает указанный файл в поле на странице. Пример: Шаг = Загрузить файл в поле; Ключ = Имя ярлыка на элемент или селектор; Значение = Путь к файлу;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                uploadFileField(step);
                return;
            }

            name = "Установить флаг на поле";
            if (mapEmpty) {
                description = "Устанавливает флаг в поле. Пример: Шаг = Установить флаг на поле; Ключ = Имя ярлыка на элемент или селектор;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                setFlagOnfield(step);
                return;
            }

            name = "Снять флаг с поля";
            if (mapEmpty) {
                description = "Снимает флаг с поля. Пример: Шаг = Снять флаг с поля; Ключ = Имя ярлыка на элемент или селектор;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                removeFlagOnfield(step);
                return;
            }

            name = "Получить значение поля";
            if (mapEmpty) {
                description = "Печатает значение поля. Пример: Шаг = Получить значение поля; Ключ = Имя ярлыка на элемент или селектор;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                getValueField(step);
                return;
            }

            name = "Проверить значение поля";
            if (mapEmpty) {
                description = "Печатает значение поля. Пример: Шаг = Проверить значение поля; Ключ = Имя ярлыка на элемент или селектор; Значение = Сравниваемое значение;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                checkValueField(step);
                return;
            }

            name = "Нажать на клавиатуре";
            if (mapEmpty) {
                description = "Имитирует нажатие на клавиатуре указанную кнопку(ENTER или ESCAPE или ESC). Пример: Шаг = Нажать на клавиатуре; Ключ = Имя кнопки;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                clickOnTheKeyboard(step);
                return;
            }

            name = "Нажать в таблице на значение";
            if (mapEmpty) {
                description = "В первой таблице нажимается двойным кликом на значение. Пример: Шаг = Нажать в таблице на значение; Значение = Значение в таблице;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                doubleClickInTableToTheValue(step);
                return;
            }

            name = "Кликнуть в таблице на значение";
            if (mapEmpty) {
                description = "В первой таблице нажимается одним кликом на значение. Пример: Шаг = Кликнуть в таблице на значение; Значение = Значение в таблице;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                clickInTableToTheValue(step);
                return;
            }

            name = "Сохранить в переменную значение таблицы в колонке";
            if (mapEmpty) {
                description = "В переменную сохраняется значение из указанной колонки. Строка задаётся при клике на значение в таблице. Пример: Шаг = Сохранить в переменную значение таблицы в колонке; Ключ = Имя/номер колонки; Значение = Имя переменной;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                saveTableValue(step);
                return;
            }

            name = "Кликнуть на значение табицы в колонке";
            if (mapEmpty) {
                description = "Выполняется клик по колонке таблицы. Строка задаётся при клике на значение в таблице. Пример: Шаг = Кликнуть на значение табицы в колонке; Ключ = Имя/номер колонки;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                clickTableValue(step);
                return;
            }

            name = "В выбранной таблице установить номер строки";
            if (mapEmpty) {
                description = "Устанавливается номер строки в выбранной по клику таблице. Строка задаётся при клике на значение в таблице. Пример: Шаг = В выбранной таблице установить номер строки; Значение = Номер строки;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                setTableRow(step);
                return;
            }

            name = "Сохранить значение первой строки таблиы в переменную из колонки";
            if (mapEmpty) {
                description = "В переменную сохраняется значение таблицы из первой строки в указанной колонке. Пример: Шаг = Сохранить значение первой строки таблиы в переменную из колонки;  Ключ = Наименование колонки; Значение = Имя переменной;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                saveColInTableToTheValue(step);
                return;
            }

            name = "Кликнуть по значению первой строки таблиы в колонке";
            if (mapEmpty) {
                description = "Выполняется клик в таблице по значению из указанной колонки в первой строке. Пример: Шаг = Кликнуть по значению первой строки таблиы в колонке;  Ключ = Наименование колонки;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                clickColInTableToTheValue(step);
                return;
            }

            name = "Скачать файл в переменную по клику на значение первой строки таблиы в колонке";
            if (mapEmpty) {
                description = "Выполняется скачивание файла после двойного клика на значение первой строки таблиы в указанной колонке. Пример: Шаг = Скачать файл в переменную по клику на значение первой строки таблиы в колонке;  Ключ = Наименование колонки; Значение = Имя переменной или имя файла;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                downloadColInTableToTheValue(step);
                return;
            }

            name = "Скачать в ZIP-архив все файлы по клику на значение таблицы в колонке";
            if (mapEmpty) {
                description = "Выполняется скачивание всех файлов после двойного клика на значение каждой строки таблиы в указанной колонке. Все файлы упакуются в zip-архив. Пример: Шаг = Скачать в ZIP-архив все файлы по клику на значение таблицы в колонке;  Ключ = Наименование колонки; Значение = Имя переменной или имя файла;";
                stepsMap.put(name, description);
            }
            if (name.equals(stepName)) {
                downloadAllFileColInTableToTheValue(step);
                return;
            }


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

            if (settings.get("ToCaptureTheScreenBrowserAfterTheFailureStep") != null
                    && settings.get("ToCaptureTheScreenBrowserAfterTheFailureStep").equals("true")) {
                try {
                    step.getAttachments().put(step.getStep() + "_error.png", selenideHelper.getScreenshotSelenide());
                } catch (Throwable ignor) {

                }
            }
        }
    }

    private void downloadAllFileColInTableToTheValue(RSTests step) {
        CopyOnWriteArrayList<File> fileListArray = new CopyOnWriteArrayList<>();

        if (selenideHelper.pressElement("//th[text()='" + step.getKey() + "']/../th")) {
            int size = $$(By.xpath("//th[text()='" + step.getKey() + "']/../th")).size();

            for (int x = 1; x <= size; x++) {// перебор колонок таблицы
                if (selenideHelper.pressElement("//th[text()='" + step.getKey() + "']/../th[" + x + "]")) {
                    if ($(By.xpath("//th[text()='" + step.getKey() + "']/../th[" + x + "]")).text().equals(step.getKey())) { //колонка найдена

                        int lineNum = 1; //скачивание файла каждой строки
                        while (selenideHelper.pressElement("//th[text()='" + step.getKey() + "']/../../following-sibling::tbody/tr[" + lineNum + "]/td[" + x + "]/a")) {
                            try {
                                fileListArray.add($(By.xpath("//th[text()='" + step.getKey() + "']/../../following-sibling::tbody/tr[" + lineNum + "]/td[" + x + "]/a")).download());
                            } catch (FileNotFoundException e) {
                                test.printStackTrace(e);
                            }
                            lineNum++;
                        }

                        break;
                    }
                }
            }
        }

        if (fileListArray.size() > 0) {
            byte[] zipArchive = new byte[0];
            try {
                zipArchive = selenideHelper.createZip(fileListArray).toByteArray();
            } catch (Exception e) {
                test.printStackTrace(e);
            }
            byteDataMap.put(step.getValue(), zipArchive);
            step.getAttachments().put(step.getValue(), zipArchive);
        } else {
            step.setErrorMessage("Не удалось скачать ниодного файла");
            step.setStatus(StepStatus.FAILURE);
        }
    }

    private void downloadColInTableToTheValue(RSTests step) {
        SelenideElement element = null;

        if (selenideHelper.pressElement("//th[text()='" + step.getKey() + "']/../th")) {
            int size = $$(By.xpath("//th[text()='" + step.getKey() + "']/../th")).size();
            for (int x = 1; x <= size; x++) {
                if (selenideHelper.pressElement("//th[text()='" + step.getKey() + "']/../th[" + x + "]")) {
                    if ($(By.xpath("//th[text()='" + step.getKey() + "']/../th[" + x + "]")).text().equals(step.getKey())) {
                        element = $(By.xpath("//th[text()='" + step.getKey() + "']/../../following-sibling::tbody/tr[1]/td[" + x + "]"));

                        if (selenideHelper.pressElement("//th[text()='" + step.getKey() + "']/../../following-sibling::tbody/tr[2]/td[" + x + "]")) {
                            step.setErrorMessage("В таблице больше одной строки с данными");
                            step.setStatus(StepStatus.FAILURE);
                        }
                        break;
                    }
                }
            }
        }
        element = element.find(By.cssSelector("a"));

        File file = null;

        try {
            file = element.download();
        } catch (FileNotFoundException e) {
            test.printStackTrace(e);
        }

        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            test.printStackTrace(e);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            for (int readNum; (readNum = fis.read(buf)) != -1; ) {
                bos.write(buf, 0, readNum); //no doubt here is 0
            }
        } catch (IOException ex) {
            test.printStackTrace(ex);
        }
        byte[] bytes = bos.toByteArray();

        byteDataMap.put(step.getValue(), bytes);
        step.getAttachments().put(step.getValue(), bytes);
    }

    private void clickColInTableToTheValue(RSTests step) {
        SelenideElement element = null;

        if (selenideHelper.pressElement("//th[text()='" + step.getKey() + "']/../th")) {
            int size = $$(By.xpath("//th[text()='" + step.getKey() + "']/../th")).size();
            for (int x = 1; x <= size; x++) {
                if (selenideHelper.pressElement("//th[text()='" + step.getKey() + "']/../th[" + x + "]")) {
                    if ($(By.xpath("//th[text()='" + step.getKey() + "']/../th[" + x + "]")).text().equals(step.getKey())) {
                        element = $(By.xpath("//th[text()='" + step.getKey() + "']/../../following-sibling::tbody/tr[1]/td[" + x + "]"));

                        if (selenideHelper.pressElement("//th[text()='" + step.getKey() + "']/../../following-sibling::tbody/tr[2]/td[" + x + "]")) {
                            step.setErrorMessage("В таблице больше одной строки с данными");
                            step.setStatus(StepStatus.FAILURE);
                        }
                        break;
                    }
                }
            }
        }
        element.click();
        selenideHelper.expectLoad();
    }

    private void saveColInTableToTheValue(RSTests step) {
        SelenideElement element = null;

        if (selenideHelper.pressElement("//th[text()='" + step.getKey() + "']/../th")) {
            int size = $$(By.xpath("//th[text()='" + step.getKey() + "']/../th")).size();
            for (int x = 1; x <= size; x++) {
                if (selenideHelper.pressElement("//th[text()='" + step.getKey() + "']/../th[" + x + "]")) {
                    if ($(By.xpath("//th[text()='" + step.getKey() + "']/../th[" + x + "]")).text().equals(step.getKey())) {
                        element = $(By.xpath("//th[text()='" + step.getKey() + "']/../../following-sibling::tbody/tr[1]/td[" + x + "]"));

                        if (selenideHelper.pressElement("//th[text()='" + step.getKey() + "']/../../following-sibling::tbody/tr[2]/td[" + x + "]")) {
                            step.setErrorMessage("В таблице больше одной строки с данными");
                            step.setStatus(StepStatus.FAILURE);
                        }
                        break;
                    }
                }
            }
        }
        stringDataMap.put(step.getValue(), element.innerHtml());
    }

    private void clickTableValue(RSTests step) {
        SelenideElement elm = selenideHelper.getTableCell(step.getKey());
        if (elm != null) {
            elm.click();
            selenideHelper.expectLoad();
        } else {
            step.setErrorMessage("Неудалось найти значение колонки");
            step.setStatus(StepStatus.FAILURE);
        }
    }

    private void saveTableValue(RSTests step) {
        SelenideElement elm = selenideHelper.getTableCell(step.getKey());
        if (elm != null) {
            stringDataMap.put(step.getValue(), elm.getText());
        } else {
            step.setErrorMessage("Неудалось найти значение колонки");
            step.setStatus(StepStatus.FAILURE);
        }
    }

    private void clickInTableToTheValue(RSTests step) {
        SelenideElement element = selenideHelper.getElement(step.getValue());
        element.click();
        selenideHelper.setRowTeable(element);
        selenideHelper.expectLoad();
    }

    private void doubleClickInTableToTheValue(RSTests step) {
        SelenideElement element = selenideHelper.getElement(step.getValue());
        element.doubleClick();
        selenideHelper.setRowTeable(element);
        selenideHelper.expectLoad();
    }

    private void clickOnTheKeyboard(RSTests step) {
        if (step.getKey().toUpperCase().equals("ENTER")) $(":focus").sendKeys(Keys.ENTER);
        if (step.getKey().toUpperCase().equals("ESCAPE") || step.getKey().toUpperCase().equals("ESC"))
            $(":focus").sendKeys(Keys.ESCAPE);
        selenideHelper.expectLoad();
    }

    private void checkValueField(RSTests step) {
        selenideHelper.expectLoad();
        SelenideElement element = selenideHelper.getElement(step.getKey());
        String result = selenideHelper.getValueItem(element);

        if (result.equals(step.getValue())) return;

        if (element.getAttribute("name") != null && !element.getAttribute("name").isEmpty() && element.getAttribute("id") != null && !element.getAttribute("id").isEmpty()) {
            if ($("label[id='" + $("input[name='" + element.getAttribute("name") + "']").attr("id") + "Icon']").isDisplayed()) {
                if ($("label[id='" + $("input[name='" + element.getAttribute("name") + "']").attr("id") + "Icon'].xChecked").exists()) {
                    if (step.getValue().equals("true")) return;
                    if (step.getValue().equals("false")) result = "true";
                    ;
                } else {
                    if (step.getValue().equals("false")) return;
                    if (step.getValue().equals("true")) result = "false";
                }
            }
        }
        step.setStatus(StepStatus.FAILURE);
        step.setErrorMessage("Проверка значения поля '" + step.getKey() + "'. Ожидаемый результат: '" + step.getValue() + "'. Факстический результат: '" + result + "'");
    }

    private void getValueField(RSTests step) {
        SelenideElement element = selenideHelper.getElement(step.getKey());
        String result = selenideHelper.getValueItem(element);

        result = "Значение поля '" + step.getKey() + "' = '" + result + "'";
        logger.info(result);
        step.getAttachments().put("значение", result.getBytes(StandardCharsets.UTF_8));
    }

    private void removeFlagOnfield(RSTests step) {
        SelenideElement element = selenideHelper.getElement(step.getKey());
        if (element.attr("checked") == null) {
            step.setErrorMessage("Флаг уже снят");
            step.setStatus(StepStatus.FAILURE);
        } else {
            element.click();
        }
    }

    private void uploadFileField(RSTests step) {
        SelenideElement element = selenideHelper.getElement(step.getKey());

        try {
            element.uploadFile(new File(step.getValue()));
        } catch (Throwable e) {
            test.printStackTrace(e);
        }
        try {
            element.uploadFromClasspath(step.getValue());
        } catch (Throwable e) {
            test.printStackTrace(e);
        }
    }

    private void toClearTheField(RSTests step) {
        selenideHelper.getElement(step.getKey()).clear();
    }

    private void fillFieldDropdownList(RSTests step) {
        String s, value;
        SelenideElement element = selenideHelper.getElement(step.getKey());

        value = stringDataMap.get(step.getValue()) != null ? stringDataMap.get(step.getValue()) : step.getValue();
        if (value != null && value.length() > 0 && !selenideHelper.getValueItem(element).equals(value)) {
            element.click();
            if (element.getTagName().toUpperCase().equals("SELECT")) {
                try {
                    element.selectOption(value);
                } catch (Throwable t) {
                }
            } else {
                Calendar timeoutCalendar = Calendar.getInstance();    // Текущее время в календарь-таймаут
                timeoutCalendar.add(Calendar.MILLISECOND, selenideHelper.getWaitingPageLoadingMillisecond());   // Прибавить timeout

                while (Calendar.getInstance().before(timeoutCalendar)) {/*Пока не достигли времени таймаута*/
                    ElementsCollection text;

                    try{
                        element.clear();
                    }catch (Throwable t) {
                    }

                    text = $$(By.xpath("//*[text()='" + value + "']"));
                    for (int x = 0; x < text.size(); x++) {
                        //if (text.get(x).isDisplayed() && text.get(x).isEnabled()) {
                        try {
                            if (element.getTagName().toUpperCase().equals("SELECT")) {
                                element.click();
                                element.selectOption(value);
                            } else {
                                if (element.parent().getTagName().toUpperCase().equals("SELECT")) {
                                    element.parent().click();
                                    element.parent().selectOption(value);
                                } else {
                                    text.get(x).click();
                                }
                            }
                        } catch (Throwable t) {
                        }
                        //}
                    }
                    if (element.getText().equals(value) || element.getValue().equals(value)) break;

                    try{
                        element.clear();
                    }catch (Throwable t) {
                    }

                    text = $$(By.xpath("//*[@value='" + value + "']"));
                    for (int x = 0; x < text.size(); x++) {
                        try {
                            //if (text.get(x).isDisplayed() && text.get(x).isEnabled()) {
                                if (element.getTagName().toUpperCase().equals("SELECT")) {
                                    element.click();
                                    element.selectOption(value);
                                } else {
                                    if (element.parent().getTagName().toUpperCase().equals("SELECT")) {
                                        element.parent().click();
                                        element.parent().selectOption(value);
                                    } else {
                                        text.get(x).click();
                                    }
                                }
                            //}
                        } catch (Throwable t) {
                        }
                    }
                    if (element.getText().equals(value) || element.getValue().equals(value)) break;

                    try{
                        element.clear();
                    }catch (Throwable t) {
                    }

                    try {
                        s = value;
                        if (s.length() > 2) {
                            s = s.substring(0, s.length() - 1);
                        }
                        element.sendKeys(Keys.HOME, s);
                        if (element.getText().equals(value) || element.getValue().equals(value)) break;
                    } catch (Throwable t) {
                    }

                    if (element.getText().equals(value) || element.getValue().equals(value)) break;

                    try{
                        element.clear();
                    }catch (Throwable t) {
                    }

                    try {
                        element.sendKeys(Keys.HOME, value);
                        if (element.getText().equals(value) || element.getValue().equals(value)) break;
                    } catch (Throwable t) {
                    }
                }
            }
        }
    }

    private void fillField(RSTests step) {
        SelenideElement element = selenideHelper.getElement(step.getKey());
        if (!step.getValue().isEmpty()) {
            element.sendKeys(Keys.HOME, step.getValue());
            if (!element.getValue().toUpperCase().equals(step.getValue().toUpperCase())) {
                element.click();
                element.clear();
                element.setValue(step.getValue());
            }
        }
    }

    private void сlick(RSTests step) {
        selenideHelper.getElement(step.getKey()).click();
        selenideHelper.expectLoad();
    }

    private void dragAndDrop(RSTests step) {
        selenideHelper.dragAndDrop(selenideHelper.getElement(step.getKey()), selenideHelper.getElement(step.getValue()));
    }

    private void getHrefFromTagA(RSTests step) {
        stringDataMap.put(step.getKey(), stringDataMap.get(step.getValue()).toString().split("href=\"")[1].split("\"")[0]);
    }

    private void сlickContext(RSTests step) {
        selenideHelper.getElement(step.getKey()).contextClick();
    }

    private void fullScreenshot(RSTests step) {
        String name = step.getValue() == null ? (step.getKey() == null ? "screen" : step.getKey()) : step.getValue();
        try {
            step.getAttachments().put(name + ".png", selenideHelper.getScreenshot());
        } catch (AWTException e) {
            test.printStackTrace(e);
        } catch (IOException e) {
            test.printStackTrace(e);
        }
    }

    private void screenshot(RSTests step) {
        String name = step.getValue() == null ? (step.getKey() == null ? "screen" : step.getKey()) : step.getValue();
        step.getAttachments().put(name + ".png", selenideHelper.getScreenshotSelenide());
    }

    private void getCurrentDomain(RSTests step) {
        stringDataMap.put(step.getKey(), selenideHelper.getDriver().getCurrentUrl().toString().split("http://")[1].split("/")[0]);
    }

    private void getCurrentURL(RSTests step) {
        stringDataMap.put(step.getKey(), selenideHelper.getDriver().getCurrentUrl());
    }

    private void cancelPopUpWindow(RSTests step) {
        switchTo().alert().dismiss();
        selenideHelper.expectLoad();
    }

    private void popUpWindowConfirm(RSTests step) {
        switchTo().alert().accept();
        selenideHelper.expectLoad();
    }

    private void clickFrame(RSTests step) {
        switchTo().frame(selenideHelper.getElement(step.getKey()));
    }

    private void clickDefaultFrame(RSTests step) {
        switchTo().defaultContent();
    }

    private void closeWindowNumber(RSTests step) {
        switchTo().window(Integer.valueOf(step.getValue())).close();
        switchTo().window(0);
    }

    private void closeWindow(RSTests step) {
        switchTo().window(step.getValue()).close();
        switchTo().window(0);
    }

    private void clickWindowNumber(RSTests step) {
        switchTo().window(Integer.valueOf(step.getValue()));
    }

    private void clickWindow(RSTests step) {
        switchTo().window(step.getValue());
    }

    private void checkLackOfElement(RSTests step) {
        SelenideElement element = selenideHelper.getElement(step.getKey());

        if (element != null || element.exists()) {
            step.setErrorMessage("Проверка отсутствия элемента '" + step.getKey() + "'. Элемент присутствует!");
            step.setStatus(StepStatus.FAILURE);
        }
    }

    private void checkAvailabilityOfElement(RSTests step) {
        SelenideElement element = selenideHelper.getElement(step.getKey());

        if (element == null || !element.exists()) {
            step.setErrorMessage("Проверка наличия элемента '" + step.getKey() + "'. Элемент не найден!");
            step.setStatus(StepStatus.FAILURE);
        }
    }

    private void preserveValueItem(RSTests step) {
        stringDataMap.put(step.getValue(), selenideHelper.getValueItem(selenideHelper.getElement(step.getKey())));
    }

    private void closePage(RSTests step) {
        selenideHelper.closeDriver();
    }

    private void openPage(RSTests step) {
        selenideHelper.getDriver();
        Selenide.open(step.getValue());
    }

    public void setFlagOnfield(RSTests step) {
        SelenideElement element = selenideHelper.getElement(step.getKey());
        if (element.attr("checked") != null) {
            step.setErrorMessage("Флаг уже установлен");
            step.setStatus(StepStatus.FAILURE);
        } else {
            element.click();
        }
    }

    public void setTableRow(RSTests step) {
        selenideHelper.rowIndex = -1;
        if (NumberUtils.isDigits(step.getKey())) {
            selenideHelper.rowIndex = Integer.valueOf(step.getKey());
        }
        if (NumberUtils.isDigits(step.getValue())) {
            selenideHelper.rowIndex = Integer.valueOf(step.getValue());
        }
        if (selenideHelper.rowIndex < 0) {
            step.setErrorMessage("Передано не числовое зачение");
            step.setStatus(StepStatus.FAILURE);
        }
    }
}
