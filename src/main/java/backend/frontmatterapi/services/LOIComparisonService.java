package backend.frontmatterapi.services;

import backend.frontmatterapi.models.FmChangeItem;
import backend.frontmatterapi.models.LoiItem;
import backend.frontmatterapi.models.LoiPageblockItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class LOIComparisonService {
    public Optional<ArrayList<String>> filterUnwantedLinesFromLatestLotLoi(List<String> lines){
        int N = lines.size();
        ArrayList<String> finalList = new ArrayList<>();
        for(int i=0; i<N; i++){
            String s = lines.get(i);
            if(s.contains("cover page") || s.contains("COLLINS")
                    || s.contains("PART OF") || s.contains("COMPONENT")
                    || s.contains("LIST OF") || s.contains("FIGURE") || s.contains("US")
                    || s.contains("VOLUME") || s.contains("LOI") || s.contains("LOT")
                    || s.contains("TABLE"))
            {
                continue;
            }
            String regex = "\\b(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s(?:0[1-9]|[12][0-9]|3[01])/(?:0[1-9]|1[0-9]|2[0-9]|3[01])\\b";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(s);
            if(matcher.find())
                continue;
            finalList.add(s);
        }

        return Optional.of(finalList);
    }
    /* Getting pageNumber, pageblock, tableNumber and title from firstLine, pagenumber will
    be empty if the title goes to multiline */
    public LoiItem getNumberTitleAndPageNumberofFirstLine(String s){
        int N = s.length();
        LoiItem loiItem = new LoiItem();
        StringBuilder sb = new StringBuilder();
        boolean isTitleFound =  false;
        int i=0;
        if(s.startsWith("IPL")){
            if(s.charAt(3) == '*'){
                i = 4;
            }
            else{
                sb.append("IPL ");
                i=4;
            }
        }
        while (i<N){
            if(s.charAt(i) == ' ' && (!sb.isEmpty())){
                loiItem.setFigureNumber(sb.toString().trim());
                loiItem.setPageblock(getPageBlock(loiItem.getFigureNumber()));
                sb = new StringBuilder();
                i= i+1;
                break;
            }
            sb.append(s.charAt(i));
            i++;

        }
        while(i<N){
            char ch = s.charAt(i);
            if(ch == '.' && !sb.isEmpty()){
                loiItem.setFigureTitle(sb.toString().trim());
                sb = new StringBuilder();
                isTitleFound = true;
            }
            else if(ch == '.'){
                i++;
                continue;
            }
            else {
                sb.append(ch);
            }

            i++;
        }
        if(!sb.isEmpty()){
            if(isTitleFound == false){
                loiItem.setFigureTitle(sb.toString().trim());
            }
            else{
                loiItem.setPageNumber(sb.toString().trim());
            }
        }
        /* If loiItem.getPageNumber == null : Additional check to find whether the last characters of the String is
        pagenumber or is it part of table title  */

        if(loiItem.getPageNumber() == null){
            sb = new StringBuilder();
            String str = loiItem.getFigureTitle();
            N = str.length();
            int idx = N-1;
            for(i=N-1; i>=0; i--){
                if(str.charAt(i)>='0' && str.charAt(i)<='9'){
                    sb.append(str.charAt(i));
                }

                else{
                    idx = i;
                    break;
                }
            }
            if(sb.isEmpty())
                return loiItem;
            try{
                sb = sb.reverse();
                int num = Integer.parseInt(sb.toString());
                if(loiItem.getPageblock().equals(getPageBlock(sb.toString()))){
                    loiItem.setFigureTitle(loiItem.getFigureTitle().substring(0, idx+1).trim());
                    loiItem.setPageNumber(sb.toString());
                }
                else
                    return loiItem;
            } catch (NumberFormatException e) {
                return loiItem;
            }
        }


        return loiItem;
    }
    public String getPageBlock(String str){
        if(str.startsWith("IPL"))
            return "ILLUSTRATED PARTS LIST";
        int tableNumber = Integer.parseInt(str);
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
            return "SPECIAL TOOLS, FIXTURES, EQUIPMENT, AND CONSUMABLES";
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
    private static boolean matchesPattern(String input, String patternToMatch) {
        // Escape any special characters in the pattern
        return input.matches(patternToMatch);
    }
    public ArrayList<String> preprocessFilteredLoiList(ArrayList<String> filteredList){
        int N = filteredList.size();
        int i=N-1;
        int iplEnd = -1;
        while(i>=0){
            String str = filteredList.get(i).trim();
            int n = str.length();
            if(matchesPattern(str.substring(n - 6, n), " 10\\d{3}") && iplEnd == -1){
                iplEnd = i;
                i--;
            }
            else if(!matchesPattern(str.substring(n - 5, n), "\\s\\d{4}")){
                i--;
                continue;
            }
            break;
        }
        if(i==N-1)
            return filteredList;
        ArrayList<String> newFilteredList = new ArrayList<>();
        for(int j=0; j<=i; j++){
            newFilteredList.add(filteredList.get(j));
        }
        newFilteredList.add(filteredList.get(i+1));
        i=i+2;
        while(i<=iplEnd){
            String str = filteredList.get(i-1);
            int n = str.length();
            if(matchesPattern(str.substring(n - 6, n), " 10\\d{3}")){
                newFilteredList.add(filteredList.get(i));
            }
            else{
                newFilteredList.set(newFilteredList.size()-1, newFilteredList.get(newFilteredList.size()-1) + " " + filteredList.get(i));
            }
            i++;
        }
        while (i<N){
            newFilteredList.add(filteredList.get(i));
            i++;
        }
        for(int j=0; j< newFilteredList.size(); j++){
            String str = newFilteredList.get(j);
            int n = str.length();
            if(matchesPattern(str.substring(n - 6, n), " 10\\d{3}") && !str.startsWith("IPL")){
                newFilteredList.set(j, "IPL*" + str);
            }
        }
        return newFilteredList;
    }
    public Optional<ArrayList<String>> makeMultilineTitlesSingleline(ArrayList<String> filteredList){
        ArrayList<String> preprocessed = preprocessFilteredLoiList(filteredList); // Added to add IPL suffix if missing in old List of illustration
        int N = preprocessed.size();
        if(N==0)
            return Optional.of(null);
        ArrayList<String> ans = new ArrayList<>();
        ans.add(preprocessed.get(0));
        LoiItem prevLoiItem = getNumberTitleAndPageNumberofFirstLine(preprocessed.get(0));
        for(int i=1; i<N; i++){
            if(prevLoiItem.getPageNumber() == null){
                ans.set(ans.size()-1, ans.get(ans.size()-1)+" " + preprocessed.get(i));
            }
            else
                ans.add(preprocessed.get(i));
            prevLoiItem = getNumberTitleAndPageNumberofFirstLine(ans.get(ans.size()-1));

        }

        return Optional.of(ans);
    }
//    public Optional<ArrayList<LoiPageblockItem>> getPageblockwiseIllustrations(ArrayList<String> formattedLoi){
//        ArrayList<LoiPageblockItem> ans = new ArrayList<>();
//        LoiPageblockItem prev = null;
//        for(int i=0; i<formattedLoi.size(); i++){
//            String str = formattedLoi.get(i);
//            LoiItem loiItem = getNumberTitleAndPageNumberofFirstLine(str);
//            if(prev == null || !prev.getPageblockName().equals(loiItem.getPageblock())){
//               LoiPageblockItem loiPageblockItem = new LoiPageblockItem(loiItem.getPageblock());
//               loiPageblockItem.getListOfIllustrations().add(loiItem);
//               prev = loiPageblockItem;
//               ans.add(prev);
//            }
//            else
//                prev.getListOfIllustrations().add(loiItem);
//        }
//        return Optional.of(ans);
//    }
    public Optional<HashMap<String, LoiPageblockItem>> getPageblockwiseIllustrations(ArrayList<String> formattedLoi){
        HashMap<String, LoiPageblockItem> ans = new HashMap<>();
        for(int i=0; i<formattedLoi.size(); i++){
            String str = formattedLoi.get(i);
            LoiItem loiItem = getNumberTitleAndPageNumberofFirstLine(str);
            if(ans.containsKey(loiItem.getPageblock())){
                ans.get(loiItem.getPageblock()).getListOfIllustrations().add(loiItem);
            }
            else{
                LoiPageblockItem loiPageblockItem = new LoiPageblockItem(loiItem.getPageblock());
                loiPageblockItem.getListOfIllustrations().add(loiItem);
                ans.put(loiItem.getPageblock(), loiPageblockItem);
            }
        }
        return Optional.of(ans);
    }
    public Optional<ArrayList<String>> compareLoi(HashMap<String, LoiPageblockItem> oldLoi, HashMap<String, LoiPageblockItem> newLoi){
        ArrayList<String> ans = new ArrayList<>();
        for(String s: newLoi.keySet()){
            if(oldLoi.containsKey(s)){
                LoiPageblockItem oldLoiPageblockItem = oldLoi.get(s);
                LoiPageblockItem newLoiPageblockItem = newLoi.get(s);
                ArrayList<LoiItem> oldListOfIllus = oldLoiPageblockItem.getListOfIllustrations();
                ArrayList<LoiItem> newListOfIllus = newLoiPageblockItem.getListOfIllustrations();
                int n = oldListOfIllus.size();
                int m = newListOfIllus.size();
                int i=0, j=0;
                while(i<n && j<m){
                    LoiItem loiItemOld = oldListOfIllus.get(i);
                    LoiItem loiItemNew = newListOfIllus.get(j);
                    if(oldListOfIllus.get(i).getFigureTitle().equals(newListOfIllus.get(j).getFigureTitle())){
                        if(!oldListOfIllus.get(i).getPageNumber().equals(newListOfIllus.get(j).getPageNumber())){
                            ans.add("Add Revbar for: {"
                                    + loiItemNew.getPageblock()
                                    +"} <" + loiItemNew.getFigureNumber() +"> |"
                                    + loiItemNew.getFigureTitle() + "| Page number got changed from " +
                                    loiItemOld.getPageNumber()+
                                    " to "
                                    + loiItemNew.getPageNumber() + ".");
                        }

                    }
                    else{
                        ans.add("Add Revbar for: {"
                                + loiItemNew.getPageblock()
                                +"} Title changed from " +loiItemOld.getFigureNumber() + " " + loiItemOld.getFigureTitle() +
                                " to <" + loiItemNew.getFigureNumber() +"> |"
                                + loiItemNew.getFigureTitle() + "|.");
                    }
                    i++;
                    j++;

                }
                while(j<m){
                    LoiItem loiItem = newListOfIllus.get(j);
                    ans.add("Add Revbar for: {"
                            + loiItem.getPageblock()
                            +"} Fig.No: <" + loiItem.getFigureNumber() +"> |"
                            + loiItem.getFigureTitle() + "|.");
                    j++;
                }

            }
            else{
                LoiPageblockItem loiPageblockItem = newLoi.get(s);
                ArrayList<LoiItem> listOfIllustrations = loiPageblockItem.getListOfIllustrations();
                for(LoiItem loiItem: listOfIllustrations){
                    ans.add("Add Revbar for: {"
                            + loiItem.getPageblock()
                            +"} Fig.No: <" + loiItem.getFigureNumber() +"> |"
                            + loiItem.getFigureTitle() + "|.");
                }
            }
        }
        return Optional.of(ans);
    }
    public void compareLoiAutomation(HashMap<String, LoiPageblockItem> oldLoi, HashMap<String, LoiPageblockItem> newLoi){
        ArrayList<String> ans = new ArrayList<>();
        for(String s: newLoi.keySet()){
            if(oldLoi.containsKey(s)){
                LoiPageblockItem oldLoiPageblockItem = oldLoi.get(s);
                LoiPageblockItem newLoiPageblockItem = newLoi.get(s);
                ArrayList<LoiItem> oldListOfIllus = oldLoiPageblockItem.getListOfIllustrations();
                ArrayList<LoiItem> newListOfIllus = newLoiPageblockItem.getListOfIllustrations();
                int n = oldListOfIllus.size();
                int m = newListOfIllus.size();
                int i=0, j=0;
                while(i<n && j<m){
                    LoiItem loiItemOld = oldListOfIllus.get(i);
                    LoiItem loiItemNew = newListOfIllus.get(j);
                    if(oldListOfIllus.get(i).getFigureTitle().equals(newListOfIllus.get(j).getFigureTitle())){
                        if(!oldListOfIllus.get(i).getPageNumber().equals(newListOfIllus.get(j).getPageNumber())){
                            ans.add("Add Revbar for: {"
                                    + loiItemNew.getPageblock()
                                    +"} <" + loiItemNew.getFigureNumber() +"> |"
                                    + loiItemNew.getFigureTitle() + "| Page number got changed from " +
                                    loiItemOld.getPageNumber()+
                                    " to "
                                    + loiItemNew.getPageNumber() + ".");
                        }

                    }
                    else{
                        ans.add("Add Revbar for: {"
                                + loiItemNew.getPageblock()
                                +"} Title changed from " +loiItemOld.getFigureNumber() + " " + loiItemOld.getFigureTitle() +
                                " to <" + loiItemNew.getFigureNumber() +"> |"
                                + loiItemNew.getFigureTitle() + "|.");
                    }
                    i++;
                    j++;

                }
                while(j<m){
                    LoiItem loiItem = newListOfIllus.get(j);
                    ans.add("Add Revbar for: {"
                            + loiItem.getPageblock()
                            +"} Fig.No: <" + loiItem.getFigureNumber() +"> |"
                            + loiItem.getFigureTitle() + "|.");
                    j++;
                }

            }
            else{
                LoiPageblockItem loiPageblockItem = newLoi.get(s);
                ArrayList<LoiItem> listOfIllustrations = loiPageblockItem.getListOfIllustrations();
                for(LoiItem loiItem: listOfIllustrations){
                    ans.add("Add Revbar for: {"
                            + loiItem.getPageblock()
                            +"} Fig.No: <" + loiItem.getFigureNumber() +"> |"
                            + loiItem.getFigureTitle() + "|.");
                }
            }
        }
    }
    public void addIntoTheMap(HashMap<String, ArrayList<FmChangeItem>> map, FmChangeItem fmChangeItem){
        if(map.containsKey(fmChangeItem.getPageblock())){
            map.get(fmChangeItem.getPageblock()).add(fmChangeItem);
        }
        else{
            ArrayList<FmChangeItem> list = new ArrayList<>();
            list.add(fmChangeItem);
            map.put(fmChangeItem.getPageblock(), list);
        }
    }
}

