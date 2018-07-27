package plugins.dataHandlers;

import modules.testExecutor.interfaces.SuiteDatas;
import modules.testExecutor.interfaces.TestDatas;
import modules.testExecutor.templates.RSTests;
import org.joda.time.DateTime;
import plugins.interfaces.DataHandler;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author nechkin.sergei.sergeevich
 * Класс для обработки строк
 */
public class StringParsers implements DataHandler{
    ConcurrentHashMap<String, String> stringDataMap;
    private SuiteDatas suite;
    private Logger logger;

    @Override
    public String getPluginName() {
        return "Заменитель строк данными";
    }

    @Override
    public ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getDefaultSettings() {
        return null;
    }

    @Override
    public void set(String programFilesDirectory, SuiteDatas suite, TestDatas test, ConcurrentHashMap<String, String> stringDataMap, ConcurrentHashMap<String, byte[]> byteDataMap, ConcurrentHashMap<String, String> settings) {
        this.suite = suite;
        this.stringDataMap = stringDataMap;
        this.logger = test.getLogger();
    }

    @Override
    public void processing(RSTests step) {
        String parseString = step.getStep();
        if (parseString != null) {
            parseString = parseDateTime(parseString);
            parseString = parseCurrentDateTime(parseString);
            parseString = replaceMap2(parseString);
            step.setStep(parseString);
        }

        parseString = step.getKey();
        if (parseString != null) {
            parseString = parseDateTime(parseString);
            parseString = parseCurrentDateTime(parseString);
            parseString = replaceMap2(parseString);
            step.setKey(parseString);
        }

        parseString = step.getValue();
        if (parseString != null) {
            parseString = parseDateTime(parseString);
            parseString = parseCurrentDateTime(parseString);
            parseString = replaceMap2(parseString);
            step.setValue(parseString);
        }
    }

    @Override
    public void close() {
    }

    //Заменяет стороку "Datetime: -1d" на текущую дату минус 1 день
    private String parseDateTime(String value) {
        long currentTime = System.currentTimeMillis();
        Integer ratio = 0;
        DateTime dateTime = new DateTime(currentTime);

        if (value.toString().indexOf("Datetime: ") >= 0) {
            try {
                String[] timeSegment = value.replaceAll("Datetime: ", "").split(" ");
                for (int x = 0; x < timeSegment.length; x++) {
                    ratio = Integer.parseInt(timeSegment[x].replaceAll("[^0-9]", ""));

                    if (timeSegment[x].substring(0, 1).equals("-")) {
                        switch (timeSegment[x].substring(timeSegment[x].length() - 1, timeSegment[x].length())) {
                            case "s":
                                dateTime = new DateTime(dateTime.minusSeconds(ratio).getMillis());
                                break;
                            case "m":
                                dateTime = new DateTime(dateTime.minusMinutes(ratio).getMillis());
                                break;
                            case "h":
                                dateTime = new DateTime(dateTime.minusHours(ratio).getMillis());
                                break;
                            case "D":
                                dateTime = new DateTime(dateTime.minusDays(ratio).getMillis());
                                break;
                            case "M":
                                dateTime = new DateTime(dateTime.minusMonths(ratio).getMillis());
                                break;
                            case "Y":
                                dateTime = new DateTime(dateTime.minusYears(ratio).getMillis());
                                break;
                            case "S":
                                dateTime = new DateTime(dateTime.minusSeconds(ratio).getMillis());
                                break;
                            case "H":
                                dateTime = new DateTime(dateTime.minusHours(ratio).getMillis());
                                break;
                            case "d":
                                dateTime = new DateTime(dateTime.minusDays(ratio).getMillis());
                                break;
                            case "y":
                                dateTime = new DateTime(dateTime.minusYears(ratio).getMillis());
                                break;
                        }
                    } else {
                        switch (timeSegment[x].substring(timeSegment[x].length() - 1, timeSegment[x].length())) {
                            case "s":
                                dateTime = new DateTime(dateTime.plusSeconds(ratio).getMillis());
                                break;
                            case "m":
                                dateTime = new DateTime(dateTime.plusMinutes(ratio).getMillis());
                                break;
                            case "h":
                                dateTime = new DateTime(dateTime.plusHours(ratio).getMillis());
                                break;
                            case "D":
                                dateTime = new DateTime(dateTime.plusDays(ratio).getMillis());
                                break;
                            case "M":
                                dateTime = new DateTime(dateTime.plusMonths(ratio).getMillis());
                                break;
                            case "Y":
                                dateTime = new DateTime(dateTime.plusYears(ratio).getMillis());
                                break;
                            case "S":
                                dateTime = new DateTime(dateTime.plusSeconds(ratio).getMillis());
                                break;
                            case "H":
                                dateTime = new DateTime(dateTime.plusHours(ratio).getMillis());
                                break;
                            case "d":
                                dateTime = new DateTime(dateTime.plusDays(ratio).getMillis());
                                break;
                            case "y":
                                dateTime = new DateTime(dateTime.plusYears(ratio).getMillis());
                                break;
                        }
                    }
                }
                return new SimpleDateFormat("dd.MM.yyyy H:mm:ss").format(dateTime.getMillis());
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        }
        return value;
    }

    //Заменяет CurrentDateTime на текущую дату. Формат может указыватся в скобках CurrentDateTime(dd/MM/yyyy H:mm:ss)
    private String parseCurrentDateTime(String value) {
        long currentTime = System.currentTimeMillis();
        DateTime dateTime = new DateTime(currentTime);
        String fullSting = "";
        String format = "";

        if (value.toString().indexOf("CurrentDateTime(") >= 0) {
            fullSting = value.substring(value.toString().indexOf("CurrentDateTime("), value.length());
            fullSting = fullSting.substring(0, fullSting.indexOf(")") + 1);
            format = fullSting.substring(fullSting.indexOf("(") + 1, fullSting.indexOf(")"));
            value = value.replace(fullSting, new SimpleDateFormat(format).format(dateTime.getMillis()));
        }

        if (value.toString().indexOf("CurrentDateTime") >= 0) {
            value = value.replaceAll("CurrentDateTime", new SimpleDateFormat("dd/MM/yyyy H:mm:ss").format(dateTime.getMillis()));
        }

        return value;
    }

    private String replaceMap2(String stringData) {
        boolean replacePress = true;

        while (replacePress) {
            replacePress = false;
            if (stringDataMap != null) {
                if (stringDataMap.size() > 0) {
                    for (Map.Entry<String, String> entry : stringDataMap.entrySet()) {
                        if (stringData.contains("{" + entry.getKey().replace(suite.getName(), "") + "}")) {
                            replacePress = true;
                            stringData = stringData.replace("{" + entry.getKey().replace(suite.getName(), "") + "}", entry.getValue().toString());
                        }
                    }
                }
            }
            if (suite.getStringDataMap() != null) {
                if (suite.getStringDataMap().size() > 0) {
                    for (Map.Entry<String, String> entry : suite.getStringDataMap().entrySet()) {
                        if (stringData.contains("{" + entry.getKey().replace(suite.getName(), "") + "}")) {
                            replacePress = true;
                            stringData = stringData.replace("{" + entry.getKey().replace(suite.getName(), "") + "}", entry.getValue().toString());
                        }
                    }
                }
            }
        }

        return stringData;
    }

}
