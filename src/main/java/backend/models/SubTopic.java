package backend.models;
import java.util.ArrayList;
public class SubTopic {
    public ArrayList<SubSubTopic> subSubTopicList;
    public String number;
    public String subject;
    public String pageNumber;
    public SubTopic(){
        subSubTopicList = new ArrayList<>();
    }

    @Override
    public String toString() {
        System.out.println(this.number+" | " + this.subject + " | " + this.pageNumber);
        for(SubSubTopic subSubTopic: this.subSubTopicList){
            System.out.println(subSubTopic);
        }
        return "----------------------------------------------------------------------------------------------";
    }
}
