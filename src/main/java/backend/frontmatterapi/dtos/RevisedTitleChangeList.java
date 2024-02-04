package backend.frontmatterapi.dtos;

import java.util.ArrayList;

public class RevisedTitleChangeList {
    private ArrayList<RevisedTitleDto> tocList;
    private ArrayList<RevisedTitleDto> loiList;
    private ArrayList<RevisedTitleDto> lotList;

    public ArrayList<RevisedTitleDto> getTocList() {
        return tocList;
    }

    public void setTocList(ArrayList<RevisedTitleDto> tocList) {
        this.tocList = tocList;
    }

    public ArrayList<RevisedTitleDto> getLoiList() {
        return loiList;
    }

    public void setLoiList(ArrayList<RevisedTitleDto> loiList) {
        this.loiList = loiList;
    }

    public ArrayList<RevisedTitleDto> getLotList() {
        return lotList;
    }

    public void setLotList(ArrayList<RevisedTitleDto> lotList) {
        this.lotList = lotList;
    }
}
