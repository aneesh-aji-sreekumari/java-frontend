package backend.frontmatterapi.models;

import java.util.ArrayList;

public class PageBlock  implements FmTypeInterface{
    public ArrayList<SubTopic> subTopicList;
    public String pageNumber;
    public String pageBlockName;
    public PageBlock(){
        subTopicList = new ArrayList<>();
    }

}
