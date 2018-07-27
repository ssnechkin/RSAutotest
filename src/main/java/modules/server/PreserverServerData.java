package modules.server;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import modules.logger.interfaces.RSLogger;
import modules.testExecutor.templates.RSTests;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Класс хранения данных сервера
 *
 * @author nechkin.sergei.sergeevich
 */
public class PreserverServerData {
    private String folderWithEditableFiles = "edit_web_files";
    private ConcurrentHashMap<String, RSTests> filesWithTests = new ConcurrentHashMap<>(); // <Имя файла, тесты из файла>
    private RSLogger rsLogger;

    public PreserverServerData(RSLogger rsLogger) {
        this.rsLogger = rsLogger;
    }

    public RSLogger getRsLogger() {
        return rsLogger;
    }

    public void readFiles(String folderWithEditableFiles) {

        this.folderWithEditableFiles = folderWithEditableFiles;
        File folder = new File(folderWithEditableFiles);

        // маска для поиска файлов в папке
        final String[] mask = {".json"};

        // Создать директорию для редактируемых файлов в Web-редакторе
        if (!folder.exists()) folder.mkdir();

        // Получить список файлов из директории dirName
        String[] list = folder.list(new FilenameFilter() {
            @Override
            public boolean accept(File folder, String name) {
                for (String s : mask)
                    if (name.toLowerCase().endsWith(s)) return true;
                return false;
            }
        });

        // Загрузить файлы из списка в массив
        for (String fileName : list)
            addFileNameTests(fileName);
    }

    public ConcurrentHashMap<String, RSTests> getFilesWithTests() {
        return filesWithTests;
    }

    /**
     * Добавляет файл в массив. Файлы должны находится в директории folderWithEditableFiles
     *
     * @param fileName Имя файла
     */
    private void addFileNameTests(String fileName) {
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(folderWithEditableFiles + File.separator + fileName), "UTF-8"));
            RSTests rsTests = new Gson().fromJson(reader, RSTests.class);
            this.filesWithTests.put(fileName, rsTests);
            reader.close();
        } catch (Exception e) {
            rsLogger.printStackTrace(e);
        }
    }

    /**
     * Добавляет файл в массив
     *
     * @param fileName  Имя файла
     * @param jsonTests содержимое файла
     */
    public void addFileNameTests(String fileName, String jsonTests) {
        try {
            Gson gson = new Gson();
            RSTests rsTests = gson.fromJson(jsonTests, RSTests.class);
            this.filesWithTests.put(fileName, rsTests);
        } catch (Exception e) {
            rsLogger.printStackTrace(e);
        }
    }

    /**
     * Возвращает файл из карты
     *
     * @param fileNameKey Имя файла
     * @return содержимое файла
     */
    public RSTests getRSTests(String fileNameKey) {
        if (fileNameKey == null) return null;
        return this.filesWithTests.get(fileNameKey);
    }

    /**
     * Возвращает список загруженных файлов
     *
     * @return спсиок с именеами файлов
     */
    public CopyOnWriteArrayList<String> getFileList() {
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        for (Map.Entry<String, RSTests> entry : filesWithTests.entrySet()) {
            list.add(entry.getKey());
        }
        return list;
    }

    /**
     * Возвращает список загруженных файлов
     *
     * @return спсиок с именеами файлов
     */
    public void updateFileInDisk(String fileNameKey) {
        if (fileNameKey != null) {
            try {
                String json = new Gson().toJson(filesWithTests.get(fileNameKey));
                try (PrintWriter out = new PrintWriter(folderWithEditableFiles + File.separator + fileNameKey, "UTF-8")) {
                    out.println(json);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Удаляет файл из массива и с диска
     *
     * @param fileName Имя файла
     */
    public void deleteFileNameTests(String fileName) {
        new File(folderWithEditableFiles + File.separator + fileName).delete();
        this.filesWithTests.remove(fileName);
    }

    /**
     * Возвращает переданные данные запросом
     *
     * @param request        полученный запрос сервером
     * @param servletContext содержимое запроса
     * @return карта с полученными данными <ключ, значение>
     */
    public ConcurrentHashMap<String, String> getRequestData(HttpServletRequest request, ServletContext servletContext) {
        ConcurrentHashMap<String, String> resultDataMap = new ConcurrentHashMap<>();
        DiskFileItemFactory fileFactory = new DiskFileItemFactory();
        File filesDir = (File) servletContext.getAttribute("FILES_DIR_FILE");
        List<FileItem> fileItemsList;

        try {
            fileFactory.setRepository(filesDir);
            fileItemsList = new ServletFileUpload(fileFactory).parseRequest(request);

            for (FileItem fileItem : fileItemsList) {
                InputStream in = fileItem.getInputStream();
                ByteArrayOutputStream _out = new ByteArrayOutputStream();
                byte[] buf = new byte[1];
                int read;

                while ((read = in.read(buf)) != -1) {
                    _out.write(buf, 0, read);
                    if (in.available() == 0) break;
                }
                resultDataMap.put(fileItem.getFieldName(), new String(_out.toByteArray(), "UTF-8"));
            }
        } catch (Exception e) {
            rsLogger.printStackTrace(e);
        }

        return resultDataMap;
    }
}
