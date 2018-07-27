/**
 * @author sergey.nechkin
 *
 */
package modules.logger;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Класс для очистки формата строки вывода в консоль
 *
 * @author nechkin.sergei.sergeevich
 */
public class LoggerFormatterEmpty extends Formatter {

	@Override
	public String format(LogRecord record) {
	    return "";
	}
}
