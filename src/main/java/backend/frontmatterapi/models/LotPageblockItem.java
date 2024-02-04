package backend.frontmatterapi.models;


import java.util.ArrayList;
public class LotPageblockItem {
   private ArrayList<LotItem> listOfTables;
   private String pageblockName;
   public LotPageblockItem(String pageblockName){
       this.pageblockName = pageblockName;
       listOfTables = new ArrayList<>();
   }

    public ArrayList<LotItem> getListOfTables() {
        return listOfTables;
    }

    public void setListOfTables(ArrayList<LotItem> listOfTables) {
        this.listOfTables = listOfTables;
    }

    public String getPageblockName() {
        return pageblockName;
    }

    public void setPageblockName(String pageblockName) {
        this.pageblockName = pageblockName;
    }
}
