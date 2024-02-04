package backend.frontmatterapi.services;
import backend.frontmatterapi.models.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class LOTComparisonService {
    UtilityFunctions utilityFunctions = new UtilityFunctions();
    public Optional<ArrayList<String>> filterUnwantedLinesFromLatestLotLoi(List<String> lines){
        int N = lines.size();
        ArrayList<String> finalList = new ArrayList<>();
        for(int i=0; i<N; i++){
            String s = lines.get(i);
            if(s.contains("cover page") || s.contains("COLLINS")
                    || s.contains("PART OF") || s.contains("COMPONENT")
                    || s.contains("TABLE")  || s.contains("US Export")
                    || s.contains("VOLUME") || s.contains("LOT-") || s.contains("PAGE") || s.contains("LIST OF TABLES"))
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
    public Optional<HashMap<String, LotPageblockItem>> getPageblockwiseTables(ArrayList<String> formattedLot){
        HashMap<String, LotPageblockItem> ans = new HashMap<>();
        for(int i=0; i<formattedLot.size(); i++){
            String str = formattedLot.get(i);
            LotItem lotItem = getLoiItemFromSingleLineString(str);
            if(ans.containsKey(lotItem.getPageblock())){
                ans.get(lotItem.getPageblock()).getListOfTables().add(lotItem);
            }
            else{
                LotPageblockItem lotPageblockItem = new LotPageblockItem(lotItem.getPageblock());
                lotPageblockItem.getListOfTables().add(lotItem);
                ans.put(lotItem.getPageblock(), lotPageblockItem);
            }
        }
        return Optional.of(ans);
    }
    public Optional<ArrayList<String>> compareLot(HashMap<String, LotPageblockItem> oldLot, HashMap<String, LotPageblockItem> newLot){
        ArrayList<String> ans = new ArrayList<>();
        for(String s: newLot.keySet()){
            if(oldLot.containsKey(s)){
                LotPageblockItem oldLotPageblockItem = oldLot.get(s);
                LotPageblockItem newLotPageblockItem = newLot.get(s);
                ArrayList<LotItem> oldListOfTables = oldLotPageblockItem.getListOfTables();
                ArrayList<LotItem> newListOfTables = newLotPageblockItem.getListOfTables();
                int n = oldListOfTables.size();
                int m = newListOfTables.size();
                int i=0, j=0;
                while(i<n && j<m){
                    LotItem loiItemOld = oldListOfTables.get(i);
                    LotItem loiItemNew = newListOfTables.get(j);
                    if(oldListOfTables.get(i).getTableTitle().equals(newListOfTables.get(j).getTableTitle())){
                        if(!oldListOfTables.get(i).getPageNumber().equals(newListOfTables.get(j).getPageNumber())){
                            ans.add("Add Revbar for: {"
                                    + loiItemNew.getPageblock()
                                    +"} <" + loiItemNew.getTableNumber() +"> |"
                                    + loiItemNew.getTableTitle() + "| Page number got changed from " +
                                    loiItemOld.getPageNumber()+
                                    " to "
                                    + loiItemNew.getPageNumber() + ".");
                        }

                    }
                    else{
                        ans.add("Add Revbar for: {"
                                + loiItemNew.getPageblock()
                                +"} Title changed from " +loiItemOld.getTableNumber() + " " + loiItemOld.getTableTitle() +
                                " to <" + loiItemNew.getTableNumber() +"> |"
                                + loiItemNew.getTableTitle() + "|.");
                    }
                    i++;
                    j++;

                }
                while(j<m){
                    LotItem lotItem = newListOfTables.get(j);
                    ans.add("Add Revbar for: {"
                            + lotItem.getPageblock()
                            +"} Fig.No: <" + lotItem.getTableNumber() +"> |"
                            + lotItem.getTableTitle() + "|.");
                    j++;
                }

            }
            else{
                LotPageblockItem lotPageblockItem = newLot.get(s);
                ArrayList<LotItem> listOfTables = lotPageblockItem.getListOfTables();
                for(LotItem lotItem: listOfTables){
                    ans.add("Add Revbar for: {"
                            + lotItem.getPageblock()
                            +"} Fig.No: <" + lotItem.getTableNumber() +"> |"
                            + lotItem.getTableTitle() + "|.");
                }
            }
        }
        return Optional.of(ans);
    }
    public void compareLotAutomation(HashMap<String, LotPageblockItem> oldLot, HashMap<String, LotPageblockItem> newLot, HashMap<String, ArrayList<FmChangeItem>> map){
        for(String s: newLot.keySet()){
            if(oldLot.containsKey(s)){
                LotPageblockItem oldLotPageblockItem = oldLot.get(s);
                LotPageblockItem newLotPageblockItem = newLot.get(s);
                ArrayList<LotItem> oldListOfTables = oldLotPageblockItem.getListOfTables();
                ArrayList<LotItem> newListOfTables = newLotPageblockItem.getListOfTables();
                int n = oldListOfTables.size();
                int m = newListOfTables.size();
                int i=0, j=0;
                while(i<n && j<m){
                    LotItem lotItemOld = oldListOfTables.get(i);
                    LotItem lotItemNew = newListOfTables.get(j);
                    if(lotItemOld.getTableTitle().toLowerCase().contains("delete")){
                        i++;
                        if(lotItemNew.getTableTitle().toLowerCase().contains("delete")){
                            //This "if" condition is added to support the Incremental revision, where the pageblock is not impacted we
                            //won't remove the deleted tables.
                            j++;
                        }
                    }
                    else if(lotItemNew.getTableTitle().toLowerCase().contains("delete")){
                        i++;
                        j++;
                    }
                    else if (lotItemOld.getTableNumber().equals(lotItemNew.getTableNumber())) {
                        if (!lotItemOld.getPageNumber().equals(lotItemNew.getPageNumber())) {
                            addIntoTheMap(map, fmChangeItemBuilder(lotItemNew));
                        }
                        i++;
                        j++;
                    } else if (!(lotItemOld.getTableNumber().equals(lotItemNew.getTableNumber()))) {
                        addIntoTheMap(map, fmChangeItemBuilder(lotItemNew));
                        i++;
                        j++;
                    }

                }
                while(j<m){
                    LotItem lotItemNew = newListOfTables.get(j);
                    if(lotItemNew.getTableNumber().toLowerCase().contains("delete"))
                        addIntoTheMap(map, fmChangeItemBuilder(lotItemNew));
                    j++;
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
    private FmChangeItem fmChangeItemBuilder(LotItem lotItemNew){
        FmChangeItem fmChangeItem = new FmChangeItem();
        fmChangeItem.setChangeType(ChangeType.PAGE_NUM_CHANGE);
        fmChangeItem.setLotItem(lotItemNew);
        fmChangeItem.setPageblock(lotItemNew.getPageblock());
        fmChangeItem.setFrontMatterType(FrontMatterType.TABLE);
        return fmChangeItem;
    }
    public Optional<ArrayList<String>> makeMultilineTitlesSinglelineVersion1(ArrayList<String> filteredList){
        if(filteredList.isEmpty())
            return Optional.empty();
        int N = filteredList.size();
        LotItem nextLine = getLotItemFromLastLine(filteredList.get(N-1).trim());
        Stack<String> list = new Stack<>();
        list.push(filteredList.get(N-1).trim());
        for(int i=N-2; i>=0; i--){
            if(nextLine.getTableNumber() == null){
                String mergedLine = filteredList.get(i).trim() + " " + list.pop();
                nextLine = getLotItemFromLastLine(mergedLine);
                list.push(mergedLine);
            }
            else{
                list.push(filteredList.get(i).trim());
                nextLine = getLotItemFromLastLine(filteredList.get(i).trim());
            }

        }
        ArrayList<String> ans = new ArrayList<>();
        while(!list.isEmpty()){
            ans.add(list.pop());
        }
        return Optional.of(ans);
    }
    public LotItem getLotItemFromLastLine(String s){
        LotItem lotItem = new LotItem();
        int N = s.length();
        int i = N-1;
        while(i>=0){
            if(s.charAt(i) == ' '){
                lotItem.setPageNumber(s.substring(i+1).trim());
                break;
            }
            i--;
        }
        lotItem.setPageblock(utilityFunctions.getPageBlockFromPageNumber(lotItem.getPageNumber()));
        PgblckRegexPattern pgblckRegexPattern = utilityFunctions.getPatternByPageblock(lotItem.getPageblock());
        for(String pattern: pgblckRegexPattern.getPatterns()){
            String tableNumber = utilityFunctions.getMatchingSubstring(s, pattern);
            if(tableNumber != null){
                lotItem.setTableNumber(tableNumber);
                break;
            }
        }
        return lotItem;
    }
    public LotItem getLoiItemFromSingleLineString(String str){ // This Method returns a Lot item from a String which has all 3 information.
        int i=0;
        LotItem lotItem = new LotItem();
        if(str.startsWith("IPL "))
            i = 4;
        while(i < str.length()){
            if(str.charAt(i) == ' '){
                lotItem.setTableNumber(str.substring(0, i));
                i++;
                break;
            }
            i++;
        }
        int j = str.length()-1;
        while(j>i){
            if(str.charAt(j) == ' '){
                lotItem.setPageNumber(str.substring(j+1));
                j--;
                break;
            }
            j--;
        }
        while(j>i){
            if(str.charAt(j) != '.')
                break;
            j--;
        }
        lotItem.setTableTitle(utilityFunctions.removeExtraSpaceFromTitle(str.substring(i, j+1).trim()));
        lotItem.setPageblock(utilityFunctions.getPageBlockFromPageNumber(lotItem.getPageNumber()));
        return lotItem;
    }
}
