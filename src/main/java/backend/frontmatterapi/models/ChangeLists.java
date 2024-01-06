package backend.frontmatterapi.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
@Getter
@Setter
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
}
