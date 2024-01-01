package backend.models;
import java.util.ArrayList;
public class Tuple {
    private ArrayList<String> tocList;
    private ArrayList<String> loiList;

    public ArrayList<String> getTocList() {
        return tocList;
    }

    public void setTocList(ArrayList<String> tocList) {
        this.tocList = tocList;
    }

    public ArrayList<String> getLoiList() {
        return loiList;
    }

    public void setLoiList(ArrayList<String> loiList) {
        this.loiList = loiList;
    }

    public ArrayList<String> getLotList() {
        return lotList;
    }

    public void setLotList(ArrayList<String> lotList) {
        this.lotList = lotList;
    }

    private ArrayList<String> lotList;
    public Tuple(){
        tocList = new ArrayList<>();
        loiList = new ArrayList<>();
        lotList = new ArrayList<>();
    }
}
