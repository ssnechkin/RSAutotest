package plugins.reports.allure;

import com.google.common.reflect.ClassPath;
import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.ReportGenerator;
import io.qameta.allure.core.Configuration;
import modules.testExecutor.interfaces.SuiteDatas;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class AllureHelper {
    private SuiteDatas suite;

    public AllureHelper(SuiteDatas suite) {
        this.suite = suite;
    }

    public static void unpackDummyResources(String prefix, Path output) throws IOException {
        ClassPath classPath = ClassPath.from(main.Main.class.getClassLoader());

        Map<String, URL> files = classPath.getResources().stream()
                .filter(info -> info.getResourceName().startsWith(prefix))
                .collect(Collectors.toMap(
                        info -> info.getResourceName().substring(prefix.length()),
                        ClassPath.ResourceInfo::url)
                );
        files.forEach((name, url) -> {
            Path file = output.resolve(name);
            try (InputStream is = url.openStream()) {
                Files.copy(is, file);
            } catch (IOException e) {
                throw new RuntimeException(String.format("name: %s, url: %s", name, url), e);
            }
        });
    }

    public void generateAllureFace(String resultsPath, String outPath) throws IOException {
        Path output = Paths.get(outPath);
        Configuration configuration = new ConfigurationBuilder().useDefault().build();
        ReportGenerator generator = new ReportGenerator(configuration);
        Path resultsDirectory = Paths.get(resultsPath);
        unpackDummyResources("allure1data/", resultsDirectory);
        generator.generate(output, resultsDirectory);
    }

    public boolean deleteDirectory(File dir) {
        boolean result = true;
        if (dir.exists()) {
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; (children != null && i < children.length); i++) {
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

    public void replaceTextInFile(String filePath, String replaceText, String newText) {

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            ArrayList<String> fileText = new ArrayList<>();
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                fileText.add(sCurrentLine.replace(replaceText, newText));
            }
            writeFile(filePath, fileText);
        } catch (IOException e) {
            suite.printStackTrace(e);
        }

    }

    public void writeFile(String filePathAndName, ArrayList<String> data) {
        OutputStream os = null;
        if (!new File(filePathAndName).exists()) {
            try {
                new File(filePathAndName).createNewFile();
            } catch (Exception e) {
                suite.printStackTrace(e);
            }
        }
        if (filePathAndName != null && new File(filePathAndName).exists()) {
            try {
                os = new FileOutputStream(new File(filePathAndName));
                for (String text : data) {
                    os.write((text).getBytes());
                    os.write("\n".getBytes());
                }
            } catch (IOException e) {
                suite.printStackTrace(e);
            } finally {
                try {
                    os.close();
                } catch (IOException e) {
                    suite.printStackTrace(e);
                }
            }
        }
    }

    public void copyDirectory(File sourceLocation, File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    public void unzipOneFile(String destinationFolder, String zipFile, String fileName) {
        if (zipFile != null && fileName != null) {
            File directory = new File(destinationFolder);
            if (!directory.exists())
                directory.mkdirs();

            byte[] buffer = new byte[2048];

            try {
                FileInputStream fInput = new FileInputStream(zipFile);
                ZipInputStream zipInput = new ZipInputStream(fInput);

                ZipEntry entry = zipInput.getNextEntry();

                while (entry != null) {
                    String entryName = entry.getName();
                    if (entryName.lastIndexOf(fileName) >= 0) {
                        File file = new File(destinationFolder + File.separator + fileName);
                        FileOutputStream fOutput = new FileOutputStream(file);
                        int count = 0;
                        while ((count = zipInput.read(buffer)) > 0) {
                            fOutput.write(buffer, 0, count);
                        }
                        fOutput.close();
                        break;
                    }
                    zipInput.closeEntry();
                    entry = zipInput.getNextEntry();

                }
                zipInput.closeEntry();
                zipInput.close();
                fInput.close();
            } catch (IOException e) {
                suite.printStackTrace(e);
            }
        }
    }

    public void createZip(String source_dir, String zip_file) throws Exception {
        FileOutputStream fout = new FileOutputStream(zip_file);
        ZipOutputStream zout = new ZipOutputStream(fout);
        //zout.setEncoding("CP866");
        File fileSource = new File(source_dir);
        addDirectoryInZip(zout, fileSource);
        zout.close();
    }

    public void addDirectoryInZip(ZipOutputStream zout, File fileSource) throws Exception {
        File[] files = fileSource.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addDirectoryInZip(zout, files[i]);
                continue;
            }
            FileInputStream fis = new FileInputStream(files[i]);
            zout.putNextEntry(new ZipEntry(files[i].getPath()));

            byte[] buffer = new byte[4048];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zout.write(buffer, 0, length);
            }
            zout.closeEntry();
            fis.close();
        }
    }

    public void addFileInZip(ZipOutputStream zout, File fileSource, String fileName) throws Exception {
        FileInputStream fis = new FileInputStream(fileSource);
        zout.putNextEntry(new ZipEntry(fileName));

        byte[] buffer = new byte[4048];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            zout.write(buffer, 0, length);
        }
        zout.closeEntry();
        fis.close();
    }
}
