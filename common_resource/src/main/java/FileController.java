import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class FileController implements Serializable {

    private String fileName;
    private long size;
    private byte[] bytes;
    private Path path;

    public FileController(Path path) {
        try {
            this.fileName = path.getFileName().toString();
            this.size = Files.size(path);
            this.bytes = Files.readAllBytes(path); // Следует заменить на чтение по пакетам
            this.path = path;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

}
