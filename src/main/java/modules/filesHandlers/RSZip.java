package modules.filesHandlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Класс для работы с zip-архивами
 *
 * @author nechkin.sergei.sergeevich
 */
public class RSZip {

    public boolean deleteDirectory(String dirName) {
        return deleteDirectory(new File(dirName));
    }

    /**
     * Рекурсивно удалить директорию
     *
     * @param dir имя директории
     * @return boolean. Если удалено, то true иначе false
     */
    private boolean deleteDirectory(File dir) {
        boolean result = true;
        if (dir.exists()) {
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    File f = new File(dir, children[i]);
                    deleteDirectory(f);
                }
                if (!dir.delete()) result = false;
            } else {
                if (!dir.delete()) result = false;
            }
        }
        return result;
    }

    /**
     * Извлеч из zip-файла директорию в указанною директорию
     *
     * @param destinationFolder путь к директори в которую будет извлечена директория
     * @param zipFile           путь к zip-файлу
     * @param folder            имя извлекаемой директории
     * @exception IOException   Ошибка чтения файла
     */
    public void unzipFolder(String destinationFolder, String zipFile, String folder) throws IOException {
        File directory = new File(destinationFolder);
        if (!directory.exists())
            directory.mkdirs();

        byte[] buffer = new byte[2048];
        if (!new File(zipFile).canRead()) {
            System.out.println("dont canRead");
        }
        FileInputStream fInput = new FileInputStream(zipFile);
        ZipInputStream zipInput = new ZipInputStream(fInput);

        ZipEntry entry = zipInput.getNextEntry();

        while (entry != null) {
            String entryName = entry.getName();
            File file = new File(destinationFolder + File.separator + entryName);
            if (entry.getName().indexOf(folder + "/") == 0) {
                if (entry.isDirectory()) {
                    File newDir = new File(file.getAbsolutePath());
                    if (!newDir.exists()) {
                        boolean success = newDir.mkdirs();
                        if (success == false) {
                            //System.out.println("Problem creating Folder");
                        }
                    }
                } else {
                    FileOutputStream fOutput = new FileOutputStream(file);
                    int count = 0;
                    while ((count = zipInput.read(buffer)) > 0) {
                        fOutput.write(buffer, 0, count);
                    }
                    fOutput.close();
                }
            }
            zipInput.closeEntry();
            entry = zipInput.getNextEntry();
        }
        zipInput.closeEntry();
        zipInput.close();
        fInput.close();
    }
}
