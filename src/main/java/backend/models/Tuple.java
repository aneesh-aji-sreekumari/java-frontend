package backend.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
@Getter
@Setter
public class Tuple {
    private ArrayList<String> tocList;
    private ArrayList<String> loiList;
    private ArrayList<String> lotList;
    public Tuple(){
        tocList = new ArrayList<>();
        loiList = new ArrayList<>();
        lotList = new ArrayList<>();
    }
}
