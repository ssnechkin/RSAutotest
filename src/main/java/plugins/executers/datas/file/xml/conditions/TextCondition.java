package plugins.executers.datas.file.xml.conditions;

import java.nio.charset.Charset;

/**
 * @author platov.nikolay
 * Интерфейс сравнения текста
 */
public interface TextCondition {
    /**
     * @param text проверяемый текст
     * @return удовлетворяет ли переданный текст условию
     */
    boolean match(String text);

    /**
     * @param text проверяемый текст
     * @return удовлетворяет ли переданный текст условию
     */
    boolean match(byte[] text);

    /**
     * @param text    проверяемый текст
     * @param charset кодировка
     * @return удовлетворяет ли переданный текст условию
     */
    boolean match(byte[] text, Charset charset);

    String description();
}
