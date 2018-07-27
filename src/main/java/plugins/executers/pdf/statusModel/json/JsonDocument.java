package plugins.executers.pdf.statusModel.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс работы со статусной моделью документа
 */
public class JsonDocument {
    private boolean isFail = false;
    // список разделов с ожидаемыми статусами
    private List<JsonGroup> groupsExpected;
    // список разделов с фактическими статусами
    private List<JsonGroup> groupsReceived;
    // список разделов с итоговыми статусами
    private List<JsonMergedGroup> resultGroups;

    // паттерн выборки данных их результата SQL запроса
    private String patternReceivedSQL = "^\\s*(\\d+)\\s*(\\d+)\\s*(.*?)\\s*(\\d+)\\s*(.*?)\\s*([0-9.]+\\s[0-9:]+)";
    // объект паттерна
    private Pattern pattern;

    // тип выводимого документа
    private DOCUMENT_TYPE type;

    /**
     * Главный конструктор.
     */
    public JsonDocument() {
        this.groupsExpected = new ArrayList<>();
        this.groupsReceived = new ArrayList<>();
        this.resultGroups = new ArrayList<>();
        this.type = DOCUMENT_TYPE.BASE;
    }

    public JsonDocument(DOCUMENT_TYPE type) {
        this();
        this.type = type;
    }

