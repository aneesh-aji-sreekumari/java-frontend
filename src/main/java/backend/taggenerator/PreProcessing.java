package backend.taggenerator;

import java.util.ArrayList;

public class PreProcessing {
    //Checks if the current '<' is of a specific tag. If true returns 1, else returns 0;
    public static int checkCurrentTag(String s, int N, int curr, String tag){
        int j=0;
        int M = tag.length();
        for(int i=curr; i<N; i++){
            if(s.charAt(i) != tag.charAt(j))
                return 0;
            j++;
            if(j==M)
                return 1;
        }
        return 0;
    }

    public static ArrayList<String> getAllItemsOfFigure(String s, String tag){
        /* This Method will get all the item numbers as a String ArrayList */
        int N = s.length();
        ArrayList<String> itemDataTags = new ArrayList<>();
        int stIdx = -1;
        for(int i=0; i<N; i++){
            if(s.charAt(i) == '<' && checkCurrentTag(s, N, i+1, tag) == 1){
                stIdx = i;
                i = i+ (tag+" ").length();
            }
            else if(stIdx != -1 && s.charAt(i) == '/' && checkCurrentTag(s, N, i+1, tag) == 1){
                int endIdx = i+ (tag+'>').length();
                itemDataTags.add(s.substring(stIdx, endIdx+1));
                i = endIdx;
                stIdx = -1;
            }
        }
        return itemDataTags;
    }

    //This method returns start and end index of a particular tag, if the tag is not found returns [-1, -1]
    public static int[] getStartAndEndIndexOfTag(String s, String tag, int currIndex){
        int[] ans = new int[]{-1, -1};
        int N = s.length();
        for(int i=currIndex; i<N; i++){
            if(s.charAt(i) == '<' && ans[0] == -1 && checkCurrentTag(s, N, i+1, tag) == 1){
                ans[0] = i;
            }
            else if(s.charAt(i) == '/' && ans[0] != -1 && checkCurrentTag(s, N, i+1, tag) == 1){
                ans[1] = i+tag.length()+1;
                return ans;
            }
        }

        return ans;
    }
    public static int firstOccuranceOfTagInIPLFigure(String iplFigure, String tag){
        int N = iplFigure.length();
        for(int i=0; i<N; i++){
            if(iplFigure.charAt(i) == '<' && checkCurrentTag(iplFigure, N, i+1, tag) == 1)
                return i-1;
        }
        return -1;
    }

    public static int lastOccuranceOfTagInIPLFigure(String iplFigure, String tag){
        int N = iplFigure.length();
        for(int i=N-1; i>=0; i--){
            if(iplFigure.charAt(i) == '>' && checkCurrentTag(iplFigure, N, i-tag.length(), tag) == 1)
                return i;
        }
        return -1;
    }
    public static String startingAndEndingOfAbsoluteTitle(String s){
        int N = s.length();
        int st = 0;
        int end = N-1;
        while (st<N){
            char ch = s.charAt(st);
            if(ch == '>'){
                st+=1;
                break;
            }
                st++;
        }
        while (end > st){
            char ch = s.charAt(end);
            if(ch == '<'){
                break;
            }
            end--;
        }
        return s.substring(st, end);
    }
}
