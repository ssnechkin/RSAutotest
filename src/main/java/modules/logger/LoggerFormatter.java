/**
 * @author sergey.nechkin
 */
package modules.logger;

import modules.logger.interfaces.RSFormatter;

import java.text.SimpleDateFormat;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.LogRecord;

/**
 * Класс для формирования строки события
 *
 * @author nechkin.sergei.sergeevich
 */
public class LoggerFormatter extends java.util.logging.Formatter implements RSFormatter {
    private CopyOnWriteArrayList<String> logHistory = new CopyOnWriteArrayList<>();
    private long threadID = 0;
    private long suiteID = 0;
    private long testID = 0;

    @Override
    public String format(LogRecord record) {
        StringBuffer buffer = new StringBuffer(50);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY.MM.dd HH:mm:ss:S");
        long sequenceNumber = record.getSequenceNumber();

        buffer.append(sequenceNumber);
        if ((sequenceNumber + "").length() == 1) buffer.append("      ");
        if ((sequenceNumber + "").length() == 2) buffer.append("     ");
        if ((sequenceNumber + "").length() == 3) buffer.append("    ");
        if ((sequenceNumber + "").length() == 4) buffer.append("   ");
        if ((sequenceNumber + "").length() == 5) buffer.append("  ");
        if ((sequenceNumber + "").length() == 6) buffer.append(" ");

        String s = simpleDateFormat.format(record.getMillis());
        if (s.length() == 21) s += "  ";
        if (s.length() == 22) s += " ";
        buffer.append(s);

        if (threadID > 0) {
            buffer.append(" Request: ");
            buffer.append(threadID);
            if ((threadID + "").length() == 1) buffer.append("   ");
            if ((threadID + "").length() == 2) buffer.append("  ");
            if ((threadID + "").length() == 3) buffer.append(" ");
        }
        if (suiteID > 0) {
            buffer.append(" Suite: ");
            buffer.append(suiteID);
            if ((suiteID + "").length() == 1) buffer.append("   ");
            if ((suiteID + "").length() == 2) buffer.append("  ");
            if ((suiteID + "").length() == 3) buffer.append(" ");
        }
        if (testID > 0) {
            buffer.append(" Test: ");
            buffer.append(testID);
            if ((testID + "").length() == 1) buffer.append("    ");
            if ((testID + "").length() == 2) buffer.append("   ");
            if ((testID + "").length() == 3) buffer.append("  ");
            if ((testID + "").length() == 4) buffer.append(" ");
        }
        buffer.append(" ");
        buffer.append(record.getMessage());
        buffer.append("\r\n");

        if (logHistory != null) {
            logHistory.add(buffer.toString());
        }
        return buffer.toString();
    }

    @Override
    public java.util.logging.Formatter getFormatter() {
        return this;
    }

    @Override
    public void setThreadID(Number threadID) {
        this.threadID = (long) threadID;
    }

    @Override
    public void setSuiteID(Number suiteID) {
        this.suiteID = (long) suiteID;
    }

    @Override
    public void setTestID(Number testID) {
        this.testID = (long) testID;
    }

    @Override
    public void setLogHistory(CopyOnWriteArrayList<String> logHistory) {
        this.logHistory = new CopyOnWriteArrayList<>();
        this.logHistory.addAll(logHistory);
    }
}
