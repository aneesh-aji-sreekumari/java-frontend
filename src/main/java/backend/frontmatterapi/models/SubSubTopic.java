package backend.frontmatterapi.models;

public class SubSubTopic  implements FmTypeInterface{
    public String number;
    public String subject;
    public String pageNumber;
    public SubTopic parentSubTopic;

    @Override
    public String toString() {
        return this.number+" | " + this.subject + " | " + this.pageNumber;
    }
}
