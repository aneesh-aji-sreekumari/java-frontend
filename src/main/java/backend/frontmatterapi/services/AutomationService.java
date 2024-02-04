package backend.frontmatterapi.services;
import backend.frontmatterapi.dtos.RevisedTitleChangeList;
import backend.frontmatterapi.dtos.RevisedTitleDto;
import backend.frontmatterapi.models.*;
import backend.taggenerator.PreProcessing;
import javafx.scene.control.Alert;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
public class AutomationService {
    private HashMap<String, String> filenamesMap;
    public AutomationService() {
        filenamesMap = new HashMap<>();
        filenamesMap.put("INTRODUCTION", "Introduction");
        filenamesMap.put("DESCRIPTION AND OPERATION", "Description and Operation");
        filenamesMap.put("TESTING AND FAULT ISOLATION", "Testing and Fault Isolation");
        filenamesMap.put("SCHEMATIC AND WIRING DIAGRAMS", "Schematic and Wiring Diagrams");
        filenamesMap.put("DISASSEMBLY", "Disassembly");
        filenamesMap.put("CLEANING", "Cleaning");
        filenamesMap.put("INSPECTION/CHECK", "Inspection");
        filenamesMap.put("REPAIR", "Repair");
        filenamesMap.put("ASSEMBLY", " Assembly");
        filenamesMap.put("FITS AND CLEARANCES", "Fits and Clearances");
        filenamesMap.put("SPECIAL TOOLS, FIXTURES, EQUIPMENT AND CONSUMABLES", "Special Tools");
        filenamesMap.put("ILLUSTRATED PARTS LIST", "ipl");
        filenamesMap.put("SPECIAL PROCEDURES", "Special Procedures");
        filenamesMap.put("REMOVAL", "Removal");
        filenamesMap.put("INSTALLATION", "Installation");
        filenamesMap.put("SERVICING", "Servicing");
        filenamesMap.put("STORAGE INCLUDING TRANSPORTATION", "Storage Including");
        filenamesMap.put("REWORK", "Rework");
        filenamesMap.put("APPENDIX A", "Appendix");
    }

    public HashMap<String, String> getFilenamesMap() {
        return filenamesMap;
    }

    public void setFilenamesMap(HashMap<String, String> filenamesMap) {
        this.filenamesMap = filenamesMap;
    }

    public Optional<HashMap<String, OutputStringDto>> addPageNumChangeDateToRequiredTitles(HashMap<String, ArrayList<FmChangeItem>> map, String folderPath,
                                                                                           String revDate, HashMap<String, String> pgnumDateDeletedMap, Workbook workbook) {
        HashMap<String, OutputStringDto> outPutMap = new HashMap<>();
        int val = checkIfAllRequiredFilesAreAvailable(map, folderPath, filenamesMap);
        if (val == -1) {
            /* When there is an issue reading the files*/
            return Optional.empty();
        } else if (val == 0) {
            /* when the required Files are not available in the folder path */
            return Optional.empty();
        }
        //Following lines of code will execute if all the required files are available
        for (String s : map.keySet()) {
            Optional<String> pageBlockContents = null;
            if(pgnumDateDeletedMap != null && pgnumDateDeletedMap.containsKey(s)){
                pageBlockContents = Optional.of(pgnumDateDeletedMap.get(s));
                pgnumDateDeletedMap.remove(s);
            }
            else
                pageBlockContents = readSgmlAndConvertItToString(folderPath + "\\" + filenamesMap.get(s));
            if (pageBlockContents.isEmpty()) {
                /* There was an issue while trying to read and convert contents of file into a String*/
                showFileExtactContents(filenamesMap.get(s));
                return Optional.empty();
            }
            //Let Proceed to add the Pagenum date to the respective titles and return the new String
            Optional<String> pageblockOutput = addRevBarForRequiredTitlesOfAPageblock(map.get(s), pageBlockContents.get(), revDate, workbook);
            if (pageblockOutput.isEmpty()) {
                /* There was an issue while trying to add the pagenum date in the extracted String*/
                showPageNumDateAddError(filenamesMap.get(s));
                return Optional.empty();
            }
            outPutMap.put(s, new OutputStringDto(filenamesMap.get(s), pageblockOutput.get()));
            //boolean outPutResult = saveFileAfterModification(folderPath, pageblockOutput.get(), filenamesMap.get(s));
            //Add OutputComments in a file if required
        }
        return Optional.of(outPutMap);
    }
    public void takeBackupCopy(String path) {

    }

