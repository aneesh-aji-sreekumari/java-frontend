package backend.models;

import java.util.ArrayList;

public class PageBlock {
    public ArrayList<SubTopic> subTopicList;
    public String pageNumber;
    public String pageBlockName;
    public PageBlock(){
        subTopicList = new ArrayList<>();
    }

    @Override
    public String toString() {
        System.out.println(pageBlockName + " | " + pageNumber);
        for(SubTopic subTopic: this.subTopicList)
            System.out.println(subTopic);
        return "..............................................................................................................";
    }


}
