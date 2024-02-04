package backend.frontmatterapi.models;
import java.util.ArrayList;
public class PgblckRegexPattern {
    private ArrayList<String> patterns;
    public PgblckRegexPattern(String Str1, String Str2){
        patterns = new ArrayList<>();
        patterns.add(Str1);
        patterns.add(Str2);
    }
    public PgblckRegexPattern(String list){
        patterns = new ArrayList<>();
        patterns.add(list);
    }

    public ArrayList<String> getPatterns() {
        return patterns;
    }

    public void setPatterns(ArrayList<String> patterns) {
        this.patterns = patterns;
    }
}
