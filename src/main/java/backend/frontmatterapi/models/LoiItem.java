package backend.frontmatterapi.models;


public class LoiItem implements FmTypeInterface{
    private String figureNumber;
    private String figureTitle;
    private String pageblock;
    private String pageNumber;

    public String getFigureNumber() {
        return figureNumber;
    }

    public void setFigureNumber(String figureNumber) {
        this.figureNumber = figureNumber;
    }

    public String getFigureTitle() {
        return figureTitle;
    }

    public void setFigureTitle(String figureTitle) {
        this.figureTitle = figureTitle;
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
