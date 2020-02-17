import java.io.*;

public class FileUtils {
    public static final String RAWS = "raws.txt";
    private String rooPath;

    public FileUtils() {
        File directory = new File("");//参数为空
        try {
            rooPath = directory.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FileUtils getInstance() {
        return new FileUtils();
    }

    public void writeFile(InputStream is, String fileName) {
        File file = new File(rooPath, fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            is.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String parseRaws() throws Exception {
        StringBuffer sb = new StringBuffer();
        File file = new File(rooPath, RAWS);
        if (!file.exists()) {
            throw new Exception("没有找到raws.txt文件");
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s;
            while ((s = br.readLine()) != null) {
                sb.append(s + "\n");
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
