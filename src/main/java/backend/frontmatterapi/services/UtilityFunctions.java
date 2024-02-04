package backend.frontmatterapi.services;

import backend.frontmatterapi.models.FmTypeInterface;
import backend.frontmatterapi.models.LoiItem;
import backend.frontmatterapi.models.PgblckRegexPattern;
import javafx.scene.control.Alert;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilityFunctions {
    AutomationService automationService = new AutomationService();
    /* This Method is used to check a pattern is available or not in the starting of a String
        This functions can identify patterns of type: "\\d{3} ",  "\\d{1,3} ", etc
     */
    public String getMatchingSubstring(String s, String p) {
        int N = s.length();
        int M = p.length();
        int i = 0;
        int j = 0;
        StringBuilder sb = new StringBuilder();
        while(i<N && j<M){
            char ch = p.charAt(j);
            if(j+1<M && p.charAt(j) == '\\' && p.charAt(j+1) == 'd'){
                if(j+5 < M && p.charAt(j+2) == '{' && p.charAt(j+6) == '}'){
                    int cnt = 0;
                    int minCnt = p.charAt(j+3)-'0';
                    int maxCnt = p.charAt(j+5)-'0';
                    while(i<M && cnt<maxCnt){
                        if(s.charAt(i) >= '0' && s.charAt(i)<= '9'){
                            cnt++;
                            if(cnt > maxCnt)
                                return null;
                            sb.append(s.charAt(i));
                            i++;
                        }
                        else{
                            break;
                        }
                    }
                    if(cnt>=minCnt && cnt<=maxCnt)
                        j=j+7;
                    else
                        return null;
                }
                else if(j+4 < M && p.charAt(j+1) == 'd' && p.charAt(j+2) == '{' && p.charAt(j+4) == '}'){
                    int cnt = 0;
                    int maxCnt = p.charAt(j+3) - '0';
                    while(i<M && cnt<maxCnt){
                        if(s.charAt(i) >= '0' && s.charAt(i) <= '9'){
                            cnt++;
                            sb.append(s.charAt(i));
                            i++;
                        }
                    }
                    if(cnt == maxCnt){
                        j = j+5;
                    }
                    else{
                        return null;
                    }
                }

            }
            else if(j+1<M && p.charAt(j) == '\\' && p.charAt(j+1) == ' '){
                j++;
            }
            else{
                if(s.charAt(i) == p.charAt(j)){
                    sb.append(s.charAt(i));
                    i++;
                    j++;
                }
                else{
                    return null;
                }
            }
        }
        if(j==M)
            return sb.toString();
        return null;
    }
    /* This method provides respective pattern of the Figure/Table of Pageblock */
    public PgblckRegexPattern getPatternByPageblock(String str) {
        if (str.startsWith("INTRO"))
            return new PgblckRegexPattern("INTRO-\\d{1,4}\\ ");
        if (str.startsWith("DESCRIPTION AND OPERATION"))
            return new PgblckRegexPattern("\\d{1,3} ");
        if (str.startsWith("TESTING AND FAULT ISOLATION"))
            return new PgblckRegexPattern("1\\d{3}\\ ");
        if (str.startsWith("SCHEMATIC AND WIRING DIAGRAMS"))
            return new PgblckRegexPattern("2\\d{3}\\ ");
        if (str.startsWith("DISASSEMBLY"))
            return new PgblckRegexPattern("3\\d{3}\\ ");
        if (str.startsWith("CLEANING"))
            return new PgblckRegexPattern("4\\d{3}\\ ");
        if (str.startsWith("INSPECTION/CHECK"))
            return new PgblckRegexPattern("5\\d{3}\\ ");
        if (str.startsWith("REPAIR"))
            return new PgblckRegexPattern("6\\d{3}\\ ");
        if (str.startsWith("ASSEMBLY"))
            return new PgblckRegexPattern("7\\d{3}\\ ");
        if (str.startsWith("FITS AND CLEARANCES"))
            return new PgblckRegexPattern("8\\d{3}\\ ");
        if (str.startsWith("SPECIAL TOOLS, FIXTURES, EQUIPMENT AND CONSUMABLES"))
            return new PgblckRegexPattern("9\\d{3}\\ ");
        if (str.startsWith("ILLUSTRATED PARTS LIST"))
            return new PgblckRegexPattern("IPL \\d{1,3}\\ ", "\\d{1,3} ");
        if (str.startsWith("SPECIAL PROCEDURES"))
            return new PgblckRegexPattern("11\\d{3}\\ ");
        if (str.startsWith("REMOVAL"))
            return new PgblckRegexPattern("12\\d{3}\\ ");
        if (str.startsWith("INSTALLATION"))
            return new PgblckRegexPattern("13\\d{3}\\ ");
        if (str.startsWith("SERVICING"))
            return new PgblckRegexPattern("14\\d{3}\\ ");
        if (str.startsWith("STORAGE INCLUDING TRANSPORTATION"))
            return new PgblckRegexPattern("15\\d{3}\\ ");
        if (str.startsWith("REWORK"))
            return new PgblckRegexPattern("16\\d{3}\\ ");
        if (str.startsWith("APPENDIX A"))
            return new PgblckRegexPattern("A-\\d{1,3}\\ ");
        return new PgblckRegexPattern("");
    }

    public String getPageBlockFromPageNumber(String str){
        if(str.contains(".")){
            int i=0, N = str.length();
            while(i<N){
                if(str.charAt(i) == '.'){
                    str = str.substring(0, i);
                    break;
                }
                i++;
            }
        }
        if(str.startsWith("INTRO"))
            return "INTRODUCTION";
        if(str.startsWith("IPL"))
            return "ILLUSTRATED PARTS LIST";
        if(str.startsWith("A-"))
            return "APPENDIX A";
        int tableNumber;
        try{
            tableNumber = Integer.parseInt(str);
        }
        catch (NumberFormatException numberFormatException){
            return null;
        }

        if(tableNumber>=1 && tableNumber<=999)
            return "DESCRIPTION AND OPERATION";
        else if(tableNumber>=1001 && tableNumber<=1999)
            return "TESTING AND FAULT ISOLATION";
        else if(tableNumber>=2001 && tableNumber<=2999)
            return "SCHEMATIC AND WIRING DIAGRAMS";
        else if(tableNumber>=3001 && tableNumber<=3999)
            return "DISASSEMBLY";
        else if(tableNumber>=4001 && tableNumber<=4999)
            return "CLEANING";
        else if(tableNumber>=5001 && tableNumber<=5999)
            return "INSPECTION/CHECK";
        else if(tableNumber>=6001 && tableNumber<=6999)
            return "REPAIR";
        else if(tableNumber>=7001 && tableNumber<=7999)
            return "ASSEMBLY";
        else if(tableNumber>=8001 && tableNumber<=8999)
            return "FITS AND CLEARANCES";
        else if(tableNumber>=9001 && tableNumber<=9999)
            return "SPECIAL TOOLS, FIXTURES, EQUIPMENT AND CONSUMABLES";
        else if(tableNumber>=10001 && tableNumber<=10999)
            return "ILLUSTRATED PARTS LIST";
        else if(tableNumber>=11001 && tableNumber<=11999)
            return "SPECIAL PROCEDURES";
        else if(tableNumber>=12001 && tableNumber<=12999)
            return "REMOVAL";
        else if(tableNumber>=13001 && tableNumber<=13999)
            return "INSTALLATION";
        else if(tableNumber>=14001 && tableNumber<=14999)
            return "SERVICING";
        else if(tableNumber>=15001 && tableNumber<=15999)
            return "STORAGE INCLUDING TRANSPORTATION";
        else if(tableNumber>=16001 && tableNumber<=16999)
            return "REWORK";
        return "Not valid";
    }

        public String getFileNameFromPageblockName(String pageblock, Set<String> listOfFiles, HashMap<String, String> filenamesMap){
            for (String str : listOfFiles) {
                if (filenamesMap.get(pageblock) != null && str.toLowerCase().contains(filenamesMap.get(pageblock).toLowerCase())) {
                    return str;
                }
            }
            return null;
        }
        public int getNumberFromString(String str){
            int N = str.length();
            if(!(str.charAt(0) >= '0' && str.charAt(0) <= '9')){
                int i=1;
                while(i<N){
                    if(str.charAt(i) >= '0' && str.charAt(i) <= '9'){
                        return Integer.parseInt(str.substring(i, N));
                    }
                    i++;
                }
            }
            return Integer.parseInt(str);
        }
    public HashMap<String, String> getFilename(String path, Set<String> pageblocks){
        Set<String> listOfFiles = automationService.listFilesInFolder(path);
        HashMap<String, String> filenamesMap = new HashMap<>();
        for(String pgblk: pageblocks){
            String str = getFileNameFromPageblockName(pgblk, listOfFiles,automationService.getFilenamesMap());
            if (str == null){
                showFileExtactContents(pgblk);
                return null;
            }
            filenamesMap.put(pgblk, str);
        }
        return filenamesMap;
    }
    public String removeExtraSpaceFromTitle(String s){
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<s.length(); i++){
            char ch = s.charAt(i);
            if(i>0 && sb.charAt(sb.length()-1) == ' ' && ch == ' ')
                continue;
            sb.append(ch);
        }
        return sb.toString();
    }
    public static int[] getFirstMatchingPattern(String input, String pattern){
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(input);

        if (matcher.find()) {
            int startIndex = matcher.start();
            int endIndex = matcher.end() - 1;
            return new int[]{startIndex, endIndex};
        }

        return null;
    }
    private void showFileExtactContents(String Pageblock) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Could Not Find The SGML File");
        alert.setHeaderText(null);
        alert.setContentText("There was some issue while trying to find SGML File of "+Pageblock + "\n" +
        "Please make sure the file name is as per standard and try again");
        alert.showAndWait();
    }

}
