package backend.frontmatterapi.dtos;

import backend.frontmatterapi.models.FmChangeItem;

public class RevisedTitleDto {
    private FmChangeItem fmChangeItem;
    private String pgnumDate;
    public RevisedTitleDto(){
        fmChangeItem = new FmChangeItem();
        pgnumDate = "";
    }

    public FmChangeItem getFmChangeItem() {
        return fmChangeItem;
    }

    public void setFmChangeItem(FmChangeItem fmChangeItem) {
        this.fmChangeItem = fmChangeItem;
    }

    public String getPgnumDate() {
        return pgnumDate;
    }

    public void setPgnumDate(String pgnumDate) {
        this.pgnumDate = pgnumDate;
    }
}
