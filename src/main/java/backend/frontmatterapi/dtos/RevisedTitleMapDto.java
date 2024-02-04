package backend.frontmatterapi.dtos;
import java.util.ArrayList;
import java.util.HashMap;
public class RevisedTitleMapDto {
    private HashMap<String, ArrayList<RevisedTitleDto>> tocMap;
    private HashMap<String, ArrayList<RevisedTitleDto>> lotMap;
    private HashMap<String, ArrayList<RevisedTitleDto>> loiMap;
    public RevisedTitleMapDto(HashMap<String, ArrayList<RevisedTitleDto>> tocMap,
                              HashMap<String, ArrayList<RevisedTitleDto>> lotMap,
                              HashMap<String, ArrayList<RevisedTitleDto>> loiMap){
        this.loiMap = loiMap;
        this.lotMap = lotMap;
        this.tocMap = tocMap;
    }

    public HashMap<String, ArrayList<RevisedTitleDto>> getTocMap() {
        return tocMap;
    }

    public void setTocMap(HashMap<String, ArrayList<RevisedTitleDto>> tocMap) {
        this.tocMap = tocMap;
    }

    public HashMap<String, ArrayList<RevisedTitleDto>> getLotMap() {
        return lotMap;
    }

    public void setLotMap(HashMap<String, ArrayList<RevisedTitleDto>> lotMap) {
        this.lotMap = lotMap;
    }

    public HashMap<String, ArrayList<RevisedTitleDto>> getLoiMap() {
        return loiMap;
    }

    public void setLoiMap(HashMap<String, ArrayList<RevisedTitleDto>> loiMap) {
        this.loiMap = loiMap;
    }
}
