package backend.frontmatterapi.models;

import java.util.ArrayList;

public class ChangeLists {
   private ArrayList<FmChangeItem> tocList;
   private ArrayList<FmChangeItem> tablesList;
   private ArrayList<FmChangeItem> figuresList;

    public ChangeLists(ArrayList<FmChangeItem> fmChangeItems){
        tocList = new ArrayList<>();
        tablesList = new ArrayList<>();
        figuresList = new ArrayList<>();
        for(int i=0; i<fmChangeItems.size(); i++){
            if(fmChangeItems.get(i).getFrontMatterType().equals(FrontMatterType.FIGURE))
                figuresList.add(fmChangeItems.get(i));
            else if(fmChangeItems.get(i).getFrontMatterType().equals(FrontMatterType.TABLE))
                tablesList.add(fmChangeItems.get(i));
            else
                tocList.add(fmChangeItems.get(i));
        }
    }

    public ArrayList<FmChangeItem> getTocList() {
        return tocList;
    }

    public void setTocList(ArrayList<FmChangeItem> tocList) {
        this.tocList = tocList;
    }

    public ArrayList<FmChangeItem> getTablesList() {
        return tablesList;
    }

    public void setTablesList(ArrayList<FmChangeItem> tablesList) {
        this.tablesList = tablesList;
    }

    public ArrayList<FmChangeItem> getFiguresList() {
        return figuresList;
    }

    public void setFiguresList(ArrayList<FmChangeItem> figuresList) {
        this.figuresList = figuresList;
    }
}
