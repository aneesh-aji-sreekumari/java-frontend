package backend.frontmatterapi.models;
public class FmChangeItem {
    String pageblock;
    ChangeType changeType;
    FrontMatterType frontMatterType;
    SubSubTopic subSubTopic;
    SubTopic subTopic;
    LotItem lotItem;
    LoiItem loiItem;

    public String getPageblock() {
        return pageblock;
    }

    public void setPageblock(String pageblock) {
        this.pageblock = pageblock;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public FrontMatterType getFrontMatterType() {
        return frontMatterType;
    }

    public void setFrontMatterType(FrontMatterType frontMatterType) {
        this.frontMatterType = frontMatterType;
    }

    public SubSubTopic getSubSubTopic() {
        return subSubTopic;
    }

    public void setSubSubTopic(SubSubTopic subSubTopic) {
        this.subSubTopic = subSubTopic;
    }

    public SubTopic getSubTopic() {
        return subTopic;
    }

    public void setSubTopic(SubTopic subTopic) {
        this.subTopic = subTopic;
    }

    public LotItem getLotItem() {
        return lotItem;
    }

    public void setLotItem(LotItem lotItem) {
        this.lotItem = lotItem;
    }

    public LoiItem getLoiItem() {
        return loiItem;
    }

    public void setLoiItem(LoiItem loiItem) {
        this.loiItem = loiItem;
    }
}
