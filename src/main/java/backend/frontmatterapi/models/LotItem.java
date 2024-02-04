package backend.frontmatterapi.models;

public class LotItem  implements FmTypeInterface{
    private String tableNumber;
    private String tableTitle;
    private String pageblock;
    private String pageNumber;

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getTableTitle() {
        return tableTitle;
    }

    public void setTableTitle(String tableTitle) {
        this.tableTitle = tableTitle;
    }

    public String getPageblock() {
        return pageblock;
    }

    public void setPageblock(String pageblock) {
        this.pageblock = pageblock;
    }

    public String getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }
}