    /**
     * Метод принимает на вход текст из JSON файла и распарсивает его в список групп статусов
     *
     * @param isReceived - записать данные в полученные или нет
     * @param jsonLines  - строки из JSON файла
     * @return - результат в виде JSON текста, который распарсился
     */
    public String fromJson(boolean isReceived, String jsonLines) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type listOfTestObject = new TypeToken<List<JsonGroup>>() {
        }.getType();
        if (isReceived) {
            this.groupsReceived = gson.fromJson(jsonLines, listOfTestObject);
            return gson.toJson(this.groupsReceived);
        } else {
            this.groupsExpected = gson.fromJson(jsonLines, listOfTestObject);
            return gson.toJson(this.groupsExpected);
        }
    }

    /**
     * Метод принимает на вход JSON файл и распарсивает его в список групп статусов
     *
     * @param isReceived - записать данные в полученные или нет
     * @param jsonFile   - JSON файл
     * @return - результат в виде JSON текста, который распарсился
     */
    public String fromJson(boolean isReceived, File jsonFile) throws IOException {
        return this.fromJson(isReceived, new String(Files.readAllBytes(jsonFile.toPath()), StandardCharsets.UTF_8));
    }

    /**
     * Метод принимает на вход байтовое представление JSON файла и распарсивает его в список групп статусов
     *
     * @param isReceived      - записать данные в полученные или нет
     * @param jsonFileInBytes - байтовое представление JSON файла
     * @return - результат в виде JSON текста, который распарсился
     */
    public String fromJson(boolean isReceived, byte[] jsonFileInBytes) {
        return this.fromJson(isReceived, new String(jsonFileInBytes, StandardCharsets.UTF_8));
    }

    /**
     * Метод принимает на вход результат работы SQL скрипта и распарисвает его в список групп статусов
     * <p>
     * Пример входных данных:
     * 1438552    29 Пакет Запрос 1 Зарегистрирован 2017.11.20 03:45:46
     * 1438553    14 УПП         1 Зарегистрирован 2017.11.20 03:45:47
     * 1438553    14 УПП        12 Сформирован     2017.11.20 03:45:51
     * 1438554 10000 СЗВ-Запрос  1 Зарегистрирован 2017.11.20 03:45:49
     * 1438554 10000 СЗВ-Запрос  8 Отправлен       2017.11.20 03:45:52
     * 1438554 10000 СЗВ-Запрос  7 Отказ       2017.11.20 03:45:55
     *
     * @param isReceived - записать данные в полученные или нет
     * @param lines      - результат работы скрипта SQL
     * @return - результат в виде JSON текста, который распарсился
     */
    public String from(boolean isReceived, String lines) throws Exception {
        pattern = Pattern.compile(this.patternReceivedSQL, Pattern.UNICODE_CHARACTER_CLASS | Pattern.UNICODE_CASE | Pattern.MULTILINE);
        Matcher finder = pattern.matcher(lines);

        // если удалось найти данные
        while (finder.find()) {
            // получаем характеристики
            String id = finder.group(1).trim();
            String type = finder.group(2).trim();
            String typeName = finder.group(3).trim();

            String statusId = finder.group(4).trim();
            String statusName = finder.group(5).trim();
            String statusDate = finder.group(6).trim();

            // находим группу или создаем новую
            if (isReceived) {
                int numberGroup = getGroupOrCreate(this.groupsReceived, id, type, typeName);
                this.groupsReceived.get(numberGroup).addStatus(new JsonStatus(statusId, statusName, statusDate));
            } else {
                int numberGroup = getGroupOrCreate(this.groupsExpected, id, type, typeName);
                this.groupsExpected.get(numberGroup).addStatus(new JsonStatus(statusId, statusName, statusDate));
            }
        }

        return new GsonBuilder().setPrettyPrinting().create().toJson(isReceived ? this.groupsReceived : this.groupsExpected);
    }

    /**
     * Метод принимает на вход ResultSet SQL скрипта.
     * Обязательные поля в ResultSet:
     * - DOC_ID            - ID документа
     * - DOC_TYP           - ID типа документа
     * - DOC_TYP_NAME      - Название документа
     * - DOC_STATUS        - ID статуса документа
     * - DOC_STATUS_NAME   - название статуса документа
     * - DOC_STATUS_DATE   - дата статуса документа
     *
     * @param isReceived - записать данные в полученные или нет
     * @param results    - ResultSet скрипта SQL
     * @return - результат в виде JSON текста, который распарсился
     */
    public String from(boolean isReceived, ResultSet results) throws SQLException {
        while (results.next()) {
            // получаем характеристики
            String id = results.getString("DOC_ID");
            String type = results.getString("DOC_TYP");
            String typeName = results.getString("DOC_TYP_NAME");

            String statusId = results.getString("DOC_STATUS");
            String statusName = results.getString("DOC_STATUS_NAME");
            String statusDate = results.getString("DOC_STATUS_DATE");

            // находим группу или создаем новую
            if (isReceived) {
                int numberGroup = getGroupOrCreate(this.groupsReceived, id, type, typeName);
                this.groupsReceived.get(numberGroup).addStatus(new JsonStatus(statusId, statusName, statusDate));
            } else {
                int numberGroup = getGroupOrCreate(this.groupsExpected, id, type, typeName);
                this.groupsExpected.get(numberGroup).addStatus(new JsonStatus(statusId, statusName, statusDate));
            }
        }

        return new GsonBuilder().setPrettyPrinting().create().toJson(isReceived ? this.groupsReceived : this.groupsExpected);
    }

    /**
     * Метод принимает на вход список из именнованных карт.
     * Обязательные поля в картах:
     * - DOC_ID            - ID документа
     * - DOC_TYP           - ID типа документа
     * - DOC_TYP_NAME      - Название документа
     * - DOC_STATUS        - ID статуса документа
     * - DOC_STATUS_NAME   - название статуса документа
     * - DOC_STATUS_DATE   - дата статуса документа
     *
     * @param isReceived - записать данные в полученные или нет
     * @param results    - список именованных карт
     * @return - результат в виде JSON текста, который распарсился
     */
    public String from(boolean isReceived, List<Map<String, String>> results) throws Exception {
        for (Map<String, String> grabbedMap : results) {
            // получаем характеристики
            String id = grabbedMap.get("DOC_ID");
            String type = grabbedMap.get("DOC_TYP");
            String typeName = grabbedMap.get("DOC_TYP_NAME");

            String statusId = grabbedMap.get("DOC_STATUS");
            String statusName = grabbedMap.get("DOC_STATUS_NAME");
            String statusDate = grabbedMap.get("DOC_STATUS_DATE");

            // находим группу или создаем новую
            if (isReceived) {
                int numberGroup = getGroupOrCreate(this.groupsReceived, id, type, typeName);
                this.groupsReceived.get(numberGroup).addStatus(new JsonStatus(statusId, statusName, statusDate));
            } else {
                int numberGroup = getGroupOrCreate(this.groupsExpected, id, type, typeName);
                this.groupsExpected.get(numberGroup).addStatus(new JsonStatus(statusId, statusName, statusDate));
            }
        }

        return new GsonBuilder().setPrettyPrinting().create().toJson(isReceived ? this.groupsReceived : this.groupsExpected);
    }

    /**
     * Метод ищет в переданном списке групп группу по ID, TYP и TYP_NAME.
     * Если группа существует - то возвращает ссылку на нее.
     * Если группа НЕ существует - то создает ее в списке и возвращает ссылку на нее.
     *
     * @param groupsReceived - список, в котором нужно искать
     * @param id             - параметр ID
     * @param type           - параметр TYP
     * @param typeName       - параметр TYP_NAME
     * @return - возвращает номер в списке группы
     */
    private int getGroupOrCreate(List<JsonGroup> groupsReceived, String id, String type, String typeName) {
        for (int iter = 0, max = groupsReceived.size(); iter < max; iter++) {
            JsonGroup group = groupsReceived.get(iter);

            if (group.getId().equals(id) && group.getType().equals(type) && group.getTypeName().equals(typeName)) {
                return iter;
            }
        }

        groupsReceived.add(new JsonGroup(id, type, typeName));
        return groupsReceived.size() - 1;
    }

    /**
     * Метод объединяет списки полученных и фактических статусов в один.
     */
    public void mergeGroups() {
        int number = -1;
        List<JsonGroup> temp = new ArrayList<>();
        temp.addAll(this.groupsReceived);

        // проходимся по ожидаемому списку
        for (JsonGroup group : this.groupsExpected) {
            number = getGroupByGroup(temp, group);
            // если нашли в фактическом списке ожидаемую группу, то объединяем статусы и удаляем из списка фактических
            if (number != -1) {
                JsonMergedGroup mergedGroup = new JsonMergedGroup(temp.get(number).getId(), group.getType(), temp.get(number).getTypeName(), group.getTypeName());
                if (!mergedGroup.generateMergedStatuses(group.getStatuses(), temp.get(number).getStatuses())) {
                    this.turnFailOn();
                }
                this.resultGroups.add(mergedGroup);
                temp.remove(number);
            }
            // иначе добавляем в итоговый список, что мы не нашли ожидаему группу
            else {
                this.turnFailOn();
                JsonMergedGroup mergedGroup = new JsonMergedGroup("", "", "", group.getTypeName());
                mergedGroup.generateMergedStatuses(group.getStatuses(), new ArrayList<>());
                this.resultGroups.add(mergedGroup);
            }
        }
        // если в фактическом списке остались элементы, выводим их в конец итогового списка
        if (temp.size() > 0) {
            this.turnFailOn();
            for (JsonGroup group : temp) {
                JsonMergedGroup mergedGroup = new JsonMergedGroup(group.getId(), group.getType(), group.getTypeName(), "");
                mergedGroup.generateMergedStatuses(new ArrayList<>(), group.getStatuses());
                this.resultGroups.add(mergedGroup);
            }
        }
    }

    /**
     * Метод ищет номер группы в переданном списке по другой группе
     *
     * @param groups - список групп, в котором нужно искать
     * @param group  - группу, которую нужно искать
     * @return - возвращает номер группы
     */
    private int getGroupByGroup(List<JsonGroup> groups, JsonGroup group) {
        for (int iter = 0, max = groups.size(); iter < max; iter++) {
            JsonGroup temp = groups.get(iter);

            if (temp.getType().equals(group.getType()) && temp.getTypeName().equals(group.getTypeName())) {
                return iter;
            }
        }
        return -1;
    }


    /**
     * Метод выводит итоговый список объединенных групп в PDF файл
     *
     * @param suiteName - название тест
     * @return - байтовое представление PDF файла
     */
    public byte[] printToPdf(String suiteName) throws IOException {
        final int FONT_SIZE_SMALL = 10;
        final int FONT_SIZE_BIG = 16;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfFont mainFont = null;
        byte[] fileB = null;
        //PdfEncodings.
        /*try {
            //mainFont = PdfFontFactory.createFont(FontConstants.TIMES_ROMAN, "cp1251", true);
            mainFont = PdfFontFactory.createFont("src/main/resources/fonts/FreeSans/FreeSans.ttf", "cp1251", true);
        } catch (Exception e) {
            LoggerInstance.getInstance().printStackTrace(e);
        }*/
        /*try {
            fileB= new RSFile().extractFileFromZip(Files.readAllBytes(
                    new File(MainClass.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).toPath()), "/fonts/FreeSans/FreeSans.ttf"
            );
        } catch (Exception e) {
            LoggerInstance.getInstance().printStackTrace(e);
        }

        try {
            mainFont = PdfFontFactory.createFont(fileB, "cp1251", true);
        } catch (Exception e) {
            LoggerInstance.getInstance().printStackTrace(e);
        }*/



        fileB = IOUtils.toByteArray(getClass().getResourceAsStream("/fonts/FreeSans/FreeSans.ttf"));



            mainFont = PdfFontFactory.createFont(fileB, "cp1251", true);


       /* try {
            byte[] fontContents = IOUtils.toByteArray(getClass().getResourceAsStream("/fonts/FreeSans/FreeSans.ttf"));
            //FontProgram fontProgram = FontProgramFactory.createFont(fontContents);
            //document.setFont(PdfFontFactory.createFont(fontProgram, PdfEncodings.IDENTITY_H));

            PdfFontFactory.register(new File("FreeSans.ttf").getPath(), "FreeSans");
            mainFont = PdfFontFactory.createRegisteredFont("Free", "cp1251", true);
        } catch (IOException e) {
            LoggerInstance.getInstance().printStackTrace(e);
        }*/



       /* try {
            mainFont = PdfFontFactory.createFont(MainClass.class.getResource("/fonts/FreeSans/FreeSans.ttf").getFile(), "cp1251", true);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        PdfDocument pdfDocument = new PdfDocument(new PdfWriter(outputStream));
        Document document = new Document(pdfDocument, new PageSize(PageSize.A4).rotate());
        document.setFont(mainFont);

        // Заголовок документа
        document.add(new Paragraph(String.format("Отчёт о проверке статусной модели: %s", suiteName))
                .setFontSize(FONT_SIZE_BIG)
                .setFont(mainFont));
        document.setMargins(20, 20, 20, 40);        // Отступы страницы

        UnitValue[] columns = new UnitValue[this.type.length];

        for (int iter = 0; iter < this.type.length; iter++) {
            columns[iter] = new UnitValue(UnitValue.PERCENT, 100 / this.type.length);
        }

        // задаем размеры столбцов
        Table table = new Table(columns, true);
        table.setWidth(new UnitValue(UnitValue.PERCENT, 100));

        // Заголовки группировок
        Cell cell = new Cell(1, this.type.lengthExpected)
                .setKeepTogether(true)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .add(new Paragraph("Фактический результат").setFontSize(FONT_SIZE_SMALL).setBold().setTextAlignment(TextAlignment.CENTER));
        table.addCell(cell);

        cell = new Cell(1, this.type.lengthReceived)
                .setKeepTogether(true)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .add(new Paragraph("Ожидаемый результат").setFontSize(FONT_SIZE_SMALL).setBold().setTextAlignment(TextAlignment.CENTER));
        table.addCell(cell);

        // Верхние заголовки таблицы
        for (String header : this.type.expected) {
            if (!header.isEmpty())
                setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, header);
        }
        for (String header : this.type.received) {
            if (!header.isEmpty())
                setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, header, ColorConstants.ORANGE);
        }
            /*setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, "ID");
            setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, "ID Тип");
            setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, "Тип");
            setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, "ID Статус");
            setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, "Статус");
            setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, "Дата время");
            setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, "Тип", ColorConstants.ORANGE);
            setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, "ID Статус", ColorConstants.ORANGE);
            setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, "Статус", ColorConstants.ORANGE);*/

        // добавляем данные
        boolean isEmpty = false;
              /*
        for (int iter = 0, max = this.resultGroups.size(); iter < max; iter++) {
            try {
                isEmpty = (iter < this.type.expected.length) ? this.type.expected[iter].isEmpty() : this.type.received[iter - this.type.expected.length].isEmpty();
                if (!isEmpty) {
                    createCellByGroup(table, this.resultGroups.get(iter), this.resultGroups.get(iter).getResultStatus().size());
                }
            }catch (Exception e) {
                LoggerInstance.getInstance().printStackTrace(e);
            }
        }   */
        for (int iter = 0, max = this.resultGroups.size(); iter < max; iter++) {
            try {
                createCellByGroup(table, this.resultGroups.get(iter), this.resultGroups.get(iter).getResultStatus().size());
            } catch (Exception e) {
            }
        }

        // Нижние заголовки таблицы
            /*setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, "ID");
            setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, "ID Тип");
            setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, "Тип");
            setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, "ID Статус");
            setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, "Статус");
            setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, "Дата время");
            setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, "Тип", ColorConstants.ORANGE);
            setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, "ID Статус", ColorConstants.ORANGE);
            setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, "Статус", ColorConstants.ORANGE);*/
        for (String header : this.type.expected) {
            if (!header.isEmpty()) {
                setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, header);
            }
        }
        for (String header : this.type.received) {
            if (!header.isEmpty()) {
                setHeaderCellAttr(2, 1, FONT_SIZE_SMALL, table, header, ColorConstants.ORANGE);
            }
        }

        document.add(table);
        document.close();

        outputStream.close();

        return outputStream.toByteArray();
    }

    /**
     * Установить атрибуты ячеек заголовка таблицы
     *
     * @param FONT_SIZE_SMALL Размер шрифта
     * @param table           Объект таблицы
     * @param label           Текст в ячейке
     */
    private void setHeaderCellAttr(int rowSpan, int colSpan, int FONT_SIZE_SMALL, Table table, String label) {
        setHeaderCellAttr(rowSpan, colSpan, FONT_SIZE_SMALL, table, label, ColorConstants.LIGHT_GRAY);
    }

    /**
     * Установить атрибуты ячеек заголовка таблицы
     *
     * @param FONT_SIZE_SMALL Размер шрифта
     * @param table           Объект таблицы
     * @param label           Текст в ячейке
     * @param color           Цвет
     */
    private void setHeaderCellAttr(int rowSpan, int colSpan, int FONT_SIZE_SMALL, Table table, String label, Color color) {
        Cell cell = new Cell().setKeepTogether(true).setBackgroundColor(color).add(
                new Paragraph(label).setFontSize(FONT_SIZE_SMALL).setBold().setTextAlignment(TextAlignment.CENTER));
        table.addCell(cell);
    }

    /**
     * Метод добавляет строку с итоговой объединенной группой в переданную таблицу
     *
     * @param table - таблица, в которую надо добавить группу
     * @param group - итоговая объединенная группа
     * @param row   - количество статусов в группе
     */
    private void createCellByGroup(Table table, JsonMergedGroup group, int row) {
        //region Пример одной группы
        /*addCellToTable(table, "DOC_ID", row);
        addCellToTable(table, "DOC_TYP", row);
        addCellToTable(table, "DOC_TYP_NAME", row);
        addCellToTable(table, "DOC_STATUS_1", 1);
        addCellToTable(table, "DOC_STATUS_NAME_1", 1);
        addCellToTable(table, "DOC_STATUS_DATE_1", 1);
        addCellToTable(table, "DOC_TYP_NAME_EXP", row);
        addCellToTable(table, "DOC_STATUS_EXP_1", 1);
        addCellToTable(table, "DOC_STATUS_NAME_EXP_1", 1);

        for (int iter = 2; iter <= row; iter++) {
            addCellToTable(table, String.format("DOC_STATUS_%d", iter), 1);
            addCellToTable(table, String.format("DOC_STATUS_NAME_%d", iter), 1);
            addCellToTable(table, String.format("DOC_STATUS_DATE_%d", iter), 1);
            addCellToTable(table, String.format("DOC_STATUS_EXP_%d", iter), 1);
            addCellToTable(table, String.format("DOC_STATUS_NAME_EXP_%d", iter), 1);
        }*/
        //endregion
        // устанавливаем цвет для группы
        Color groupColor = group.isFalseBlock ? ColorConstants.RED : ColorConstants.GREEN;
        Color statusColor;

        // --------------- ДОБАВЛЯЕМ ПЕРВЫЙ СТАТУС ----------------------
        // добавляем поля
        addCellToTable(table, group.id, row, groupColor, TextAlignment.LEFT, !this.type.expected[0].isEmpty());
        addCellToTable(table, group.type, row, groupColor, TextAlignment.CENTER, !this.type.expected[1].isEmpty());
        addCellToTable(table, group.receivedTypeName, row, groupColor, TextAlignment.LEFT, !this.type.expected[2].isEmpty());

        // устанавливаем цвет для статусной строки
        statusColor = ((Boolean) group.getResultStatus().get(0).get(5)) ? ColorConstants.RED : ColorConstants.GREEN;

        addCellToTable(table, group.getResultStatus().get(0).get(0).toString(), 1, statusColor, TextAlignment.CENTER, !this.type.expected[3].isEmpty());
        addCellToTable(table, group.getResultStatus().get(0).get(1).toString(), 1, statusColor, TextAlignment.LEFT, !this.type.expected[4].isEmpty());
        addCellToTable(table, group.getResultStatus().get(0).get(2).toString(), 1, statusColor, TextAlignment.LEFT, !this.type.expected[5].isEmpty());
        addCellToTable(table, group.expectedTypeName, row, groupColor, TextAlignment.LEFT, !this.type.received[0].isEmpty());
        addCellToTable(table, group.getResultStatus().get(0).get(3).toString(), 1, statusColor, TextAlignment.CENTER, !this.type.received[1].isEmpty());
        addCellToTable(table, group.getResultStatus().get(0).get(4).toString(), 1, statusColor, TextAlignment.LEFT, !this.type.received[2].isEmpty());

        // ---------------- ДОБАВЛЯЕМ ОСТАЛЬНЫЕ СТАТУСЫ --------------------
        for (int iter = 1; iter < row; iter++) {
            statusColor = ((Boolean) group.getResultStatus().get(iter).get(5)) ? ColorConstants.RED : ColorConstants.GREEN;

            addCellToTable(table, group.getResultStatus().get(iter).get(0).toString(), 1, statusColor, TextAlignment.CENTER, !this.type.expected[3].isEmpty());
            addCellToTable(table, group.getResultStatus().get(iter).get(1).toString(), 1, statusColor, TextAlignment.LEFT, !this.type.expected[4].isEmpty());
            addCellToTable(table, group.getResultStatus().get(iter).get(2).toString(), 1, statusColor, TextAlignment.LEFT, !this.type.expected[5].isEmpty());
            addCellToTable(table, group.getResultStatus().get(iter).get(3).toString(), 1, statusColor, TextAlignment.CENTER, !this.type.received[1].isEmpty());
            addCellToTable(table, group.getResultStatus().get(iter).get(4).toString(), 1, statusColor, TextAlignment.LEFT, !this.type.received[2].isEmpty());
        }
    }

    /**
     * Метод добавляем текст в указанную таблицу по переданным внешним характеристикам
     *
     * @param table     - таблица, в которую нужно добавить строку
     * @param text      - добавляемый текст
     * @param row       - количество занимаемых строк
     * @param color     - цвет, в который нужно окрасить строку таблицы
     * @param alignment - сделать выравнивание ячейки
     */
    private void addCellToTable(Table table, String text, int row, Color color, TextAlignment alignment, boolean isShow) {
        if (isShow) {
            Cell cell = new Cell(row, 1).add(text).setBackgroundColor(color).setTextAlignment(alignment).setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
        }
    }

    private void turnFailOn() {
        this.isFail = true;
    }

    public boolean isFailed() {
        return this.isFail;
    }

    public enum DOCUMENT_TYPE {
        BASE(
                new String[]{
                        "ID",
                        "ID Тип",
                        "Тип",
                        "ID Статус",
                        "Статус",
                        "Дата время"
                },
                new String[]{
                        "Тип",
                        "ID Статус",
                        "Статус"
                }
        ),
        USPN_SKMV(
                new String[]{
                        "№ заявления",
                        "№",
                        "Этап",
                        "",
                        "Комментарий",
                        "Дата начала"
                },
                new String[]{
                        "Этап",
                        "",
                        "Комментарий"
                }
        );

        private String[] expected;
        private String[] received;
        private int length;
        private int lengthExpected;
        private int lengthReceived;

        DOCUMENT_TYPE(String[] expected, String[] received) {
            this.expected = expected;
            this.received = received;
            this.lengthExpected = 0;
            this.lengthReceived = 0;
            for (String header : this.expected) {
                if (!header.isEmpty()) {
                    this.lengthExpected++;
                }
            }
            for (String header : this.received) {
                if (!header.isEmpty()) {
                    this.lengthReceived++;
                }
            }
            this.length = this.lengthExpected + this.lengthReceived;
        }
    }
}
