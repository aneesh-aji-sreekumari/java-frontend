package backend.frontmatterapi.models;

public class DecimalPageHandler {
    String pattern;
    int startIndex;
    public DecimalPageHandler(String pattern, int startIndex){
        this.startIndex = startIndex;
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
}