    /* This method check if the selected folder have the required files*/
    private int checkIfAllRequiredFilesAreAvailable(HashMap<String, ArrayList<FmChangeItem>> map, String folderPath, HashMap<String, String> filenamesMap) {
            Set<String> listOfAvailableFiles = listFilesInFolder(folderPath);
            for (String s : map.keySet()) {
                UtilityFunctions utilityFunctions = new UtilityFunctions();
                String filename =utilityFunctions.getFileNameFromPageblockName(s, listOfAvailableFiles, filenamesMap);
                if (filename == null) {
                    showFileFindErrorAlert(s);
                    return 0;
                } else {
                    filenamesMap.put(s, filename);
                    Path path = Paths.get(folderPath + "\\" + filenamesMap.get(s));
                    try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "rw");
                         FileChannel fileChannel = file.getChannel()) {
                        // Attempt to obtain an exclusive lock on the file
                        FileLock fileLock = fileChannel.tryLock();
                        if (fileLock != null) {
                            // File is not in use, as we successfully obtained a lock, Release the lock
                            fileLock.release();
                        } else {
                            showFileOpenedByOthersErrorAlert(filenamesMap.get(s));
                            return 0;
                        }

                    } catch (FileNotFoundException e){
                        showFileOpenedByOthersErrorAlert(filenamesMap.get(s));
                        return 0;
                    }
                    catch (Exception e) {
                        showFileFindErrorAlert(s);
                        return -1;
                    }
                }
            }
            return 1;
    }

    public Set<String> listFilesInFolder(String folderPath) {
        Set<String> filesList = new HashSet<>();
        try {
            Files.walkFileTree(Paths.get(folderPath), EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (attrs.isRegularFile() && !file.startsWith(Paths.get(folderPath, "Graphics"))) {
                        filesList.add(file.getFileName().toString());
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (dir.endsWith("Graphics")) {
                        return FileVisitResult.SKIP_SUBTREE; // Skip the "Graphics" subfolder and its contents
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return filesList;
    }

    private Optional<String> addRevBarForRequiredTitlesOfAPageblock(ArrayList<FmChangeItem> list, String input, String revDate, Workbook workbook) {
        ChangeLists changeLists = new ChangeLists(list);
        Optional<StringBuilder> tocOutput = addRevisionBarForTOCItemsVersion1(input, changeLists.getTocList(), revDate, workbook.getSheetAt(1));
        if (tocOutput.isEmpty())
            return Optional.empty();
        Optional<StringBuilder> loiOutput = addRevisionBarForFiguresVersion1(tocOutput.get(), changeLists.getFiguresList(), revDate, workbook.getSheetAt(3));
        if (loiOutput.isEmpty())
            return Optional.empty();
        Optional<StringBuilder> lotOutput = addRevisionBarForTablesVersion1(loiOutput.get(), changeLists.getTablesList(), revDate, workbook.getSheetAt(2));
        if (lotOutput.isEmpty())
            return Optional.empty();
        try{
            addPageblockNameIntoOutputExcel(workbook.getSheetAt(0), list.get(0).getPageblock());
            return Optional.of(lotOutput.toString());
        }
        catch (Exception exception){
            return Optional.empty();
        }
    }

    private void addPageblockNameIntoOutputExcel(Sheet sheet, String s) {
        Row row = sheet.createRow(sheet.getLastRowNum()+1);
        row.createCell(0).setCellValue(s);
    }

    public Optional<String> readSgmlAndConvertItToString(String filePath) {
        try {
            // Read the contents of the file into a byte array
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            // Convert the byte array to a String using the default charset (UTF-8)
            String fileContent = new String(fileBytes);
            return Optional.of(fileContent);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public SaveFilesResult saveAllTheFilesIntoSGMLFile(String path, HashMap<String, OutputStringDto> resultMap){
        ArrayList<String> completedList = new ArrayList<>();
        ArrayList<String> incompleteList = new ArrayList<>();
        for(String s: resultMap.keySet()){
            try{
                boolean isSaved = saveFileAfterModification(path, resultMap.get(s).getOutput(), resultMap.get(s).getFilename());
                if(isSaved == true)
                    completedList.add(s);
            } catch (Exception e) {
                incompleteList.add(s);
            }
        }
        return new SaveFilesResult(completedList, incompleteList);
    }

    public boolean saveFileAfterModification(String folderPath, String output, String fileName) throws IOException {
        String targetPath = folderPath + "\\" + fileName.replace(".sgm", ".txt");
        String editedOutput = removeUnwantedLinesFromOutputString(output);
            Path path = Paths.get(targetPath);
                Files.write(path, editedOutput.getBytes());
                return true;
        }

    private String removeUnwantedLinesFromOutputString(String output) {
        StringBuilder sb = new StringBuilder();
        try {
            // Create a BufferedReader to read the multiline string
            BufferedReader reader = new BufferedReader(new StringReader(output));
            String line;
            boolean isStartFound = false;
            while ((line = reader.readLine()) != null) {
                if(!isStartFound){
                    if(line.startsWith("<pgblk") || line.startsWith("<ipl")){
                        sb.append(line).append("\n");
                        isStartFound = true;
                    }
                }
                else{
                    if(line.contains("</pgblk>") || line.contains("</ipl>")){
                        String lastLine = trimContentAfterPgblkEndTag(line);
                        sb.append(lastLine);
                        reader.close();
                        return sb.toString();
                    }
                    else
                        sb.append(line).append("\n");
                }
            }
            reader.close();
            return null;
        }
        catch(Exception e){
            return null;
        }
    }

    private String trimContentAfterPgblkEndTag(String line) {
        String output = null;
        if(line.contains("</pgblk>")){
            int[] pgblk = UtilityFunctions.getFirstMatchingPattern(line, "</pgblk>");
            output = line.substring(0, pgblk[1]+1);
        }
        else if(line.contains("</ipl>")){
            int[] ipl = UtilityFunctions.getFirstMatchingPattern(line, "</ipl>");
            output =  line.substring(0, ipl[1]+1);
        }
        return output;
    }

    public String titleExtractor(String titleFromTag) {
        if(titleFromTag.contains("<?Pub")){
            titleFromTag = removeNewlineTagFromTitle(titleFromTag);
        }
        //System.out.println(titleFromTag);
        StringBuilder sb = new StringBuilder();
        int N = titleFromTag.length();
        int i = 0;
        while (i < N) {
            char ch = titleFromTag.charAt(i);
            if (ch == '<') {
                i = findEndIndexOfTag(titleFromTag, i + 1, N) + 1;
            } else if(titleFromTag.charAt(i) == ' ' && !sb.isEmpty() && sb.charAt(sb.length()-1) == ' '){
                i++;
            }
            else{
                sb.append(ch);
                i++;
            }
        }
        return sb.toString();
    }

    private String removeNewlineTagFromTitle(String titleFromTag) {
        int N = titleFromTag.length();
        int i=0;
        StringBuilder sb = new StringBuilder();
        while(i<N){
            char ch = titleFromTag.charAt(i);
            if((ch == '<') && ((i+4)<N) && (titleFromTag.substring(i, i+5).equals("<?Pub"))){
                while(i<N){
                    if(titleFromTag.charAt(i) == '>'){
                        break;
                    }
                    i++;
                }
                i = i+1;
            }
            else{
                sb.append(ch);
                i++;
            }
        }
        return sb.toString();
    }
    private int findEndIndexOfTag(String s, int st, int N) {
        String[] tag = getCurrentTag(s, st, N);
        for (int i = Integer.parseInt(tag[1]); i < N; i++) {
            if (tag[0].contains("delete") || tag[0].contains("sub") || tag[0].contains("super")) {
                if (s.charAt(i) == '/') {
                    i = i + 1;
                    while (i < N) {
                        if (s.charAt(i) == '>')
                            return i;
                        i++;
                    }
                }
            } else if (tag[0].contains("revst") || tag[0].contains("revend") || tag[0].contains("hotlink") || tag[0].contains("fmdate")
                    || tag[0].contains("leafst") || tag[0].contains("pgbrk") || tag[0].contains("reloc") || tag[0].contains("date")
                    || tag[0].contains("leafend")) {
                if (tag[0].charAt(tag[0].length() - 1) == '>')
                    return Integer.parseInt(tag[1]);
                if (s.charAt(i) == '>')
                    return i;
            }
        }
        return -1;
    }

    private String[] getCurrentTag(String s, int i, int N) {
        StringBuilder sb = new StringBuilder();
        while (i < N) {
            if (s.charAt(i) == ' ')
                return new String[]{sb.toString(), Integer.toString(i + 1)};
            else if (s.charAt(i) == '>')
                return new String[]{sb.toString(), Integer.toString(i)};
            else {
                sb.append(s.charAt(i));
                i++;
            }

        }
        return new String[]{sb.toString(), Integer.toString(i + 1)};
    }


    private String updatePagenumDateToTitleTag(String s, String revDate) {
        StringBuilder sb = new StringBuilder();
        if (s.charAt(6) == '>') {
            sb.append("<title pgnumdate=");
            sb.append('"');
            sb.append(revDate);
            sb.append('"');
            sb.append('>');
            sb.append(s.substring(7, s.length()));
        } else if (s.contains("pgnumdate")) {
            int[] indices = getStartAndEndOfPagenumDate(s, 7);
            sb.append(s.substring(0, indices[0] + 1));
            sb.append(revDate);
            sb.append(s.substring(indices[1], s.length()));
            return sb.toString();

        } else if (s.contains("fmrelocdate")) {
            int end = getEndOfFmreLocDate(s, 7);
            sb.append(s.substring(0, end + 1)).append(" pgnumdate=");
            sb.append('"');
            sb.append(revDate);
            sb.append('"');
            sb.append(s.substring(end + 1));
            return sb.toString();
        }
        return sb.toString();
    }

    private int getEndOfFmreLocDate(String s, int i) {
        int ans = -1;
        while (i < s.length()) {
            if (s.charAt(i) == 'f') {
                i = i + 13;
                while (i < s.length()) {
                    if (s.charAt(i) == '"') {
                        return i;
                    }
                    i++;
                }
            } else
                i++;
        }
        return ans;
    }

    private int[] getStartAndEndOfPagenumDate(String s, int i) {
        int[] ans = new int[]{-1, -1};
        while (i < s.length()) {
            if (s.charAt(i) == 'p') {
                ans[0] = i + 10;
                i = ans[0] + 1;
                while (i < s.length()) {
                    if (s.charAt(i) == '"') {
                        ans[1] = i;
                        return ans;
                    }
                    i++;
                }
            } else
                i++;
        }
        return ans;
    }

    private void showFileFindErrorAlert(String pageblock) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("File Not Found");
        alert.setHeaderText(null);
        alert.setContentText("There was an issue while trying to find the SGML file of " + pageblock +", Please make sure we a SGML file with appropriate file name in selected working folder.");
        alert.showAndWait();
    }
    private void showFileFindErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("File Not Found");
        alert.setHeaderText(null);
        alert.setContentText("There was an issue while trying to find the SGML files in the Working Folder. Make sure you have selected the correct Working Folder and Try Again.");
        alert.showAndWait();
    }

    private void showFilesNotAvailableErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Could Not Find The Required Files");
        alert.setHeaderText(null);
        alert.setContentText("Required files are not available in the Selected Working Folder, Please choose the" +
                " correct Working Folder. Also make sure filename of the SGML files are as per standard");
        alert.showAndWait();
    }

    private void showFileOpenedByOthersErrorAlert(String filename) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("FIle Already Opened By Someone");
        alert.setHeaderText(null);
        alert.setContentText(filename + " is opened already. Please close the file and try again. Also make sure all other SGML files are also closed");
        alert.showAndWait();
    }

    private void showFileExtactContents(String filename) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Could Not Extract File Contents");
        alert.setHeaderText(null);
        alert.setContentText("There was some issue while trying to extract contents of : '" + filename + "' as a String, Please try again");
        alert.showAndWait();
    }

    private void showPageNumDateAddError(String filename) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Could Not Add PageNum Date");
        alert.setHeaderText(null);
        alert.setContentText("There was some issue while trying add pagenum date to :" + filename + ", Please try again");
        alert.showAndWait();
    }

    public int getTableCntFromTableNumberString(String s) {
        int N = s.length();
        if (N < 4)
            return Integer.parseInt(s);
        if (N == 4) {
            int i = 1;
            while (i < N) {
                if (s.charAt(i) != '0')
                    return Integer.parseInt(s.substring(i, N));
                i++;
            }
        }
        int i = 2;
        while (i < N) {
            if (s.charAt(i) != '0')
                break;
            i++;
        }
        return Integer.parseInt(s.substring(i, N));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    private Optional<StringBuilder> addRevisionBarForTOCItemsVersion1(String input, ArrayList<FmChangeItem> list, String revDate, Sheet sheet) {
        if (list.isEmpty())
            return Optional.of(new StringBuilder(input));
        int M = list.size();
        int j = 0;
        StringBuilder sb = new StringBuilder();
        int currRow = 1;
        try {
            // Create a BufferedReader to read the multiline string
            BufferedReader reader = new BufferedReader(new StringReader(input));
            String line;
            String subtopicTitle = null;
            while ((line = reader.readLine()) != null && j<M) {
                FmChangeItem fmChangeItem = list.get(j);
                if (line.startsWith("<task")) {
                    sb.append(line).append("\n");
                    String nextLine = reader.readLine();
                    while(nextLine != null && !nextLine.contains("<title")){
                        sb.append(nextLine).append("\n");
                        nextLine = reader.readLine();
                    }
                    if(nextLine != null){
                        int[] ttlIndx = PreProcessing.getStartAndEndIndexOfTag(nextLine, "title", 0);
                        subtopicTitle = titleExtractor(PreProcessing.startingAndEndingOfAbsoluteTitle(nextLine.substring(ttlIndx[0], ttlIndx[1]+1))).trim();
                        if(fmChangeItem.getFrontMatterType().equals(FrontMatterType.SUB_TOPIC)
                        && subtopicTitle.equals(fmChangeItem.getSubTopic().subject)){
                            ExcelService excelService = new ExcelService();
                            excelService.createTaskRow(fmChangeItem,nextLine.substring(ttlIndx[0], ttlIndx[1]+1),sheet, currRow);
                            currRow+=1;
                            String updatedTitleLine = updatePagenumDateToTitleTag(nextLine.substring(ttlIndx[0], ttlIndx[1]+1), revDate);
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
                        String currTitle = titleExtractor(PreProcessing.startingAndEndingOfAbsoluteTitle(nextLine.substring(ttlIndx[0], ttlIndx[1] + 1))).trim();
                        if(fmChangeItem.getFrontMatterType().equals(FrontMatterType.SUB_SUB_TOPIC)
                                && subtopicTitle.equals(fmChangeItem.getSubTopic().subject)
                                && currTitle.equals(fmChangeItem.getSubSubTopic().subject)){
                            ExcelService excelService = new ExcelService();
                            excelService.createSubTaskRow(fmChangeItem,nextLine.substring(ttlIndx[0], ttlIndx[1]+1),sheet, currRow);
                            String updatedTitleLine = updatePagenumDateToTitleTag(nextLine.substring(ttlIndx[0], ttlIndx[1]+1), revDate);
                            sb.append(nextLine.substring(0, ttlIndx[0])).append(updatedTitleLine).append(nextLine.substring(ttlIndx[1]+1)).append("\n");
                            j++;
                        }
                        else{
                            sb.append(nextLine).append("\n");
                        }
                    }
                }
                else if(line.startsWith("<eqdeslist")){
                    HashMap<String, FmChangeItem> iplChanges = new HashMap();
                    if(fmChangeItem.getFrontMatterType().equals(FrontMatterType.SUB_TOPIC) && (fmChangeItem.getSubTopic().subject.startsWith("Equipment Designator") || fmChangeItem.getSubTopic().subject.startsWith("Numerical Index") || fmChangeItem.getSubTopic().subject.startsWith("Optional Vendor Index") || fmChangeItem.getSubTopic().subject.startsWith("Detailed Parts List"))){
                        while (j<M){
                            fmChangeItem = list.get(j);
                            if(fmChangeItem.getSubTopic().subject.startsWith("Equipment Designator"))
                                iplChanges.put("eqdes", fmChangeItem);
                            else if(fmChangeItem.getSubTopic().subject.startsWith("Numerical Index"))
                                iplChanges.put("num", fmChangeItem);
                            else if(fmChangeItem.getSubTopic().subject.startsWith("Optional Vendor Index"))
                                iplChanges.put("opt", fmChangeItem);
                            else if(fmChangeItem.getSubTopic().subject.startsWith("Detailed Parts List"))
                                iplChanges.put("dp", fmChangeItem);
                            else
                                break;
                            j++;
                        }
                        while (!line.startsWith("<dplist")){
                            sb.append(line).append("\n");
                            line = reader.readLine();
                            System.out.println(line);
                        }
                        if(line.startsWith("<dplist")){
                            String newLine = updateIPLSubtaskPageNumDates(line, iplChanges, revDate, currRow, sheet);
                            sb.append(newLine).append("\n");
                        }

                    }
                    else{
                        sb.append(line).append("\n");
                    }
                }
                else if(line.startsWith("<vendlist") && fmChangeItem.getFrontMatterType().equals(FrontMatterType.SUB_TOPIC) && fmChangeItem.getSubTopic().subject.startsWith("Manufacturer's Code, Name")){
                    sb.append(line).append("\n");
                    String nextLine = reader.readLine();
                    while (nextLine != null && !nextLine.contains("<title")) {
                        sb.append(nextLine).append("\n");
                        nextLine = reader.readLine();
                    }
                    if (nextLine != null) {
                        int[] ttlIndx = PreProcessing.getStartAndEndIndexOfTag(nextLine, "title", 0);
                        ExcelService excelService = new ExcelService();
                        excelService.createSubTaskRow(fmChangeItem, nextLine.substring(ttlIndx[0], ttlIndx[1] + 1), sheet, currRow);
                        String updatedTitleLine = updatePagenumDateToTitleTag(nextLine.substring(ttlIndx[0], ttlIndx[1] + 1), revDate);
                        sb.append(nextLine.substring(0, ttlIndx[0])).append(updatedTitleLine).append(nextLine.substring(ttlIndx[1] + 1)).append("\n");
                        j++;

                    }
                }
                else
                    sb.append(line).append("\n");
            }
            if (j<M){
                FmChangeItem fmChangeItem = list.get(j);
                showTitleNotFound(fmChangeItem);
                return Optional.empty();
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

    private String updateIPLSubtaskPageNumDates(String line, HashMap<String, FmChangeItem> iplChanges, String revDate, int currRow, Sheet sheet) {
        int dpListEnd = -1;
        for(int i=0; i<line.length(); i++){
            if(line.charAt(i) == '>'){
                dpListEnd = i+1;
                break;
            }
        }
        StringBuilder sb = new StringBuilder("<dplist");
        int[] eqdesList = UtilityFunctions.getFirstMatchingPattern(line, "pgnumdate_eqdes=\"\\d{8}\\\"");
        int[] numIndx = UtilityFunctions.getFirstMatchingPattern(line, "pgnumdate_num=\"\\d{8}\\\"");
        int[] optIndx = UtilityFunctions.getFirstMatchingPattern(line, "pgnumdate_optven=\"\\d{8}\\\"");
        int[] dpl = UtilityFunctions.getFirstMatchingPattern(line, "pgnumdate=\"\\d{8}\\\"");

        ExcelService excelService = new ExcelService();
        if(iplChanges.containsKey("dp")){
            sb.append(" " + "pgnumdate=\"").append(revDate).append("\"");
        }
        else if(dpl != null)
            sb.append(" ").append(line.substring(dpl[0], dpl[1]+1));
        if(iplChanges.containsKey("eqdes")){
            sb.append(" " + "pgnumdate_eqdes=\"").append(revDate).append("\"");
            excelService.createIPLTaskRow(iplChanges.get("eqdes"), sheet, line, eqdesList);
        }
        else if(eqdesList != null)
            sb.append(" ").append(line.substring(eqdesList[0], eqdesList[1]+1));
        if(iplChanges.containsKey("num")){
            sb.append(" " + "pgnumdate_num=\"").append(revDate).append("\"");
            excelService.createIPLTaskRow(iplChanges.get("num"), sheet, line, numIndx);
        }
        else if(numIndx != null)
            sb.append(" ").append(line.substring(numIndx[0], numIndx[1]+1));
        if(iplChanges.containsKey("opt")){
            sb.append(" " + "pgnumdate_optven=\"").append(revDate).append("\"");
            excelService.createIPLTaskRow(iplChanges.get("opt"), sheet, line, optIndx);
        }
        else if(optIndx != null)
            sb.append(" ").append(line.substring(optIndx[0], optIndx[1]+1));
        if(iplChanges.containsKey("dp")){
            excelService.createIPLTaskRow(iplChanges.get("dp"), sheet, line, dpl);
        }
        sb.append('>');
        if(dpListEnd<line.length())
            sb.append(line.substring(dpListEnd));
      return sb.toString();
    }

    private Optional<StringBuilder> addRevisionBarForTablesVersion1(StringBuilder input, ArrayList<FmChangeItem> list, String revDate, Sheet sheet) {
        if (list.isEmpty())
            return Optional.of(new StringBuilder(input));
        int currRow = 1;
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
                FmChangeItem fmChangeItem = list.get(j);
                if (line.contains("<table")) {
                    sb.append(line).append("\n");
                    String nextLine = reader.readLine();
                    while(nextLine != null && !nextLine.contains("<title")){
                        sb.append(nextLine).append("\n");
                        nextLine = reader.readLine();
                    }
                    if(nextLine != null){
                        int[] ttlIndx = PreProcessing.getStartAndEndIndexOfTag(nextLine, "title", 0);
                        String currTableTitle = titleExtractor(PreProcessing.startingAndEndingOfAbsoluteTitle(nextLine.substring(ttlIndx[0], ttlIndx[1]+1))).trim();
//                        if(nextLine.contains("(CPN 822-1753-351)")){
//                            System.out.println(nextLine);
//                            System.out.println("The extracted title: " + currTableTitle);
//                        }
                        // System.out.println("Title From PDF: "+ fmChangeItem.getLotItem().getTableTitle() + "\n" +
                              //  "Title From SGML: " + currTableTitle + "\n" + "Comparison Result: " + fmChangeItem.getLotItem().getTableTitle().equals(currTableTitle));
                        if(fmChangeItem.getLotItem().getTableTitle().equals(currTableTitle)){
                            ExcelService excelService = new ExcelService();
                            excelService.createTableRow(fmChangeItem,nextLine.substring(ttlIndx[0], ttlIndx[1]+1),sheet, currRow);
                            currRow+=1;
                            String updatedTitleLine = updatePagenumDateToTitleTag(nextLine.substring(ttlIndx[0], ttlIndx[1]+1), revDate);
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
            if (j<M){
                FmChangeItem fmChangeItem = list.get(j);
                showTitleNotFound(fmChangeItem);
                return Optional.empty();
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

    private Optional<StringBuilder> addRevisionBarForFiguresVersion1(StringBuilder input, ArrayList<FmChangeItem> list, String revDate, Sheet sheet) {
        if (list.isEmpty())
            return Optional.of(new StringBuilder(input));
        int currRow = 1;
        int N = input.length();
        int M = list.size();
        int j = 0;
        StringBuilder sb = new StringBuilder();
        try {
            // Create a BufferedReader to read the multiline string
            BufferedReader reader = new BufferedReader(new StringReader(input.toString()));
            String line;
            if(list.get(0).getLoiItem().getPageblock().equals("ILLUSTRATED PARTS LIST")){
                while ((line = reader.readLine()) != null && j<M) {
                    FmChangeItem fmChangeItem = list.get(j);
                    if (line.startsWith("<figure")) {
                        sb.append(line).append("\n");
                        String nextLine = reader.readLine();
                        while(nextLine != null && !nextLine.contains("<title")){
                            sb.append(nextLine).append("\n");
                            nextLine = reader.readLine();
                        }
                        if(nextLine != null){
                            int[] ttlIndx = PreProcessing.getStartAndEndIndexOfTag(nextLine, "title", 0);
                            String currFigureTitle = titleExtractor(PreProcessing.startingAndEndingOfAbsoluteTitle(nextLine.substring(ttlIndx[0], ttlIndx[1]+1))).trim();
                            if(fmChangeItem.getLoiItem().getFigureTitle().trim().equals(currFigureTitle.trim())){
                                ExcelService excelService = new ExcelService();
                                excelService.createFigureRow(fmChangeItem,nextLine.substring(ttlIndx[0], ttlIndx[1]+1),sheet, currRow);
                                currRow+=1;
                                String updatedFigureLine = updatePagenumDateToTitleTag(nextLine.substring(ttlIndx[0], ttlIndx[1]+1), revDate);
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
            }
            else{
                while ((line = reader.readLine()) != null && j<M) {
                    FmChangeItem fmChangeItem = list.get(j);
                    if (line.startsWith("<graphic")) {
                        sb.append(line).append("\n");
                        String nextLine = reader.readLine();
                        while(nextLine != null && !nextLine.contains("<title")){
                            sb.append(nextLine).append("\n");
                            nextLine = reader.readLine();
                        }
                        if(nextLine != null){
                            int[] ttlIndx = PreProcessing.getStartAndEndIndexOfTag(nextLine, "title", 0);
                            String currFigureTitle = titleExtractor(PreProcessing.startingAndEndingOfAbsoluteTitle(nextLine.substring(ttlIndx[0], ttlIndx[1]+1))).trim();
                            if(fmChangeItem.getLoiItem().getFigureTitle().equals(currFigureTitle)){
                                ExcelService excelService = new ExcelService();
                                excelService.createFigureRow(fmChangeItem,nextLine.substring(ttlIndx[0], ttlIndx[1]+1),sheet, currRow);
                                currRow+=1;
                                String updatedFigureLine = updatePagenumDateToTitleTag(nextLine.substring(ttlIndx[0], ttlIndx[1]+1), revDate);
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
            }
            if (j<M){
                FmChangeItem fmChangeItem = list.get(j);
                showTitleNotFound(fmChangeItem);
                return Optional.empty();
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
    private void showTitleNotFound(FmChangeItem fmChangeItem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Could Not Find The Following Title IN SGML");
        String pageblock = fmChangeItem.getPageblock();
        String fmtype = null;
        String fmNumber = null;
        String fmTitle = null;
        if(fmChangeItem.getFrontMatterType().equals(FrontMatterType.FIGURE)){
            fmtype = "Figure";
            fmNumber = fmChangeItem.getLoiItem().getFigureNumber();
            fmTitle = fmChangeItem.getLoiItem().getFigureTitle();
        }
        else if(fmChangeItem.getFrontMatterType().equals(FrontMatterType.TABLE)){
            fmtype = "Table";
            fmNumber = fmChangeItem.getLotItem().getTableNumber();
            fmTitle = fmChangeItem.getLotItem().getTableTitle();
        }
        else if(fmChangeItem.getFrontMatterType().equals(FrontMatterType.SUB_TOPIC)){
            fmtype = "TASK";
            fmNumber = fmChangeItem.getSubTopic().number;
            fmTitle = fmChangeItem.getSubTopic().subject;
        }
        else if(fmChangeItem.getFrontMatterType().equals(FrontMatterType.SUB_SUB_TOPIC)){
            fmtype = "SUBTASK";
            fmNumber = fmChangeItem.getSubSubTopic().number;
            fmTitle = fmChangeItem.getSubSubTopic().subject;
        }
        alert.setHeaderText(null);
        alert.setContentText("Pageblock :" + pageblock + " " + fmtype + " No: " + fmNumber +" " + fmTitle);
        alert.showAndWait();
    }
    public Optional<HashMap<String, String>> deletePreviouslyAddedPgnumDateFromSGMLFile(HashMap<String, RevisedTitleChangeList> pgblkWiseReviseChangesMap, String wfPath, HashMap<String, String> fileNamesMap) {
        HashMap<String, String> map = new HashMap<>();
        for(String s: pgblkWiseReviseChangesMap.keySet()){
            Optional<String> pageBlockContents = readSgmlAndConvertItToString(wfPath + "\\" + fileNamesMap.get(s));
            if(pageBlockContents.isEmpty()){
                //Throw an error saying that we couldn't open SGML file from the path;
                showNotAbleToOpenSGMLFile(fileNamesMap.get(s), wfPath);
                return Optional.empty();
            }
            Optional<String> processedString = removePgnumDatesFromPageblocks(pageBlockContents.get(), pgblkWiseReviseChangesMap.get(s));
            if(processedString.isEmpty())
                return Optional.empty();
            map.put(s, processedString.get());

        }
        return Optional.of(map);
    }

    private Optional<String> removePgnumDatesFromPageblocks(String input, RevisedTitleChangeList revisedTitleChangeList) {
            Optional<StringBuilder> tocOutput = removePgnumDatesFromTOCItemsVersion1(input, revisedTitleChangeList.getTocList());
            if (tocOutput.isEmpty())
                return Optional.empty();
            Optional<StringBuilder> loiOutput = removePgnumDatesFromFiguresVersion1(tocOutput.get().toString(), revisedTitleChangeList.getLoiList());
            if (loiOutput.isEmpty())
                return Optional.empty();
            Optional<StringBuilder> lotOutput = removePgnumDatesFromTablesVersion1(loiOutput.get().toString(), revisedTitleChangeList.getLotList());
            if (lotOutput.isEmpty())
                return Optional.empty();
            return Optional.of(lotOutput.get().toString());

        }

    private Optional<StringBuilder> removePgnumDatesFromTablesVersion1(String input, ArrayList<RevisedTitleDto> list) {
        if(list == null || list.isEmpty())
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
                if (line.contains("<table")) {
                    sb.append(line).append("\n");
                    String nextLine = reader.readLine();
                    while(nextLine != null && !nextLine.contains("<title")){
                        sb.append(nextLine).append("\n");
                        nextLine = reader.readLine();
                    }
                    if(nextLine != null){
                        int[] ttlIndx = PreProcessing.getStartAndEndIndexOfTag(nextLine, "title", 0);
                        String currTableTitle = titleExtractor(PreProcessing.startingAndEndingOfAbsoluteTitle(nextLine.substring(ttlIndx[0], ttlIndx[1]+1))).trim();
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
            if (j<M){
                FmChangeItem fmChangeItem = list.get(j).getFmChangeItem();
                showTitleNotFoundFromPagenumDateRemoval(fmChangeItem);
                return Optional.empty();
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
        return s.substring(0, i) + "pgnumdate=\"" + revDate +"\""  + s.substring(j+1);
    }

    private Optional<StringBuilder> removePgnumDatesFromFiguresVersion1(String input, ArrayList<RevisedTitleDto> list) {
        if (list == null || list.isEmpty())
            return Optional.of(new StringBuilder(input));
        int M = list.size();
        int j = 0;
        StringBuilder sb = new StringBuilder();
        try {
            // Create a BufferedReader to read the multiline string
            BufferedReader reader = new BufferedReader(new StringReader(input.toString()));
            String line;
            if(list.get(0).getFmChangeItem().getLoiItem().getPageblock().equals("ILLUSTRATED PARTS LIST")){
                while ((line = reader.readLine()) != null && j<M) {
                    FmChangeItem fmChangeItem = list.get(j).getFmChangeItem();
                    if (line.startsWith("<figure")) {
                        sb.append(line).append("\n");
                        String nextLine = reader.readLine();
                        while(nextLine != null && !nextLine.contains("<title")){
                            sb.append(nextLine).append("\n");
                            nextLine = reader.readLine();
                        }
                        if(nextLine != null){
                            int[] ttlIndx = PreProcessing.getStartAndEndIndexOfTag(nextLine, "title", 0);
                            String currFigureTitle = titleExtractor(PreProcessing.startingAndEndingOfAbsoluteTitle(nextLine.substring(ttlIndx[0], ttlIndx[1]+1))).trim();
                            if(fmChangeItem.getLoiItem().getFigureTitle().trim().equals(currFigureTitle.trim())){
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
            }
            else{
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
                            String currFigureTitle = titleExtractor(PreProcessing.startingAndEndingOfAbsoluteTitle(nextLine.substring(ttlIndx[0], ttlIndx[1]+1))).trim();
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
            }
            if (j<M){
                FmChangeItem fmChangeItem = list.get(j).getFmChangeItem();
                showTitleNotFoundFromPagenumDateRemoval(fmChangeItem);
                return Optional.empty();
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

    private Optional<StringBuilder> removePgnumDatesFromTOCItemsVersion1(String input, ArrayList<RevisedTitleDto> list) {
        if (list ==null || list.isEmpty())
            return Optional.of(new StringBuilder(input));
        int M = list.size();
        int j = 0;
        StringBuilder sb = new StringBuilder();
        int currRow = 1;
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
                        subtopicTitle = titleExtractor(PreProcessing.startingAndEndingOfAbsoluteTitle(nextLine.substring(ttlIndx[0], ttlIndx[1]+1))).trim();
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
                        String currTitle = titleExtractor(PreProcessing.startingAndEndingOfAbsoluteTitle(nextLine.substring(ttlIndx[0], ttlIndx[1]+1))).trim();
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
                else if(line.startsWith("<eqdeslist")){
                    HashMap<String, RevisedTitleDto> iplChanges = new HashMap();
                    if(fmChangeItem.getFrontMatterType().equals(FrontMatterType.SUB_TOPIC) && (fmChangeItem.getSubTopic().subject.startsWith("Equipment Designator") || fmChangeItem.getSubTopic().subject.startsWith("Numerical Index") || fmChangeItem.getSubTopic().subject.startsWith("Optional Vendor Index") || fmChangeItem.getSubTopic().subject.startsWith("Detailed Parts List"))){
                        while (j<M){
                            fmChangeItem = list.get(j).getFmChangeItem();
                            if(fmChangeItem.getSubTopic().subject.startsWith("Equipment Designator"))
                                iplChanges.put("eqdes", list.get(j));
                            else if(fmChangeItem.getSubTopic().subject.startsWith("Numerical Index"))
                                iplChanges.put("num", list.get(j));
                            else if(fmChangeItem.getSubTopic().subject.startsWith("Optional Vendor Index"))
                                iplChanges.put("opt", list.get(j));
                            else if(fmChangeItem.getSubTopic().subject.startsWith("Detailed Parts List"))
                                iplChanges.put("dp", list.get(j));
                            else
                                break;
                            j++;
                        }
                        while (!line.startsWith("<dplist")){
                            sb.append(line).append("\n");
                            line = reader.readLine();
                            System.out.println(line);
                        }
                        if(line.startsWith("<dplist")){
                            String newLine = removeIPLTaskPageNumDates(line, iplChanges);
                            sb.append(newLine).append("\n");
                        }

                    }
                    else{
                        sb.append(line).append("\n");
                    }
                }
                else if(line.startsWith("<vendlist") && fmChangeItem.getFrontMatterType().equals(FrontMatterType.SUB_TOPIC) && fmChangeItem.getSubTopic().subject.startsWith("Manufacturer's Code, Name")){
                    sb.append(line).append("\n");
                    String nextLine = reader.readLine();
                    while (nextLine != null && !nextLine.contains("<title")) {
                        sb.append(nextLine).append("\n");
                        nextLine = reader.readLine();
                    }
                    if (nextLine != null) {
                        int[] ttlIndx = PreProcessing.getStartAndEndIndexOfTag(nextLine, "title", 0);
                        String updatedTitleLine = updatePagenumDateToTitleTag(nextLine.substring(ttlIndx[0], ttlIndx[1] + 1), list.get(j).getPgnumDate());
                        sb.append(nextLine.substring(0, ttlIndx[0])).append(updatedTitleLine).append(nextLine.substring(ttlIndx[1] + 1)).append("\n");
                        j++;

                    }
                }
                else
                    sb.append(line).append("\n");
            }
            if (j<M){
                FmChangeItem fmChangeItem = list.get(j).getFmChangeItem();
                showTitleNotFoundFromPagenumDateRemoval(fmChangeItem);
                return Optional.empty();
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

    private String removeIPLTaskPageNumDates(String line, HashMap<String, RevisedTitleDto> iplChanges) {
        int dpListEnd = -1;
        for(int i=0; i<line.length(); i++){
            if(line.charAt(i) == '>'){
                dpListEnd = i+1;
                break;
            }
        }
        StringBuilder sb = new StringBuilder("<dplist");
        int[] eqdesList = UtilityFunctions.getFirstMatchingPattern(line, "pgnumdate_eqdes=\"\\d{8}\\\"");
        int[] numIndx = UtilityFunctions.getFirstMatchingPattern(line, "pgnumdate_num=\"\\d{8}\\\"");
        int[] optIndx = UtilityFunctions.getFirstMatchingPattern(line, "pgnumdate_optven=\"\\d{8}\\\"");
        int[] dpl = UtilityFunctions.getFirstMatchingPattern(line, "pgnumdate=\"\\d{8}\\\"");
        if(iplChanges.containsKey("dp")){
            if(iplChanges.get("dp").getPgnumDate() != null){
                String revDate = iplChanges.get("dp").getPgnumDate();
                sb.append(" " + "pgnumdate=\"").append(revDate).append("\"");
            }
        }
        else if(dpl != null)
            sb.append(" ").append(line.substring(dpl[0], dpl[1]+1));

        if(iplChanges.containsKey("eqdes")){
            if(iplChanges.get("eqdes").getPgnumDate() != null){
                String revDate = iplChanges.get("eqdes").getPgnumDate();
                sb.append(" " + "pgnumdate_eqdes=\"").append(revDate).append("\"");
            }

        }
        else if(eqdesList != null)
            sb.append(" ").append(line.substring(eqdesList[0], eqdesList[1]+1));
        if(iplChanges.containsKey("num")){
            if(iplChanges.get("num").getPgnumDate() != null){
                String revDate = iplChanges.get("num").getPgnumDate();
                sb.append(" " + "pgnumdate_num=\"").append(revDate).append("\"");
            }
        }
        else if(numIndx != null)
            sb.append(" ").append(line.substring(numIndx[0], numIndx[1]+1));
        if(iplChanges.containsKey("opt")){
            if(iplChanges.get("opt").getPgnumDate() != null){
                String revDate = iplChanges.get("opt").getPgnumDate();
                sb.append(" " + "pgnumdate_optven=\"").append(revDate).append("\"");
            }
        }
        else if(optIndx != null)
            sb.append(" ").append(line.substring(optIndx[0], optIndx[1]+1));
        sb.append('>');
        if(dpListEnd<line.length())
            sb.append(line.substring(dpListEnd));
        return sb.toString();
    }

    private void showTitleNotFoundFromPagenumDateRemoval(FmChangeItem fmChangeItem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Could Not Find The Following Title IN SGML");
        String pageblock = fmChangeItem.getPageblock();
        String fmtype = null;
        String fmNumber = null;
        String fmTitle = null;
        if(fmChangeItem.getFrontMatterType().equals(FrontMatterType.FIGURE)){
            fmtype = "Figure";
            fmNumber = fmChangeItem.getLoiItem().getFigureNumber();
            fmTitle = fmChangeItem.getLoiItem().getFigureTitle();
        }
        else if(fmChangeItem.getFrontMatterType().equals(FrontMatterType.TABLE)){
            fmtype = "Table";
            fmNumber = fmChangeItem.getLotItem().getTableNumber();
            fmTitle = fmChangeItem.getLotItem().getTableTitle();
        }
        else if(fmChangeItem.getFrontMatterType().equals(FrontMatterType.SUB_TOPIC)){
            fmtype = "TASK";
            fmNumber = fmChangeItem.getSubTopic().number;
            fmTitle = fmChangeItem.getSubTopic().subject;
        }
        else if(fmChangeItem.getFrontMatterType().equals(FrontMatterType.SUB_SUB_TOPIC)){
            fmtype = "SUBTASK";
            fmNumber = fmChangeItem.getSubSubTopic().number;
            fmTitle = fmChangeItem.getSubSubTopic().subject;
        }
        alert.setHeaderText(null);
        alert.setContentText("Pageblock :" + pageblock + " " + fmtype + " No: " + fmNumber +" " + fmTitle + "\n" +
                "If u have manually updated the title in SGML," + "\n" + "Please update the title in output.xlsx file to match the SGML file");
        alert.showAndWait();
    }
    private void showNotAbleToOpenSGMLFile(String filename, String wfPath) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Couldn't Read "+filename + " File!");
        alert.setHeaderText(null);
        alert.setContentText("There was an issue while trying to read" + filename +"\n" + "Make sure " + filename + " is available in " + wfPath);
        alert.showAndWait();
    }

}

