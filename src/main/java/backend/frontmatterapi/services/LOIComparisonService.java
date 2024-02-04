package backend.frontmatterapi.services;

import backend.frontmatterapi.models.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LOIComparisonService {
    UtilityFunctions utilityFunctions = new UtilityFunctions();
    public Optional<ArrayList<String>> filterUnwantedLinesFromLatestLotLoi(List<String> lines) {
        int N = lines.size();
        ArrayList<String> finalList = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            String s = lines.get(i);
            if (s.contains("cover page") || s.contains("COLLINS")
                    || s.contains("PART OF") || s.contains("COMPONENT")
                    || s.contains("LIST OF ILLUSTRATIONS") || s.contains("TABLE") || s.contains("US Export")
                    || s.contains("VOLUME") || s.contains("LOI-") || s.contains("PAGE")) {
                continue;
            }
            String regex = "\\b(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s(?:0[1-9]|[12][0-9]|3[01])/(?:0[1-9]|1[0-9]|2[0-9]|3[01])\\b";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(s);
            if (matcher.find())
                continue;
            finalList.add(s);
        }

        return Optional.of(finalList);
    }
    public Optional<HashMap<String, LoiPageblockItem>> getPageblockwiseIllustrations(ArrayList<String> formattedLoi) {
        HashMap<String, LoiPageblockItem> ans = new HashMap<>();
        for (int i = 0; i < formattedLoi.size(); i++) {
            String str = formattedLoi.get(i);
            LoiItem loiItem = getLoiItemFromSingleLineString(str);
            if (ans.containsKey(loiItem.getPageblock())) {
                ans.get(loiItem.getPageblock()).getListOfIllustrations().add(loiItem);
            } else {
                LoiPageblockItem loiPageblockItem = new LoiPageblockItem(loiItem.getPageblock());
                loiPageblockItem.getListOfIllustrations().add(loiItem);
                ans.put(loiItem.getPageblock(), loiPageblockItem);
            }
        }
       //utilityFunctions.ExcelWriter(ans.get("SCHEMATIC AND WIRING DIAGRAMS").getListOfIllustrations());
        return Optional.of(ans);
    }

    public Optional<ArrayList<String>> compareLoi(HashMap<String, LoiPageblockItem> oldLoi, HashMap<String, LoiPageblockItem> newLoi) {
        ArrayList<String> ans = new ArrayList<>();
        for (String s : newLoi.keySet()) {
            if (oldLoi.containsKey(s)) {
                LoiPageblockItem oldLoiPageblockItem = oldLoi.get(s);
                LoiPageblockItem newLoiPageblockItem = newLoi.get(s);
                ArrayList<LoiItem> oldListOfIllus = oldLoiPageblockItem.getListOfIllustrations();
                ArrayList<LoiItem> newListOfIllus = newLoiPageblockItem.getListOfIllustrations();
                int n = oldListOfIllus.size();
                int m = newListOfIllus.size();
                int i = 0, j = 0;
                while (i < n && j < m) {
                    LoiItem loiItemOld = oldListOfIllus.get(i);
                    LoiItem loiItemNew = newListOfIllus.get(j);
                    if (oldListOfIllus.get(i).getFigureTitle().equals(newListOfIllus.get(j).getFigureTitle())) {
                        if (!oldListOfIllus.get(i).getPageNumber().equals(newListOfIllus.get(j).getPageNumber())) {
                            ans.add("Add Revbar for: {"
                                    + loiItemNew.getPageblock()
                                    + "} <" + loiItemNew.getFigureNumber() + "> |"
                                    + loiItemNew.getFigureTitle() + "| Page number got changed from " +
                                    loiItemOld.getPageNumber() +
                                    " to "
                                    + loiItemNew.getPageNumber() + ".");
                        }

                    } else {
                        ans.add("Add Revbar for: {"
                                + loiItemNew.getPageblock()
                                + "} Title changed from " + loiItemOld.getFigureNumber() + " " + loiItemOld.getFigureTitle() +
                                " to <" + loiItemNew.getFigureNumber() + "> |"
                                + loiItemNew.getFigureTitle() + "|.");
                    }
                    i++;
                    j++;

                }
                while (j < m) {
                    LoiItem loiItem = newListOfIllus.get(j);
                    ans.add("Add Revbar for: {"
                            + loiItem.getPageblock()
                            + "} Fig.No: <" + loiItem.getFigureNumber() + "> |"
                            + loiItem.getFigureTitle() + "|.");
                    j++;
                }

            } else {
                LoiPageblockItem loiPageblockItem = newLoi.get(s);
                ArrayList<LoiItem> listOfIllustrations = loiPageblockItem.getListOfIllustrations();
                for (LoiItem loiItem : listOfIllustrations) {
                    ans.add("Add Revbar for: {"
                            + loiItem.getPageblock()
                            + "} Fig.No: <" + loiItem.getFigureNumber() + "> |"
                            + loiItem.getFigureTitle() + "|.");
                }
            }
        }
        return Optional.of(ans);
    }

    public void compareLoiAutomation(HashMap<String, LoiPageblockItem> oldLoi, HashMap<String, LoiPageblockItem> newLoi, HashMap<String, ArrayList<FmChangeItem>> map) {
        for (String s : newLoi.keySet()) {
            if (oldLoi.containsKey(s)) {
                LoiPageblockItem oldLoiPageblockItem = oldLoi.get(s);
                LoiPageblockItem newLoiPageblockItem = newLoi.get(s);
                ArrayList<LoiItem> oldListOfIllus = oldLoiPageblockItem.getListOfIllustrations();
                ArrayList<LoiItem> newListOfIllus = newLoiPageblockItem.getListOfIllustrations();
                int n = oldListOfIllus.size();
                int m = newListOfIllus.size();
                int i = 0, j = 0;
                while (i < n && j < m) {
                    LoiItem loiItemOld = oldListOfIllus.get(i);
                    LoiItem loiItemNew = newListOfIllus.get(j);
                    if (loiItemOld.getFigureTitle().toLowerCase().contains("delete")) {
                        i++;
                        if (loiItemNew.getFigureTitle().toLowerCase().contains("delete")) {
                            //This "if" condition is added to support the Incremental revision, where the pageblock is not impacted we
                            //won't remove the deleted tables.
                            j++;
                        }
                    }
                    else if(loiItemNew.getFigureTitle().toLowerCase().contains("delete")){
                        i++;
                        j++;
                    }
                    else if (loiItemOld.getFigureNumber().equals(loiItemNew.getFigureNumber())) {
                        if (!loiItemOld.getPageNumber().equals(loiItemNew.getPageNumber())) {
                            addIntoTheMap(map, fmChangeItemBuilder(loiItemNew));
                        }
                        i++;
                        j++;
                    } else if (!(loiItemOld.getFigureNumber().equals(loiItemNew.getFigureNumber()))) {
                        addIntoTheMap(map, fmChangeItemBuilder(loiItemNew));
                        i++;
                        j++;
                    }
                }
                while (j < m) {
                    LoiItem loiItemNew = newListOfIllus.get(j);
                    if(!loiItemNew.getFigureTitle().toLowerCase().contains("delete"))
                        addIntoTheMap(map, fmChangeItemBuilder(loiItemNew));
                    j++;
                }
            }
        }
    }

    public void addIntoTheMap(HashMap<String, ArrayList<FmChangeItem>> map, FmChangeItem fmChangeItem) {
        if (map.containsKey(fmChangeItem.getPageblock())) {
            map.get(fmChangeItem.getPageblock()).add(fmChangeItem);
        } else {
            ArrayList<FmChangeItem> list = new ArrayList<>();
            list.add(fmChangeItem);
            map.put(fmChangeItem.getPageblock(), list);
        }
    }
    private FmChangeItem fmChangeItemBuilder(LoiItem loiItemNew) {
        FmChangeItem fmChangeItem = new FmChangeItem();
        fmChangeItem.setChangeType(ChangeType.PAGE_NUM_CHANGE);
        fmChangeItem.setLoiItem(loiItemNew);
        fmChangeItem.setPageblock(loiItemNew.getPageblock());
        fmChangeItem.setFrontMatterType(FrontMatterType.FIGURE);
        return fmChangeItem;
    }
    public Optional<ArrayList<String>> makeMultilineTitlesSinglelineVersion1(ArrayList<String> filteredList) { //This Method Helps Merge multiline to a single line.
        if (filteredList.size() == 0)
            return Optional.empty();
        int N = filteredList.size();
        LoiItem nextLine = getLoiItemFromLastLine(filteredList.get(N - 1).trim());
        Stack<String> list = new Stack<>();
        list.push(filteredList.get(N - 1).trim());
        for (int i = N - 2; i >= 0; i--) {
            if (nextLine.getFigureNumber() == null) {
                String mergedLine = filteredList.get(i).trim() + " " + list.pop();
                nextLine = getLoiItemFromLastLine(mergedLine);
                list.push(mergedLine);
            } else {
                list.push(filteredList.get(i).trim());
                nextLine = getLoiItemFromLastLine(filteredList.get(i).trim());
            }

        }
        ArrayList<String> ans = new ArrayList<>();
        while (list.size() > 0) {
            ans.add(list.pop());
        }
        return Optional.of(ans);
    }

    public LoiItem getLoiItemFromLastLine(String s) { //This method returns a Loi item from a String with either with all 3, or with title and pageNumber
        LoiItem loiItem = new LoiItem();
        int N = s.length();
        int i = N - 1;
        while (i >= 0) {
            if (s.charAt(i) == ' ') {
                loiItem.setPageNumber(s.substring(i + 1).trim());
                break;
            }
            i--;
        }
        loiItem.setPageblock(utilityFunctions.getPageBlockFromPageNumber(loiItem.getPageNumber()));
        PgblckRegexPattern pgblckRegexPattern = utilityFunctions.getPatternByPageblock(loiItem.getPageblock());
        for (String pattern : pgblckRegexPattern.getPatterns()) {
            String figurenumber = utilityFunctions.getMatchingSubstring(s, pattern);
            if (figurenumber != null) {
                loiItem.setFigureNumber(figurenumber);
                break;
            }
        }
        return loiItem;
    }
    public LoiItem getLoiItemFromSingleLineString(String str){ // This Method returns a Loi item from a String which has all 3 information.
        int i=0;
        LoiItem loiItem = new LoiItem();
        if(str.startsWith("IPL "))
            i = 4;
        while(i < str.length()){
            if(str.charAt(i) == ' '){
                loiItem.setFigureNumber(str.substring(0, i));
                i++;
                break;
            }
            i++;
        }
        int j = str.length()-1;
        while(j>i){
            if(str.charAt(j) == ' '){
                loiItem.setPageNumber(str.substring(j+1));
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
        loiItem.setFigureTitle(utilityFunctions.removeExtraSpaceFromTitle(str.substring(i, j+1).trim()));
        loiItem.setPageblock(utilityFunctions.getPageBlockFromPageNumber(loiItem.getPageNumber()));
        return loiItem;
    }


}

