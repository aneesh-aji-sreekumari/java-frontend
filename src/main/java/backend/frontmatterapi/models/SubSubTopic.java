package backend.frontmatterapi.models;

public class SubSubTopic {
    public String number;
    public String subject;
    public String pageNumber;

    @Override
    public String toString() {
        return this.number+" | " + this.subject + " | " + this.pageNumber;
    }
}
