package backend.frontmatterapi.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;

@Getter
@Setter
@ToString
public class LoiPageblockItem {
   private ArrayList<LoiItem> listOfIllustrations;
   private String pageblockName;
   public LoiPageblockItem(String pageblockName){
       this.pageblockName = pageblockName;
       listOfIllustrations = new ArrayList<>();
   }
}
