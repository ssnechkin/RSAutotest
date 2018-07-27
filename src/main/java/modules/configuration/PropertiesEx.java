package modules.configuration;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 * Класс, расширяющий функциональность стандартного класса Properties.
 *
 * @author stanislav.bakharev
 */
public class PropertiesEx extends Properties {

    /**
     * Он так хотел.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Кодировка по умолчанию
     */
    private static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;
    /**
     * Исходный файл nio
     */
    private Path srcPath;
    /**
     * Исходная кодировка
     */
    private Charset srcCharset = DEFAULT_ENCODING;

    /**
     * Конструктор
     */
    public PropertiesEx() {
        super();
    }

    /**
     * Конструктор, загружающий содержимое из файла в указанной кодировке. IO
     *
     * @param file    Файл.
     * @param charset Кодировка.
     * @throws IOException Ошибка чтения файла
     */
    public PropertiesEx(File file, Charset charset) throws IOException {
        this(file.toPath(), charset);
    }

    /**
     * Конструктор, загружающий содержимое из файла в указанной кодировке. NIO
     *
     * @param path    Путь к файлу.
     * @param charset Кодировка.
     * @throws IOException
     */
    public PropertiesEx(Path path, Charset charset) throws IOException {
        super();
        srcPath = path;
        srcCharset = charset;
        Reader reader = Files.newBufferedReader(path, charset);
        load(reader);
    }

    /**
     * Конструктор, загружающий содержимое из файла в кодировке UTF-8. IO
     *
     * @param file Файл.
     * @throws IOException
     */
    public PropertiesEx(File file) throws IOException {
        this(file.toPath());
    }

    /**
     * Конструктор, загружающий содержимое из файла в кодировке UTF-8. NIO
     *
     * @param path Путь к файлу.
     * @throws IOException
     */
    public PropertiesEx(Path path) throws IOException {
        this(path, DEFAULT_ENCODING);
    }

    /**
     * Перезаписать содержимое
     *
     * @throws IOException
     */
    public void save() throws IOException {
        Writer writer = Files.newBufferedWriter(srcPath, srcCharset);
        store(writer, "This file has been generated automatically");
    }

    public void save(StringBuilder comments) throws IOException {
        Writer writer = Files.newBufferedWriter(srcPath, srcCharset);
        store(writer, comments.toString());
    }

    /**
     * Получить массив байт
     *
     * @throws IOException
     */
    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(out, srcCharset);
        store(writer, "This file has been generated automatically");
        return out.toByteArray();
    }

    public Enumeration keys() {
        Enumeration keysEnum = super.keys();
        Vector<String> keyList = new Vector<String>();
        while (keysEnum.hasMoreElements()) {
            keyList.add((String) keysEnum.nextElement());
        }
        Collections.sort(keyList);
        return keyList.elements();
    }
}
