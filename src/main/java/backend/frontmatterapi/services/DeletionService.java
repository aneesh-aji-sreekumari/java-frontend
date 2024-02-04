package backend.frontmatterapi.services;

import backend.frontmatterapi.dtos.RevisedTitleChangeList;
import backend.frontmatterapi.dtos.RevisedTitleDto;
import backend.frontmatterapi.models.FmChangeItem;
import backend.frontmatterapi.models.FrontMatterType;
import backend.taggenerator.PreProcessing;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class DeletionService {
    AutomationService automationService = new AutomationService();
    public Optional<HashMap<String, String>> inputAfterDeletingPgnumDate(HashMap<String, RevisedTitleChangeList> changesRequired, HashMap<String, String> fileNamesMap, String wfPath){
        HashMap<String, String> inputMap = new HashMap<>();
        for (String s: changesRequired.keySet()){
            try{
                RevisedTitleChangeList revisedTitleChangeList = changesRequired.get(s);
                Optional<String> input = automationService.readSgmlAndConvertItToString(wfPath+"\\"+fileNamesMap.get(s));
                if(input.isEmpty())
                    return Optional.empty();
                Optional<StringBuilder> outputAfterTocDateRemooval = deleteRevisionBarForTOCItemsVersion1(input.get(), revisedTitleChangeList.getTocList());
                if(outputAfterTocDateRemooval.isEmpty())
                    return Optional.empty();
                Optional<StringBuilder> outputAfterLotDateRemooval = deleteRevisionBarForTablesVersion1(outputAfterTocDateRemooval.get(), revisedTitleChangeList.getLotList());
                if (outputAfterLotDateRemooval.isEmpty())
                    return Optional.empty();
                Optional<StringBuilder> outputAfterLoiDateRemooval = deleteRevisionBarForFiguresVersion1(outputAfterLotDateRemooval.get(), revisedTitleChangeList.getLoiList());
                if(outputAfterLoiDateRemooval.isEmpty())
                    return Optional.empty();
                inputMap.put(s, outputAfterLoiDateRemooval.get().toString());
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        return Optional.of(inputMap);
    }
    private Optional<StringBuilder> deleteRevisionBarForTOCItemsVersion1(String input, ArrayList<RevisedTitleDto> list) {
        if (list.isEmpty())
            return Optional.of(new StringBuilder(input));
        int N = input.length();
        int M = list.size();
        int j = 0;
        StringBuilder sb = new StringBuilder();
        try {
            // Create a BufferedReader to read the multiline string
            BufferedReader reader = new BufferedReader(new StringReader(input));
            String line;
            String subtopicTitle = null;
            while ((line = reader.readLine()) != null && j<M) {
                FmChangeItem fmChangeItem = list.get(j).getFmChangeItem();
                if (line.startsWith("<task")) {
                    sb.append(line).append("\n");
                    String nextLine = reader.readLine();
                    while(nextLine != null && !nextLine.contains("<title")){
                        sb.append(nextLine).append("\n");
                        nextLine = reader.readLine();
                    }
                    if(nextLine != null){
                        int[] ttlIndx = PreProcessing.getStartAndEndIndexOfTag(nextLine, "title", 0);
                        subtopicTitle = automationService.titleExtractor(PreProcessing.startingAndEndingOfAbsoluteTitle(nextLine.substring(ttlIndx[0], ttlIndx[1]+1)));
                        if(fmChangeItem.getFrontMatterType().equals(FrontMatterType.SUB_TOPIC)
                                && subtopicTitle.equals(fmChangeItem.getSubTopic().subject)){
                            String updatedTitleLine = removeOrReplacePgnumDateFromTitleTag(nextLine.substring(ttlIndx[0], ttlIndx[1]+1), list.get(j).getPgnumDate());
                            sb.append(nextLine.substring(0, ttlIndx[0])).append(updatedTitleLine).append(nextLine.substring(ttlIndx[1]+1)).append("\n");
                            j++;

                        }
                        else{
                            sb.append(nextLine).append("\n");
                        }
                    }
                }
                else if (line.startsWith("<subtask")) {
                    sb.append(line).append("\n");
                    String nextLine = reader.readLine();
                    while(nextLine != null && !nextLine.contains("<title")){
                        sb.append(nextLine).append("\n");
                        nextLine = reader.readLine();
                    }
                    if(nextLine != null){
                        int[] ttlIndx = PreProcessing.getStartAndEndIndexOfTag(nextLine, "title", 0);
                        String currTitle = automationService.titleExtractor(PreProcessing.startingAndEndingOfAbsoluteTitle(nextLine.substring(ttlIndx[0], ttlIndx[1]+1)));
                        if(fmChangeItem.getFrontMatterType().equals(FrontMatterType.SUB_SUB_TOPIC)
                                && subtopicTitle.equals(fmChangeItem.getSubTopic().subject)
                                && currTitle.equals(fmChangeItem.getSubSubTopic().subject)){
                            String updatedTitleLine = removeOrReplacePgnumDateFromTitleTag(nextLine.substring(ttlIndx[0], ttlIndx[1]+1), list.get(j).getPgnumDate());
                            sb.append(nextLine.substring(0, ttlIndx[0])).append(updatedTitleLine).append(nextLine.substring(ttlIndx[1]+1)).append("\n");
                            j++;
                        }
                        else{
                            sb.append(nextLine).append("\n");
                        }
                    }
                }
                else
                    sb.append(line).append("\n");
            }
            while(line != null){
                sb.append(line).append("\n");
                line = reader.readLine();
            }
            // Close the BufferedReader
            reader.close();
            return Optional.of(sb);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private Optional<StringBuilder> deleteRevisionBarForTablesVersion1(StringBuilder input, ArrayList<RevisedTitleDto> list) {
        if (list.isEmpty())
            return Optional.of(new StringBuilder(input));
        int N = input.length();
        int M = list.size();
        int j = 0;
        StringBuilder sb = new StringBuilder();
        try {
            // Create a BufferedReader to read the multiline string
            BufferedReader reader = new BufferedReader(new StringReader(input.toString()));
            String line;
            // String subtopicTitle = null;
            while ((line = reader.readLine()) != null && j<M) {
                FmChangeItem fmChangeItem = list.get(j).getFmChangeItem();
                if (line.startsWith("<table")) {
                    sb.append(line).append("\n");
                    String nextLine = reader.readLine();
                    while(nextLine != null && !nextLine.contains("<title")){
                        sb.append(nextLine).append("\n");
                        nextLine = reader.readLine();
                    }
                    if(nextLine != null){
                        int[] ttlIndx = PreProcessing.getStartAndEndIndexOfTag(nextLine, "title", 0);
                        String currTableTitle = automationService.titleExtractor(PreProcessing.startingAndEndingOfAbsoluteTitle(nextLine.substring(ttlIndx[0], ttlIndx[1]+1)));
                        if(fmChangeItem.getLotItem().getTableTitle().equals(currTableTitle)){
                            String updatedTitleLine = removeOrReplacePgnumDateFromTitleTag(nextLine.substring(ttlIndx[0], ttlIndx[1]+1), list.get(j).getPgnumDate());
                            sb.append(nextLine.substring(0, ttlIndx[0])).append(updatedTitleLine).append(nextLine.substring(ttlIndx[1]+1)).append("\n");
                            j++;
                        }
                        else{
                            sb.append(nextLine).append("\n");
                        }
                    }
                }
                else
                    sb.append(line).append("\n");
            }
            while(line != null){
                sb.append(line).append("\n");
                line = reader.readLine();
            }
            // Close the BufferedReader
            reader.close();
            return Optional.of(sb);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private Optional<StringBuilder> deleteRevisionBarForFiguresVersion1(StringBuilder input, ArrayList<RevisedTitleDto> list) {
        if (list.isEmpty())
            return Optional.of(new StringBuilder(input));
        int N = input.length();
        int M = list.size();
        int j = 0;
        StringBuilder sb = new StringBuilder();
        try {
            // Create a BufferedReader to read the multiline string
            BufferedReader reader = new BufferedReader(new StringReader(input.toString()));
            String line;
            while ((line = reader.readLine()) != null && j<M) {
                FmChangeItem fmChangeItem = list.get(j).getFmChangeItem();
                if (line.startsWith("<graphic")) {
                    sb.append(line).append("\n");
                    String nextLine = reader.readLine();
                    while(nextLine != null && !nextLine.contains("<title")){
                        sb.append(nextLine).append("\n");
                        nextLine = reader.readLine();
                    }
                    if(nextLine != null){
                        int[] ttlIndx = PreProcessing.getStartAndEndIndexOfTag(nextLine, "title", 0);
                        String currFigureTitle = automationService.titleExtractor(PreProcessing.startingAndEndingOfAbsoluteTitle(nextLine.substring(ttlIndx[0], ttlIndx[1]+1)));
                        if(fmChangeItem.getLoiItem().getFigureTitle().equals(currFigureTitle)){
                            String updatedFigureLine = removeOrReplacePgnumDateFromTitleTag(nextLine.substring(ttlIndx[0], ttlIndx[1]+1), list.get(j).getPgnumDate());
                            sb.append(nextLine.substring(0, ttlIndx[0])).append(updatedFigureLine).append(nextLine.substring(ttlIndx[1]+1)).append("\n");
                            j++;
                        }
                        else{
                            sb.append(nextLine).append("\n");
                        }
                    }
                }
                else
                    sb.append(line).append("\n");
            }
            while(line != null){
                sb.append(line).append("\n");
                line = reader.readLine();
            }
            // Close the BufferedReader
            reader.close();
            return Optional.of(sb);
        } catch (IOException e) {
            return Optional.empty();
        }
    }
    public String removeOrReplacePgnumDateFromTitleTag(String s, String revDate){
        int N = s.length();
        int i=0; int j=0;
        for(i=0; i<N; i++){
            if(s.charAt(i) == 'p'){
                break;
            }
        }
        int cntOfChar = 0;
        for(j=i+1; j<N; j++){
            if(s.charAt(j) == '"'){
                cntOfChar+=1;
                if(cntOfChar == 2){
                    break;
                }
            }
        }
        if(revDate == null){
            if(s.charAt(j+1) == '>')
                return s.substring(0,i-1)+s.substring(j+1);
        }
        return s.substring(0, i) + "pgnumdate=\"" + revDate + "\n" + s.substring(j+1);
    }
    public HashMap<String, String> filesPendingForSaving(HashMap<String, String> pgnumDateDeletedMap, HashMap<String, ArrayList<FmChangeItem>> fmChangeItemsMap) {
        HashMap<String, String> filesPending = new HashMap<>();
        for(String s: pgnumDateDeletedMap.keySet()){
            if(!fmChangeItemsMap.containsKey(s)){
                filesPending.put(s, pgnumDateDeletedMap.get(s));
            }
        }
        return filesPending;
    }

}
