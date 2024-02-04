package backend.frontmatterapi.models;
import java.util.ArrayList;
public class SaveFilesResult {
    private ArrayList<String> completedList;
    private ArrayList<String> incompletedList;
    public SaveFilesResult(ArrayList<String> completedList, ArrayList<String> incompletedList){
        this.completedList = completedList;
        this.incompletedList = incompletedList;
    }

    public ArrayList<String> getCompletedList() {
        return completedList;
    }

    public void setCompletedList(ArrayList<String> completedList) {
        this.completedList = completedList;
    }

    public ArrayList<String> getIncompletedList() {
        return incompletedList;
    }

    public void setIncompletedList(ArrayList<String> incompletedList) {
        this.incompletedList = incompletedList;
    }
}
