package plugins;

import org.apache.commons.io.FilenameUtils;
import plugins.dataHandlers.StringParsers;
import plugins.executers.CMDExecuter;
import plugins.executers.WaitTestExecuter;
import plugins.executers.datas.ValueExecuter;
import plugins.executers.datas.file.FileExecuter;
import plugins.executers.ibm.mq.IBMmqExecuter;
import plugins.executers.net.ftp.FTPExecutor;
import plugins.executers.net.http.HttpExecuter;
import plugins.executers.pdf.statusModel.PDFStatusModelExecutor;
import plugins.executers.selenide.SelenideExecuter;
import plugins.executers.sql.SQLExecuter;
import plugins.interfaces.DataHandler;
import plugins.interfaces.ReportWriter;
import plugins.interfaces.TestExecutor;
import plugins.reports.allure.AllureReport;
import plugins.reports.ibm.IBMqmForAllure;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Класс для чтения плагинов
 *
 * @author nechkin.sergei.sergeevich
 */
public class PluginReader {
    private String pluginDirName = "plugins";
    private CopyOnWriteArrayList<ReportWriter> reportWriters = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<TestExecutor> testExecutors = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<DataHandler> dataHandlers = new CopyOnWriteArrayList<>();

    /**
     * Читает плагины и загрудает их в память
     */
    public void read() {
        try {
            /* Получить наименование текщего jar-файла */
            String thisJarFileName = FilenameUtils.getBaseName(new File(main.Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getName());

            // Цикл по файлам в директории с плагинами
            for (File file : getFileList(thisJarFileName + File.separator + pluginDirName)) {

                try {
                    // Загрузить классы плагина в память
                    ClassLoader classLoader = URLClassLoader.newInstance(new URL[]{new File(file.getPath()).toURL()}, Thread.currentThread().getContextClassLoader());

                    try {
                        // Цикл по файлам в плагине
                        for (String className : getFileNameListFromZip(file)) {

                            try {// Получить класс из памяти
                                Class clazz = classLoader.loadClass(className.replace("/", ".").replace(".class", ""));

                                try {// Создать экземпляр класса
                                    clazz.newInstance();
                                } catch (Exception e) {
                                }

                                try {// Получить экземпляр класса и добавить в лист
                                    reportWriters.add((ReportWriter) clazz.newInstance());
                                } catch (Exception e) {
                                }

                                try {// Получить экземпляр класса и добавить в лист
                                    testExecutors.add((TestExecutor) clazz.newInstance());
                                } catch (Exception e) {
                                }

                            } catch (Exception e) {
                            }
                        }

                    } catch (Exception e) {
                    }

                } catch (Exception e) {
                }
            }

        } catch (Exception e) {
        }
    }

    public CopyOnWriteArrayList<ReportWriter> getReportWriters() {
        reportWriters = new CopyOnWriteArrayList<>();
        reportWriters.add(new AllureReport());
        reportWriters.add(new IBMqmForAllure());
        return reportWriters;
    }

    public CopyOnWriteArrayList<TestExecutor> getTestExecutors() {
        testExecutors = new CopyOnWriteArrayList<>();
        testExecutors.add(new WaitTestExecuter());
        testExecutors.add(new SelenideExecuter());
        testExecutors.add(new PDFStatusModelExecutor());
        testExecutors.add(new IBMmqExecuter());
        testExecutors.add(new HttpExecuter());
        testExecutors.add(new FTPExecutor());
        testExecutors.add(new ValueExecuter());
        testExecutors.add(new CMDExecuter());
        testExecutors.add(new SQLExecuter());
        testExecutors.add(new FileExecuter());
        return testExecutors;
    }

    public CopyOnWriteArrayList<DataHandler> getDataHandlers() {
        dataHandlers = new CopyOnWriteArrayList<>();
        dataHandlers.add(new StringParsers());
        return dataHandlers;
    }

    /**
     * Возвращает список файлов в директории
     *
     * @param directoryName Имя директории
     */
    private CopyOnWriteArrayList<File> getFileList(String directoryName) {
        File directory = new File(directoryName);

        CopyOnWriteArrayList<File> resultList = new CopyOnWriteArrayList<>();

        File[] fList = directory.listFiles();
        resultList.addAll(Arrays.asList(fList));
        for (File file : fList)
            if (file.isDirectory())
                resultList.addAll(getFileList(file.getAbsolutePath()));

        return resultList;
    }

    /**
     * Возвращает список имён файлов в zip-файле
     *
     * @param zipFile zip-файл
     */
    private CopyOnWriteArrayList<String> getFileNameListFromZip(File zipFile) throws IOException {
        CopyOnWriteArrayList<String> resultList = new CopyOnWriteArrayList<>();
        ZipFile myZipFile = new ZipFile(zipFile);
        Enumeration zipEntries = myZipFile.entries();

        while (zipEntries.hasMoreElements()) resultList.add(((ZipEntry) zipEntries.nextElement()).getName());

        return resultList;
    }
}
