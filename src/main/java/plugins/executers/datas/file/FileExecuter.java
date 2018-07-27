package plugins.executers.datas.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import modules.testExecutor.enums.StepStatus;
import modules.testExecutor.interfaces.CalledFromTest;
import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.RSTests;
import org.apache.commons.io.IOUtils;
import plugins.executers.datas.file.xml.conditions.TextCondition;
import plugins.executers.datas.file.xml.conditions.XmlXpathTextCondition;
import plugins.executers.datas.file.xml.wrapper.XmlWrapper;
import plugins.interfaces.TestExecutor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileExecuter implements TestExecutor {
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

    private String soapPacketdataXpath, soapPacketdataXpathValue;
    private List<TextCondition> xmlConditions = new LinkedList<>();

    @Override
    public String getPluginName() {
        return "Файл";
    }

    @Override
    public String getGroupName() {
        return "Данные";
    }

    @Override
    public ConcurrentHashMap<String, String> getAllStepsMap() {
        if (stepsMap.size() == 0) execute(null);
        return stepsMap;
    }

    @Override
    public ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getDefaultSettings() {
        return null;
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


    private byte[] extractFileFromZip(byte[] zipFile, String fileName) {
        byte[] result;
        for (int i = 0; i <= 100; i++) {
            result = extractFileFromZip(zipFile, fileName, i);
            if (result != null) return result;
        }
        return null;
    }

    private byte[] extractFileFromZip(byte[] zipFile, String fileName, int skipSeparatorIndex) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipFile));
        try {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();

                // === Удаляет из пути директории в количестве skipSeparator ===
                String[] entryNameArray;
                if (entryName.contains("/")) {
                    entryNameArray = entryName.split("/");
                } else {
                    entryNameArray = entryName.split("\\\\");
                }
                if (entryNameArray != null && entryNameArray.length > 0) {
                    int lengthName = 0;
                    for (int i = 0; i < entryNameArray.length - 1; i++) {
                        if (skipSeparatorIndex > 0 && i < skipSeparatorIndex) {
                            lengthName += entryNameArray[i].length() + 1;
                        } else {
                            break;
                        }
                    }
                    entryName = entryName.substring(lengthName, entryName.length());
                }
                // ======

                if (entryName.equals(fileName)) {
                    byte[] byteBuff = new byte[4096];
                    int bytesRead = 0;
                    while ((bytesRead = zipInputStream.read(byteBuff)) != -1) {
                        byteArrayOutputStream.write(byteBuff, 0, bytesRead);
                    }
                    break;
                }
                zipInputStream.closeEntry();
            }
            zipInputStream.close();
        } catch (Exception e) {
            //LoggerInstance.getInstance().printStackTrace(e);
            return null;
        }

        if (byteArrayOutputStream.toByteArray().length <= 0) {
            return null;
        } else {
            return byteArrayOutputStream.toByteArray();
        }
    }

    private Map<String, Object> JSONfromByteFileToHashMap(byte[] byteJSONFile) {
        if (byteJSONFile != null) {
            try {
                return new ObjectMapper().readValue(byteJSONFile, HashMap.class);
            } catch (Exception e) {
                logger.info(e.getMessage());
                return new HashMap<>();
            }
        } else {
            return new HashMap<>();
        }
    }

    private String getXMLwithoutArtifacts(String document) {
        String[] s = document.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : s) {
            if (line.contains("<") || line.contains(">")) {
                sb.append(line);
                sb.append("\n");
            }
        }
        boolean press = sb.toString().contains("<?xml");
        String cXml = "";
        String[] xmlFileArray = sb.toString().split("<?xml ");

        if (press) cXml = "<?xml ";
        if (xmlFileArray.length > 1) {
            return cXml + xmlFileArray[xmlFileArray.length - 1];
        } else {
            return cXml + xmlFileArray[0];
        }
    }

    private ArrayList<byte[]> getUnZIP(byte[] zipFile) throws IOException {
        ArrayList<byte[]> fileListArray = new ArrayList<>();

        ByteArrayOutputStream byteArrayOutputStream;
        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipFile));
        ZipEntry entry = null;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            String entryName = entry.getName();
            byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] byteBuff = new byte[4096];
            int bytesRead = 0;
            while ((bytesRead = zipInputStream.read(byteBuff)) != -1) {
                byteArrayOutputStream.write(byteBuff, 0, bytesRead);
            }

            if (byteArrayOutputStream.toByteArray().length > 0) {
                fileListArray.add(byteArrayOutputStream.toByteArray());
            }
        }

        zipInputStream.closeEntry();
        zipInputStream.close();
        return fileListArray;
    }

    private byte[] getMessageXML(byte[] ZIPbyteFile, List<TextCondition> xmlConditions) throws IOException {
        ArrayList<byte[]> extractByteFileArray = getUnZIP(ZIPbyteFile);

        for (byte[] file : extractByteFileArray) {
            boolean match = true;
            file = getXMLwithoutArtifacts(new String(file, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
            for (TextCondition condition : xmlConditions) {
                if (!condition.match(file)) {
                    match = false;      // Если не выполнено условие, то флаг сбросить
                    break;      // Достаточно одного невыполненного условия, остальные проверять нет смысла
                }
            }
            if (match) {
                return file;
            }
        }
        return null;
    }

    @Override
    public void execute(RSTests step) {
        String name, description, stepName = "";
        boolean mapEmpty = stepsMap.size() == 0;
        if (step != null) stepName = step.getStep();

        name = "Сохранить содержимое файла в переменную";
        if (mapEmpty) {
            description = "Выполняется чтение файла из указанной переменной и сохранение содержимого в указанную переменную. Пример: Шаг = Сохранить содержимое файла в переменную; Ключ = Имя переменной для результата; Значение = Имя переменной содержащая файл;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            toSaveContentsOfFileIntoVariable(step);
            return;
        }

        name = "Извлечь из ZIP-файла файл";
        if (mapEmpty) {
            description = "Выполняется извлечение указанного файла из указанного ZIP-файла. Сохранение содержимого в указанную переменную (имя извлекаемого файла). Пример: Шаг = Извлечь из ZIP-файла файл; Ключ = Имя извлекаемого файла; Значение = ZIP-файл;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            extractFromZIPfile(step);
            return;
        }

        name = "Извлечь из Property-файла значение";
        if (mapEmpty) {
            description = "Выполняется извлечение указанного значения из указанного Property-файла. Сохранение полученного значения в указанную переменную (имя извлекаемого значения). Пример: Шаг = Извлечь из Property-файла значение; Ключ = Имя извлекаемого значения; Значение = Property-файл;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            extractFromPropertyFileValue(step);
            return;
        }

        name = "Из JSON файла сохранить значение";
        if (mapEmpty) {
            description = "Выполняется извлечение указанного значения из указанного JSON-файла. Сохранение полученного значения в указанную переменную (имя извлекаемого значения). Пример: Шаг = Из JSON файла сохранить значение; Ключ = Имя извлекаемого значения; Значение = JSON-файл;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            getValueFromJsonFile(step);
            return;
        }

        name = "Задать XPath для поиска файла в XML документе";
        if (mapEmpty) {
            description = "Сохраняет впамяти переданный селектор. Пример: Шаг = Задать XPath для поиска файла в XML документе; Значение = Селектор;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setXPathFormZIPinXML(step);
            return;
        }

        name = "Из XML документа сохранить ZIP архив в";
        if (mapEmpty) {
            description = "По ранее заданному селектору выполянется поиск файла в документе. Результат сохраняется в указанную переменную (имя файла). Пример: Шаг = Из XML документа сохранить ZIP архив в; Ключ = имя переменной/файла; Значение = Переменная содержащая XML-документ;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            getZipFormXML(step);
            return;
        }

        name = "Задать XPath для поиска значения в XML документе";
        if (mapEmpty) {
            description = "Сохраняет в памяти селектор для поиска в XML-документе. Пример: Шаг = Задать XPath для поиска значения в XML документе; Значение = Селектор;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setXPathFormValueinXML(step);
            return;
        }

        name = "Из XML документа по ранее заданному XPath получить значение в переменную";
        if (mapEmpty) {
            description = "В указанном файле выполняется поиск значения по ранее заданному селектору. Результат сохранить в указанную переменную. Пример: Шаг = Из XML документа по ранее заданному XPath получить значение в переменную; Ключ = имя переменной; Значение = Переменная содержащая XML-документ;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            getValueFormXMLtoXpath(step);
            return;
        }

        name = "Из ZIP-архива с XML документами по ранее заданному XPath сравнить значение каждого файла с";
        if (mapEmpty) {
            description = "В указанном файле выполняется перебор каждого файла и поиск вних значения по ранее заданному селектору. Значение сравнивается с указанным значением. Пример: Шаг = Из ZIP-архива с XML документами по ранее заданному XPath сравнить значение каждого файла с; Ключ = Сравниваемое значение; Значение = Переменная содержащая ZIP-файл;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            checkValueFormAllFileXMLtoXpath(step);
            return;
        }

        name = "В файле удалить строку номер";
        if (mapEmpty) {
            description = "В указанном файле выполняется удаление строки с указанным номером. Пример: Шаг = В файле удалить строку номер; Ключ = Номер строки; Значение = Переменная содержащая файл;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            deleteLineFromFile(step);
            return;
        }

        name = "Для поиска XML файла в ZIP-архиве добавить искомое значение по XPath селектору";
        if (mapEmpty) {
            description = "В память сохраняется селектор и искомое значение. Пример: Шаг = Для поиска XML файла в ZIP-архиве добавить искомое значение по XPath селектору; Ключ = Селектор; Значение = Искомое значение;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            setZIPMessageXMLXPath(step);
            return;
        }

        name = "Для поиска XML файла в ZIP-архиве удалить все XPath селекторы";
        if (mapEmpty) {
            description = "Из памяти удалит селекторы с искомыми значениями (Для поиска XML файла в ZIP-архиве). Пример: Шаг = Для поиска XML файла в ZIP-архиве удалить все XPath селекторы;";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            cleanZIPXMLXPathSelectors(step);
            return;
        }

        name = "В ZIP-архиве найти XML файл и сохранить его в переменную";
        if (mapEmpty) {
            description = "По ранее заданным селекторам выполняется поиск XML-документа в заданном файле. Содержимое найденного файла сохраняется в указанную переменную. Пример: Шаг = В ZIP-архиве найти XML файл и сохранить его в переменную; Ключ = имя переменной/файла; Значение = Переменная содержащая ZIP-файл";
            stepsMap.put(name, description);
        }
        if (name.equals(stepName)) {
            getZIPMessageXMLNotValue(step);
            return;
        }
    }

    private void getZIPMessageXMLNotValue(RSTests step) {
        byte[] byteFileInput = byteDataMap.get(step.getValue());
        if (byteFileInput == null) {
            step.setErrorMessage("Отсутсвует файл " + step.getValue());
            step.setStatus(StepStatus.FAILURE);
        } else {
            byte[] byteFile = new byte[0];
            try {
                byteFile = getMessageXML(byteFileInput, xmlConditions);
                if (byteFile == null) {
                    step.setErrorMessage("Искомый файл не найден");
                    step.setStatus(StepStatus.FAILURE);
                } else {
                    byteDataMap.put(step.getKey(), byteFile);
                    step.getAttachments().put(step.getKey(), byteFile);
                }
            } catch (IOException e) {
                step.setErrorMessage("Неудалось прочитать архив " + step.getValue());
                step.setStatus(StepStatus.FAILURE);
            }
        }
    }

    private void cleanZIPXMLXPathSelectors(RSTests step) {
        xmlConditions.clear();
    }

    private void deleteLineFromFile(RSTests step) {
        byte[] byteFile = byteDataMap.get(step.getValue());
        if (byteFile == null) {
            step.setErrorMessage("Отсутсвует файл " + step.getValue());
            step.setStatus(StepStatus.FAILURE);
        } else {

            String[] s = new String(byteFile, StandardCharsets.UTF_8).split("\n");
            StringBuilder sb = new StringBuilder();
            int x = 0;
            for (String line : s) {
                x++;
                if (x != Integer.parseInt(step.getKey())) {
                    sb.append(line);
                }
            }
            byteDataMap.put(step.getValue(), sb.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    private void checkValueFormAllFileXMLtoXpath(RSTests step) {
        byte[] bt = byteDataMap.get(step.getValue());
        if (bt == null) {
            step.setErrorMessage("Отсутсвует файл " + step.getValue());
            step.setStatus(StepStatus.FAILURE);
        } else {

            try {
                ArrayList<byte[]> fileList = getUnZIP(bt);

                if (fileList != null && fileList.size() > 0) {
                    for (byte[] byteFile : fileList) {
                        try {
                            String[] xmlFileArray = new String(byteFile, StandardCharsets.UTF_8).split("<?xml");

                            XmlWrapper xmlPacketWrapper;
                            if (xmlFileArray.length > 1) {
                                xmlPacketWrapper = new XmlWrapper(getXMLwithoutArtifacts("<?xml" + xmlFileArray[1]));
                            } else {
                                xmlPacketWrapper = new XmlWrapper(getXMLwithoutArtifacts(xmlFileArray[0]));
                            }

                            String valueArchive = xmlPacketWrapper.readValue(soapPacketdataXpathValue);
                            if (valueArchive != null && valueArchive.equals(step.getKey())) {

                            } else {
                                throw new Exception();
                            }
                        } catch (Exception e) {
                            step.setErrorMessage("Значение в файлах не совпадает либо отсутсвует");
                            step.setStatus(StepStatus.FAILURE);
                            step.getAttachments().put("Проверяемый файл.xml", byteFile);
                        }
                    }
                } else {
                    step.setErrorMessage("Отсутсвуют файлы в архиве");
                    step.setStatus(StepStatus.FAILURE);
                }
            } catch (IOException e) {
                step.setErrorMessage("Неудалось прочитать zip " + step.getValue());
                step.setStatus(StepStatus.FAILURE);
            }
        }
    }

    private void getValueFormXMLtoXpath(RSTests step) {
        byte[] bt = byteDataMap.get(step.getValue());
        if (bt == null) {
            step.setErrorMessage("Отсутсвует файл " + step.getValue());
            step.setStatus(StepStatus.FAILURE);
        } else {
            XmlWrapper xmlPacketWrapper = new XmlWrapper(getXMLwithoutArtifacts(new String(bt, StandardCharsets.UTF_8)));
            String valueArchive = xmlPacketWrapper.readValue(soapPacketdataXpathValue);
            stringDataMap.put(step.getKey(), valueArchive);
            step.getAttachments().put(step.getKey(), valueArchive.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void getZipFormXML(RSTests step) {
        byte[] bt = byteDataMap.get(step.getValue());
        if (bt == null) {
            step.setErrorMessage("Отсутсвует файл " + step.getValue());
            step.setStatus(StepStatus.FAILURE);
        } else {
            XmlWrapper xmlPacketWrapper = new XmlWrapper(bt);

            String b64ZipArchive = xmlPacketWrapper.readValue(soapPacketdataXpath);
            byte[] zipArchive = Base64.getDecoder().decode(b64ZipArchive);
            byteDataMap.put(step.getKey(), zipArchive);
            step.getAttachments().put(step.getKey(), zipArchive);
        }
    }

    private void getValueFromJsonFile(RSTests step) {
        String str = null;
        byte[] bt = byteDataMap.get(step.getValue());
        if (bt == null && stringDataMap.get(step.getValue()) != null) {
            bt = stringDataMap.get(step.getValue()).getBytes(StandardCharsets.UTF_8);
        }
        if (bt == null) {
            File f = new File(step.getValue()) ;
            if(f.exists()) {
                try {
                    FileInputStream inputStream = new FileInputStream(step.getValue());
                    try {
                        byte[] byt = IOUtils.toByteArray(inputStream);
                        Map<String, Object> map = JSONfromByteFileToHashMap(byt);
                        str = map.get(step.getKey()).toString();
                    } finally {
                        inputStream.close();
                    }
                }catch (Exception e) {
                    step.setErrorMessage("Нейдалось прочитать файл " + step.getValue() + " Error: "+e.getMessage());
                    step.setStatus(StepStatus.FAILURE);
                }
            } else {
                step.setErrorMessage("Отсутсвует файл " + step.getValue());
                step.setStatus(StepStatus.FAILURE);
            }

        } else {
            Map<String, Object> map = JSONfromByteFileToHashMap(bt);
            str = map.get(step.getKey()).toString();
        }
        if (str == null) {
            step.setErrorMessage("В файле отсутсвует значения " + step.getKey());
            step.setStatus(StepStatus.FAILURE);
        } else {
            stringDataMap.put(step.getKey(), str);
        }
    }

    private void extractFromPropertyFileValue(RSTests step) {
        String str;
        Properties property;
        byte[] bt = byteDataMap.get(step.getValue());

        if (bt == null) {
            File f = new File(step.getValue());
            if (f.exists()) {
                try {
                    FileInputStream inputStream = new FileInputStream(step.getValue());
                    try {
                        bt = IOUtils.toByteArray(inputStream);
                    } finally {
                        inputStream.close();
                    }
                } catch (Exception e) {
                    step.setErrorMessage("Нейдалось прочитать файл " + step.getValue() + " Error: " + e.getMessage());
                    step.setStatus(StepStatus.FAILURE);
                }
            }
        }

        if (bt == null) {
            step.setErrorMessage("Отсутсвует файл " + step.getValue());
            step.setStatus(StepStatus.FAILURE);
        } else {

            property = new Properties();

            try {
                ByteArrayInputStream input = new ByteArrayInputStream(bt);
                property.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            } catch (IOException e) {
                logger.info(e.getMessage());
            }

            str = property.getProperty(step.getKey());

            if (str == null) {
                step.setErrorMessage("В файле отсутсвует значение " + step.getKey());
                step.setStatus(StepStatus.FAILURE);
            } else {
                stringDataMap.put(step.getKey(), str);
            }
        }
    }

    private void extractFromZIPfile(RSTests step) {
        byte[] zipByteFile = byteDataMap.get(step.getValue());
        if (zipByteFile == null) {
            step.setErrorMessage("Отсутсвует файл " + step.getValue());
            step.setStatus(StepStatus.FAILURE);
        } else {
            byte[] extractByteFile = extractFileFromZip(zipByteFile, step.getKey());

            if (extractByteFile == null) {
                step.setErrorMessage("Файл не существует " + step.getKey());
                step.setStatus(StepStatus.FAILURE);
            } else {
                step.getAttachments().put(step.getKey(), extractByteFile);
                byteDataMap.put(step.getKey(), extractByteFile);
            }
        }
    }

    private void toSaveContentsOfFileIntoVariable(RSTests step) {
        if (byteDataMap.get(step.getValue()) == null) {
            File f = new File(step.getValue()) ;
            if(f.exists()) {
                try {
                    FileInputStream inputStream = new FileInputStream(step.getValue());
                    try {
                        byte[] byteFile = IOUtils.toByteArray(inputStream);
                        stringDataMap.put(step.getKey(), new String(byteFile, StandardCharsets.UTF_8));
                    } finally {
                        inputStream.close();
                    }
                }catch (Exception e) {
                    step.setErrorMessage("Нейдалось прочитать файл " + step.getValue() + " Error: "+e.getMessage());
                    step.setStatus(StepStatus.FAILURE);
                }
            } else {
                step.setErrorMessage("Отсутсвует файл " + step.getValue());
                step.setStatus(StepStatus.FAILURE);
            }
        } else {
            byte[] byteFile = byteDataMap.get(step.getValue());
            if (byteFile == null) {
                step.setErrorMessage("Файл не существует " + step.getValue());
                step.setStatus(StepStatus.FAILURE);
            } else {
                stringDataMap.put(step.getKey(), new String(byteFile, StandardCharsets.UTF_8));
            }
        }
    }

    public void setXPathFormZIPinXML(RSTests step) {
        this.soapPacketdataXpath = step.getValue();
    }

    public void setXPathFormValueinXML(RSTests step) {
        this.soapPacketdataXpathValue = step.getValue();
    }

    public void setZIPMessageXMLXPath(RSTests step) {
        xmlConditions.add(new XmlXpathTextCondition(step.getKey(), step.getValue()));
    }
}
