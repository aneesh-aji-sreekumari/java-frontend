package backend.frontmatterapi.models;
import java.nio.file.Path;
import java.util.ArrayList;
public class DownloadResults {
    private ArrayList<String> tocResult;
    private ArrayList<String> loiResult;
    private  ArrayList<String> lotResult;
    private Path path;

    public ArrayList<String> getTocResult() {
        return tocResult;
    }

    public void setTocResult(ArrayList<String> tocResult) {
        this.tocResult = tocResult;
    }

    public ArrayList<String> getLoiResult() {
        return loiResult;
    }

    public void setLoiResult(ArrayList<String> loiResult) {
        this.loiResult = loiResult;
    }

    public ArrayList<String> getLotResult() {
        return lotResult;
    }

    public void setLotResult(ArrayList<String> lotResult) {
        this.lotResult = lotResult;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
