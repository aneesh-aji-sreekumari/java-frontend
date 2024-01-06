package backend.frontmatterapi.services;
import backend.taggenerator.PreProcessing;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Client{
    public static void main(String[] args) {
//        String  s = "This <date pgnum= '20'>is a test <delete deldate='20240121'>abcdef</delete>title <revst revdate='202140121'>for <revst revdate='202140121'>the<revend> text<revend> extractor";
//        System.out.println("The String input was :" + s);
//        System.out.println("The Output is: " + titleExtractor(s));
        //System.out.println(getSubSubTopicNumberIntegerValue("BC"));
        System.out.println(updatePagenumDateToTitleTag("<title fmrelocdate=\"20240121\" pgnumdate=\"20230526\">General</title>", "20240121"));
    }
    public static String titleExtractor(String titleFromTag){
        StringBuilder sb = new StringBuilder();
        int N = titleFromTag.length();
        int i=0;
        while(i<N){
            char ch = titleFromTag.charAt(i);
            if(ch == '<'){
                i = findEndIndexOfTag(titleFromTag, i+1, N) + 1;
            }
            else{
                sb.append(ch);
                i++;
            }
        }
        return sb.toString();
    }
    public static int findEndIndexOfTag(String s, int st, int N){
        String[] tag = getCurrentTag(s, st, N );
        for(int i=Integer.parseInt(tag[1]); i<N; i++){
            if(tag[0].contains("delete") || tag[0].contains("sub") || tag[0].contains("super")){
                if(s.charAt(i) == '/'){
                    i=i+1;
                    while(i<N){
                        if(s.charAt(i) == '>')
                            return i;
                        i++;
                    }
                }
            }
            else if(tag[0].contains("revst") || tag[0].contains("revend") || tag[0].contains("hotlink") || tag[0].contains("fmdate")
                    || tag[0].contains("leafst") || tag[0].contains("pgbrk") || tag[0].contains("reloc") || tag[0].contains("date")
            || tag[0].contains("leafend")){
                if(tag[0].charAt(tag[0].length()-1) == '>')
                    return Integer.parseInt(tag[1]);
                if(s.charAt(i) == '>')
                    return i;
            }
        }
        return -1;
    }
    public static String[] getCurrentTag(String s, int i, int N){
        StringBuilder sb = new StringBuilder();
        while (i<N){
            if(s.charAt(i) == ' ')
                return new String[] {sb.toString(), Integer.toString(i+1)};
            else if(s.charAt(i) == '>')
                return new String[] {sb.toString(), Integer.toString(i)};
            else{
                sb.append(s.charAt(i));
                i++;
            }

        }
        return new String[] {sb.toString(), Integer.toString(i+1)};
    }
    public static String updatePagenumDateToTitleTag(String s, String revDate){
        StringBuilder sb = new StringBuilder();
        if(s.charAt(6) == '>'){
            sb.append("<title pgnumdate=");
            sb.append('"');
            sb.append(revDate);
            sb.append('"');
            sb.append('>');
            sb.append(s.substring(7, s.length()));
        }
        else if(s.contains("pgnumdate")){
            int[]indices = getStartAndEndOfPagenumDate(s, 7);
            System.out.println("The Trim; " + s.substring(indices[0], indices[1]+1));
            sb.append(s.substring(0, indices[0]+1));
            sb.append(revDate);
            sb.append(s.substring(indices[1], s.length()));
            return sb.toString();
        }
        else if(s.contains("fmrelocdate")){
            int end = getEndOfFmreLocDate(s, 7);
            sb.append(s.substring(0, end + 1)).append(" pgnumdate=");
            sb.append('"');
            sb.append(revDate);
            sb.append('"');
            //sb.append('>');
            sb.append(s.substring(end+1));
            return sb.toString();
        }
        return sb.toString();
    }

    private static int getEndOfFmreLocDate(String s, int i) {
        int ans = -1;
        while(i<s.length()){
            if(s.charAt(i) == 'f'){
                i = i+13;
                while(i<s.length()){
                    if(s.charAt(i) == '"'){
                        return i;
                    }
                    i++;
                }
            }
            else
                i++;
        }
        return ans;
    }

    private static int[] getStartAndEndOfPagenumDate(String s, int i) {
        int[] ans = new int[]{-1, -1};
        while(i<s.length()){
            if(s.charAt(i) == 'p'){
                ans[0] = i+ 10;
                i = ans[0]+1;
                while(i<s.length()){
                    if(s.charAt(i) == '"'){
                        ans[1] = i;
                        return ans;
                    }
                     i++;
                }
            }
            else
                i++;
        }
        return ans;
    }



}
