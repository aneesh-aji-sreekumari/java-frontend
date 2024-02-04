package backend.frontmatterapi.models;

import java.util.ArrayList;

public class LoiPageblockItem {
   private ArrayList<LoiItem> listOfIllustrations;
   private String pageblockName;
   public LoiPageblockItem(String pageblockName){
       this.pageblockName = pageblockName;
       listOfIllustrations = new ArrayList<>();
   }

    public ArrayList<LoiItem> getListOfIllustrations() {
        return listOfIllustrations;
    }

    public void setListOfIllustrations(ArrayList<LoiItem> listOfIllustrations) {
        this.listOfIllustrations = listOfIllustrations;
    }

    public String getPageblockName() {
        return pageblockName;
    }

    public void setPageblockName(String pageblockName) {
        this.pageblockName = pageblockName;
    }
}
