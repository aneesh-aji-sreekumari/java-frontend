package backend.frontmatterapi.services;
import backend.frontmatterapi.models.ChangeLists;
import backend.frontmatterapi.models.FmChangeItem;
import backend.frontmatterapi.models.FrontMatterType;
import backend.taggenerator.PreProcessing;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutomationService {
    public final HashMap<String, String> filenamesMap;

    public AutomationService() {
        filenamesMap = new HashMap<>();
        filenamesMap.put("DESCRIPTION AND OPERATION", "");
        filenamesMap.put("TESTING AND FAULT ISOLATION", "");
        filenamesMap.put("SCHEMATIC AND WIRING DIAGRAMS", "");
        filenamesMap.put("DISASSEMBLY", "");
        filenamesMap.put("CLEANING", "");
        filenamesMap.put("INSPECTION/CHECK", "");
        filenamesMap.put("REPAIR", "");
        filenamesMap.put("ASSEMBLY", "");
        filenamesMap.put("FITS AND CLEARANCES", "");
        filenamesMap.put("SPECIAL TOOLS, FIXTURES, EQUIPMENT, AND CONSUMABLES", "");
        filenamesMap.put("ILLUSTRATED PARTS LIST", "");
        filenamesMap.put("SPECIAL PROCEDURES", "");
        filenamesMap.put("REMOVAL", "");
        filenamesMap.put("INSTALLATION", "");
        filenamesMap.put("SERVICING", "");
        filenamesMap.put("STORAGE INCLUDING TRANSPORTATION", "");
        filenamesMap.put("REWORK", "");
    }

    public void addPageNumChangeDateToRequiredTitles(HashMap<String, ArrayList<FmChangeItem>> map, String folderPath,
                                                     String revDate) {
        int val = checkIfAllRequiredFilesAreAvailable(map, folderPath);
        if(val == -1){
            //Handle the case of error
        }
        else if(val == 0){
            //Handle the case when the required Files are not available in the folder path
        }
        //Following lines of code will execute if all the required files are available
        for (String s : map.keySet()) {
            Optional<String> pageBlockContents = readSgmlAndConvertItToString(folderPath +  "\\" + filenamesMap.get(s));
            if(pageBlockContents.isEmpty()){
                //Show some error alert to user
                //Make the user to start again
                return;
            }
            //Let Proceed to add the Pagenum date to the respective titles and return the new String
            Optional<String> pageblockOutput = addRevBarForRequiredTitlesOfAPageblock(map.get(s), pageBlockContents.get(), revDate);
            if (pageblockOutput.isEmpty()){
                //Show some error alert to user
                //Make the user to start again
                return;
            }
            boolean outPutResult = saveFileAfterModification(folderPath, pageblockOutput.get(), filenamesMap.get(s));
            //Add OutputComments in a file if required
        }
    }

    public void replaceAddedPageNumChangeDateWithPrevRevisionDate(String path) {

    }

    public void removeNewlyAddedDates(String path) {

    }

    public void takeBackupCopy(String path) {

    }

    public int checkIfAllRequiredFilesAreAvailable(HashMap<String, ArrayList<FmChangeItem>> map, String folderPath)  {
        try {
            Set<String> listOfAvailableFiles = listFilesInFolder(folderPath);
            for (String s : map.keySet()) {
                if(!listOfAvailableFiles.contains(map.get(s))){
                    return 0;
                }
            }
        } catch (IOException e) {
            return -1;

        }

        return 1;
    }
    private static Set<String> listFilesInFolder(String folderPath) throws IOException {
        try (Stream<Path> walk = Files.walk(Paths.get(folderPath), FileVisitOption.FOLLOW_LINKS)) {
            return walk
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toCollection(HashSet::new));
        }
    }
    public Optional<String> addRevBarForRequiredTitlesOfAPageblock(ArrayList<FmChangeItem> list, String input, String revDate){
        ChangeLists changeLists = new ChangeLists(list);
        Optional<StringBuilder> tocOutput = addRevisionBarForTOCItems(input, changeLists.getTocList(), revDate);
        if(tocOutput.isEmpty())
            return Optional.empty();
        Optional<StringBuilder> loiOutput = addRevisionBarForFigures(tocOutput.get(), changeLists.getFiguresList(), revDate);
        if (loiOutput.isEmpty())
            return Optional.empty();
        Optional<StringBuilder> lotOutput = addRevisionBarForTables(loiOutput.get(), changeLists.getTablesList(), revDate);
        if (lotOutput.isEmpty())
            return Optional.empty();
        return Optional.of(lotOutput.toString());
    }
    public Optional<String> readSgmlAndConvertItToString(String filePath){
        try {
            // Read the contents of the file into a byte array
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            // Convert the byte array to a String using the default charset (UTF-8)
            String fileContent = new String(fileBytes);
            return Optional.of(fileContent);
        } catch (IOException e) {
            return Optional.of(null);
        }
    }
    public boolean saveFileAfterModification(String folderPath, String output, String fileName){
        String targetPath = folderPath + "\\" + fileName;
        try {
            Path path = Paths.get(targetPath);
            //System.out.println(stringArrayList);
            Files.write(path, output.getBytes());
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    public Optional<StringBuilder> addRevisionBarForTOCItems(String input, ArrayList<FmChangeItem> list, String revDate){
        if(list.isEmpty())
            return Optional.of(new StringBuilder(input));
        int N = input.length();
        int M = list.size();
        int j = 0;
        StringBuilder sb = new StringBuilder();
        int taskCnt = 0;
        int subtaskCnt = 0;
        for(int i=0; i<N; i++) {
            FmChangeItem fmChangeItem = list.get(j);
            char ch = input.charAt(i);
            if (ch == '<' && PreProcessing.checkCurrentTag(input, N, i + 1, "task") == 1) {
                subtaskCnt = 0;
                taskCnt += 1;
                int currTaskNumber = Integer.parseInt(fmChangeItem.getSubTopic().number);
                int[] ttltagIdx = PreProcessing.getStartAndEndIndexOfTag(input.substring(i, N), "title");
                if (fmChangeItem.getFrontMatterType().equals(FrontMatterType.SUB_TOPIC) && taskCnt == currTaskNumber) {
                    sb.append(updatePagenumDateToTitleTag(input.substring(ttltagIdx[0], ttltagIdx[1]+1), revDate));
                    //String updatedTitleTag = addpagenumDatetoTitleTag();
                    sb.append(input.substring(i, ttltagIdx[0]));
                    sb.append("updatedTitleTag"); // Need to be edited
                    j++;
                } else {
                    sb.append(input.substring(i, ttltagIdx[1] + 1));
                }
                i = ttltagIdx[1];
            } else if (ch == '<' && PreProcessing.checkCurrentTag(input, N, i + 1, "subtask") == 1) {
                subtaskCnt += 1;
                int[] subtaskInd = PreProcessing.getStartAndEndIndexOfTag(input.substring(i, N), "subtask");
                if (fmChangeItem.getFrontMatterType().equals(FrontMatterType.SUB_SUB_TOPIC)
                        && getSubSubTopicNumberIntegerValue(fmChangeItem.getSubSubTopic().number) == subtaskCnt
                        && Integer.parseInt(fmChangeItem.getSubTopic().number) == taskCnt) {
                    int[] titleInd = PreProcessing.getStartAndEndIndexOfTag(input.substring(i, N), "title");
                    sb.append(input.substring(i, titleInd[0]));
                    sb.append(updatePagenumDateToTitleTag(input.substring(titleInd[0], titleInd[1]+1), revDate));
                    sb.append(input.substring(titleInd[1] + 1, subtaskInd[1] + 1));
                    j++;
                } else {
                    sb.append(input.substring(i, subtaskInd[1] + 1));
                }
                i = subtaskInd[1];
            } else {
                sb.append(input.charAt(i));
            }
            if (j == M) {
                sb.append(input.substring(i + 1, N));
                break;
            }
        }
        return Optional.of(sb);
    }
    public Optional<StringBuilder> addRevisionBarForTables(StringBuilder input, ArrayList<FmChangeItem> list, String revDate){
        return null;
    }
    public Optional<StringBuilder> addRevisionBarForFigures(StringBuilder input, ArrayList<FmChangeItem> list, String revDate){
        return null;
    }
    public boolean titleMatcher(String titleFromTag, String tocTitle){
        StringBuilder sb = new StringBuilder();
        return true;
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
    public static int getSubSubTopicNumberIntegerValue(String s){
        int x = 0;
        int pow = 1;
        for(int i=s.length()-1; i>=0; i--){
            x = x + pow * (s.charAt(i)-'A' + 1);
            pow = pow * 26;
        }
        return x;
    }
    public String updatePagenumDateToTitleTag(String s, String revDate){
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
    private int getEndOfFmreLocDate(String s, int i) {
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

    private int[] getStartAndEndOfPagenumDate(String s, int i) {
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

