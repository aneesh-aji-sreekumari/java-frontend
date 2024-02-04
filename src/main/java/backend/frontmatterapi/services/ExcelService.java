package backend.frontmatterapi.services;
import backend.frontmatterapi.dtos.RevisedTitleChangeList;
import backend.frontmatterapi.dtos.RevisedTitleDto;
import backend.frontmatterapi.models.*;
import javafx.scene.control.Alert;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelService {
    public HashMap<String, RevisedTitleChangeList> extractOutputExcelContents(String path) {
        String excelFilePath = path + "\\" + "output.xlsx";
        try (FileInputStream fileInputStream = new FileInputStream(new File(excelFilePath));
             Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            ArrayList<String> pageblockList = getAllUpdatedPageblocks(workbook.getSheetAt(0));
            HashMap<String, ArrayList<RevisedTitleDto>> tocFmChangesList = new HashMap<>();
            HashMap<String, ArrayList<RevisedTitleDto>> lotFmChangesList = new HashMap<>();
            HashMap<String, ArrayList<RevisedTitleDto>> loiFmChangesList = new HashMap<>();
            tocFmChangesList = getPageblockWiseTocFmChagesListFromExcel(workbook.getSheetAt(1));
            lotFmChangesList = getPageblockWiseLotFmChagesListFromExcel(workbook.getSheetAt(2));
            loiFmChangesList = getPageblockWiseLoiFmChagesListFromExcel(workbook.getSheetAt(3));
            HashMap<String, RevisedTitleChangeList> pgblkWiseReviseChangesMap = getRevisedTitleChangeList(pageblockList, tocFmChangesList, lotFmChangesList, loiFmChangesList);
            return pgblkWiseReviseChangesMap;
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private HashMap<String, RevisedTitleChangeList> getRevisedTitleChangeList(ArrayList<String> pageblockList, HashMap<String, ArrayList<RevisedTitleDto>> tocFmChangesList, HashMap<String, ArrayList<RevisedTitleDto>> lotFmChangesList, HashMap<String, ArrayList<RevisedTitleDto>> loiFmChangesList) {
        HashMap<String, RevisedTitleChangeList> map = new HashMap<>();
        for (String s : pageblockList) {
            RevisedTitleChangeList revisedTitleChangeList = new RevisedTitleChangeList();
            if (lotFmChangesList.containsKey(s))
                revisedTitleChangeList.setLotList(lotFmChangesList.get(s));
            if (loiFmChangesList.containsKey(s))
                revisedTitleChangeList.setLoiList(loiFmChangesList.get(s));
            if (tocFmChangesList.containsKey(s))
                revisedTitleChangeList.setTocList(tocFmChangesList.get(s));
            if (revisedTitleChangeList.getLotList() != null || revisedTitleChangeList.getLoiList() != null || revisedTitleChangeList.getTocList() != null)
                map.put(s, revisedTitleChangeList);
        }
        return map;
    }

    private HashMap<String, ArrayList<RevisedTitleDto>> getPageblockWiseLoiFmChagesListFromExcel(Sheet sheet) {
        HashMap<String, ArrayList<RevisedTitleDto>> loiFmChangeItemMap = new HashMap<>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0)
                continue;
            RevisedTitleDto revisedTitleDto = getLoiRevisedTitleDtoFromTableRow(row);
            String pgblk = revisedTitleDto.getFmChangeItem().getPageblock();
            addRevisedTitleDtoIntoMap(pgblk, revisedTitleDto, loiFmChangeItemMap);
        }
        return loiFmChangeItemMap;
    }

    private RevisedTitleDto getLoiRevisedTitleDtoFromTableRow(Row row) {
        RevisedTitleDto revisedTitleDto = new RevisedTitleDto();
        FmChangeItem fmChangeItem = revisedTitleDto.getFmChangeItem();
        fmChangeItem.setPageblock(row.getCell(0).toString());
        fmChangeItem.setFrontMatterType(FrontMatterType.FIGURE);
        LoiItem loiItem = new LoiItem();
        loiItem.setFigureTitle(row.getCell(2).toString());
        loiItem.setFigureNumber(row.getCell(1).toString());
        loiItem.setPageblock(row.getCell(0).toString());
        fmChangeItem.setLoiItem(loiItem);
        if (row.getCell(3) != null)
            revisedTitleDto.setPgnumDate(row.getCell(3).toString().substring(1, row.getCell(3).toString().length() - 1));
        return revisedTitleDto;
    }

    private HashMap<String, ArrayList<RevisedTitleDto>> getPageblockWiseLotFmChagesListFromExcel(Sheet sheet) {
        HashMap<String, ArrayList<RevisedTitleDto>> lotFmChangeItemMap = new HashMap<>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0)
                continue;
            RevisedTitleDto revisedTitleDto = getLotRevisedTitleDtoFromTableRow(row);
            String pgblk = revisedTitleDto.getFmChangeItem().getPageblock();
            addRevisedTitleDtoIntoMap(pgblk, revisedTitleDto, lotFmChangeItemMap);
        }
        return lotFmChangeItemMap;
    }

    private RevisedTitleDto getLotRevisedTitleDtoFromTableRow(Row row) {
        RevisedTitleDto revisedTitleDto = new RevisedTitleDto();
        FmChangeItem fmChangeItem = revisedTitleDto.getFmChangeItem();
        fmChangeItem.setPageblock(row.getCell(0).toString());
        fmChangeItem.setFrontMatterType(FrontMatterType.TABLE);
        LotItem lotItem = new LotItem();
        lotItem.setTableTitle(row.getCell(2).toString());
        lotItem.setTableNumber(row.getCell(1).toString());
        lotItem.setPageblock(row.getCell(0).toString());
        fmChangeItem.setLotItem(lotItem);
        if (row.getCell(3) != null)
            revisedTitleDto.setPgnumDate(row.getCell(3).toString().substring(1, row.getCell(3).toString().length() - 1));
        return revisedTitleDto;
    }

    private HashMap<String, ArrayList<RevisedTitleDto>> getPageblockWiseTocFmChagesListFromExcel(Sheet sheet) {
        HashMap<String, ArrayList<RevisedTitleDto>> tocFmChangeItemMap = new HashMap<>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0)
                continue;
            RevisedTitleDto revisedTitleDto = getTocRevisedTitleDtoFromTableRow(row);
            String pgblk = revisedTitleDto.getFmChangeItem().getPageblock();
            addRevisedTitleDtoIntoMap(pgblk, revisedTitleDto, tocFmChangeItemMap);
        }
        return tocFmChangeItemMap;
    }

    private void addRevisedTitleDtoIntoMap(String pgblk, RevisedTitleDto revisedTitleDto, HashMap<String, ArrayList<RevisedTitleDto>> FmChangeItemMap) {
        if (!FmChangeItemMap.containsKey(pgblk)) {
            ArrayList<RevisedTitleDto> list = new ArrayList<>();
            list.add(revisedTitleDto);
            FmChangeItemMap.put(pgblk, list);
        } else {
            FmChangeItemMap.get(pgblk).add(revisedTitleDto);
        }
    }

    private RevisedTitleDto getTocRevisedTitleDtoFromTableRow(Row row) {
        RevisedTitleDto revisedTitleDto = new RevisedTitleDto();
        revisedTitleDto.getFmChangeItem().setPageblock(row.getCell(0).toString());
        String fmType = row.getCell(1).toString();
        if (fmType.equals("TASK")) {
            revisedTitleDto.getFmChangeItem().setFrontMatterType(FrontMatterType.SUB_TOPIC);
            SubTopic subTopic = new SubTopic();
            subTopic.subject = row.getCell(4).toString();
            subTopic.number = row.getCell(5).toString();
            revisedTitleDto.getFmChangeItem().setSubTopic(subTopic);
        } else {
            revisedTitleDto.getFmChangeItem().setFrontMatterType(FrontMatterType.SUB_SUB_TOPIC);
            SubSubTopic subSubTopic = new SubSubTopic();
            subSubTopic.subject = row.getCell(4).toString();
            subSubTopic.number = row.getCell(6).toString();
            revisedTitleDto.getFmChangeItem().setSubSubTopic(subSubTopic);
            SubTopic subTopic = new SubTopic();
            subTopic.subject = row.getCell(2).toString();
            subTopic.number = row.getCell(3).toString();
            revisedTitleDto.getFmChangeItem().setSubTopic(subTopic);
        }
        if (row.getCell(7) != null)
            revisedTitleDto.setPgnumDate(row.getCell(7).toString().substring(1, row.getCell(7).toString().length() - 1));

        return revisedTitleDto;
    }


    private ArrayList<String> getAllUpdatedPageblocks(Sheet sheet) {
        ArrayList<String> pageblockList = new ArrayList<>();
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (row.getRowNum() != 0)
                    pageblockList.add(cell.toString());
            }
        }
        return pageblockList;
    }

    public HashMap<String, RevisedTitleChangeList> getOnlyDatesTobeRemovedRevisedTitleMap(HashMap<String, RevisedTitleChangeList> pgblkWiseReviseChangesMap, HashMap<String, ArrayList<FmChangeItem>> fmChangeListMap) {
        HashMap<String, RevisedTitleChangeList> result = new HashMap<>();
        for (String s : pgblkWiseReviseChangesMap.keySet()) {
            if (!fmChangeListMap.containsKey(s)) {
                result.put(s, pgblkWiseReviseChangesMap.get(s));
            } else {
                ChangeLists changeLists = new ChangeLists(fmChangeListMap.get(s));
                ArrayList<RevisedTitleDto> revisedToc = pgblkWiseReviseChangesMap.get(s).getTocList();
                ArrayList<RevisedTitleDto> revisedLot = pgblkWiseReviseChangesMap.get(s).getLotList();
                ArrayList<RevisedTitleDto> revisedLoi = pgblkWiseReviseChangesMap.get(s).getLoiList();
                ArrayList<FmChangeItem> currToc = changeLists.getTocList();
                ArrayList<FmChangeItem> currLot = changeLists.getTablesList();
                ArrayList<FmChangeItem> currLoi = changeLists.getFiguresList();
                ArrayList<RevisedTitleDto> tocChangesRequired = getRequiredTocChangesList(revisedToc, currToc);
                ArrayList<RevisedTitleDto> lotChangesRequired = getRequiredLotChangesList(revisedLot, currLot);
                ArrayList<RevisedTitleDto> loiChangesRequired = getRequiredLoiChangesList(revisedLoi, currLoi);
                if (!tocChangesRequired.isEmpty() || !lotChangesRequired.isEmpty() || !loiChangesRequired.isEmpty()) {
                    RevisedTitleChangeList revisedTitleChangeList = new RevisedTitleChangeList();
                    if (tocChangesRequired.isEmpty())
                        revisedTitleChangeList.setTocList(tocChangesRequired);
                    if (lotChangesRequired.isEmpty())
                        revisedTitleChangeList.setLotList(lotChangesRequired);
                    if (loiChangesRequired.isEmpty())
                        revisedTitleChangeList.setLoiList(loiChangesRequired);
                    result.put(s, revisedTitleChangeList);
                }
            }
        }
        return result;
    }

    private ArrayList<RevisedTitleDto> getRequiredLoiChangesList(ArrayList<RevisedTitleDto> revisedLoi, ArrayList<FmChangeItem> currLoi) {
        ArrayList<RevisedTitleDto> ans = new ArrayList<>();
        int N = revisedLoi.size();
        int M = currLoi.size();
        int i = 0;
        int j = 0;
        while (i < N && j < M) {
            UtilityFunctions utilityFunctions = new UtilityFunctions();
            FmChangeItem revised = revisedLoi.get(i).getFmChangeItem();
            FmChangeItem curr = currLoi.get(j);
            int currFigureNumber = utilityFunctions.getNumberFromString(curr.getLoiItem().getFigureNumber());
            int revisedFigureNumber = utilityFunctions.getNumberFromString(revised.getLoiItem().getFigureNumber());
            if (revisedFigureNumber < currFigureNumber) {
                ans.add(revisedLoi.get(i));
                i++;
            } else if (revisedFigureNumber > currFigureNumber) {
                j++;
            } else {
                i++;
                j++;
            }
        }
        while (i < N) {
            ans.add(revisedLoi.get(i));
            i++;
        }
        return ans;
    }

    private ArrayList<RevisedTitleDto> getRequiredLotChangesList(ArrayList<RevisedTitleDto> revisedLot, ArrayList<FmChangeItem> currLot) {
        ArrayList<RevisedTitleDto> ans = new ArrayList<>();
        int N = revisedLot.size();
        int M = currLot.size();
        int i = 0;
        int j = 0;
        while (i < N && j < M) {
            UtilityFunctions utilityFunctions = new UtilityFunctions();
            FmChangeItem revised = revisedLot.get(i).getFmChangeItem();
            FmChangeItem curr = currLot.get(j);
            int currTableNumber = utilityFunctions.getNumberFromString(curr.getLotItem().getTableNumber());
            int revisedTableNumber = utilityFunctions.getNumberFromString(revised.getLotItem().getTableNumber());
            if (revisedTableNumber < currTableNumber) {
                ans.add(revisedLot.get(i));
                i++;
            } else if (revisedTableNumber > currTableNumber) {
                j++;
            } else {
                i++;
                j++;
            }
        }
        while (i < N) {
            ans.add(revisedLot.get(i));
            i++;
        }
        return ans;
    }

    private ArrayList<RevisedTitleDto> getRequiredTocChangesList(ArrayList<RevisedTitleDto> revisedToc, ArrayList<FmChangeItem> currToc) {
        ArrayList<RevisedTitleDto> ans = new ArrayList<>();
        int N = revisedToc.size();
        int M = currToc.size();
        int i = 0;
        int j = 0;
        while (i < N && j < M) {
            FmChangeItem revised = revisedToc.get(i).getFmChangeItem();
            FmChangeItem curr = currToc.get(j);
            int revisedTaskNumber = Integer.parseInt(revised.getSubTopic().number);
            int currTaskNumber = Integer.parseInt(curr.getSubTopic().number);
            if (revised.getFrontMatterType().equals(FrontMatterType.SUB_TOPIC)) {
                if (revisedTaskNumber < currTaskNumber) {
                    ans.add(revisedToc.get(i));
                    i++;
                } else if (revisedTaskNumber > currTaskNumber) {
                    j++;
                } else {
                    i++;
                    j++;
                }
            } else {
                if (revisedTaskNumber < currTaskNumber) {
                    ans.add(revisedToc.get(i));
                    i++;
                } else if (revisedTaskNumber > currTaskNumber) {
                    j++;
                } else {
                    if (curr.getFrontMatterType().equals(FrontMatterType.SUB_TOPIC))
                        j++;
                    else {
                        int revisedSubTaskNumber = getSubtaskNumberFromString(revised.getSubSubTopic().number);
                        int currSubTaskNumber = getSubtaskNumberFromString(curr.getSubSubTopic().number);
                        if (revisedSubTaskNumber < currSubTaskNumber) {
                            ans.add(revisedToc.get(i));
                            i++;
                        } else if (revisedSubTaskNumber > currSubTaskNumber) {
                            j++;
                        } else {
                            i++;
                            j++;
                        }

                    }
                }
            }
        }
        while (i < N) {
            ans.add(revisedToc.get(i));
            i++;
        }
        return ans;
    }

    public Workbook createExcelWorkbook() {
        Workbook workbook = new XSSFWorkbook();
        // Created Multiple Sheets
        Sheet sheet1 = workbook.createSheet("Updated PAGEBLOCKS");
        Sheet sheet2 = workbook.createSheet("TOC");
        Sheet sheet3 = workbook.createSheet("LOT");
        Sheet sheet4 = workbook.createSheet("LOI");

        // Creating Header Row for sheet1
        Row headerRow1 = sheet1.createRow(0);
        createHeaderRow(headerRow1, "LIST OF PAGEBLOCKS UPDATED");

        // Creating Header Row and Columns for sheet2
        Row headerRow2 = sheet2.createRow(0);
        createHeaderRow(headerRow2, "PAGEBLOCK", "TOC TYPE", "PARENT TASK TITLE", "PARENT TASK NO.",
                "TITLE OF TASK/SUBTASK", "TASK NO.", "SUBTASK NO.", "PREV. PGNUM DATE");

        // Creating Header Row and Columns for sheet3
        Row headerRow3 = sheet3.createRow(0);
        createHeaderRow(headerRow3, "PAGEBLOCK", "TABLE NUMBER", "TITLE", "PREV. PGNUM DATE");

        // Creating Header Row and Columns for sheet4
        Row headerRow4 = sheet4.createRow(0);
        createHeaderRow(headerRow4, "PAGEBLOCK", "FIGURE NUMBER", "TITLE", "PREV. PGNUM DATE");

        return workbook;
    }

    private void createHeaderRow(Row headerRow, String... headers) {
        CellStyle headerCellStyle = headerRow.getSheet().getWorkbook().createCellStyle();
        headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font headerFont = headerRow.getSheet().getWorkbook().createFont();
        headerFont.setBold(true);
        headerCellStyle.setFont(headerFont);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Set cell width to wrap text
        for (int i = 0; i < headers.length; i++) {
            headerRow.getSheet().autoSizeColumn(i);
        }
    }

    public void createTaskRow(FmChangeItem fmChangeItem, String titleTag, Sheet sheet, int currRow) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue(fmChangeItem.getPageblock());
        row.createCell(1).setCellValue("TASK");
        row.createCell(2).setCellValue("");
        row.createCell(3).setCellValue("");
        row.createCell(4).setCellValue(fmChangeItem.getSubTopic().subject);
        row.createCell(5).setCellValue(fmChangeItem.getSubTopic().number);
        row.createCell(6).setCellValue("");
        String pgnumDate = getPageNumDateFromTitleTag(titleTag);
        if (pgnumDate != null)
            row.createCell(7).setCellValue("(" + pgnumDate + ")");
        else
            row.createCell(7).setCellValue(pgnumDate);
    }

    public void createSubTaskRow(FmChangeItem fmChangeItem, String titleTag, Sheet sheet, int currRow) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue(fmChangeItem.getPageblock());
        row.createCell(1).setCellValue("SUB TASK");
        row.createCell(2).setCellValue(fmChangeItem.getSubTopic().subject);
        row.createCell(3).setCellValue(fmChangeItem.getSubTopic().number);
        row.createCell(4).setCellValue(fmChangeItem.getSubSubTopic().subject);
        row.createCell(5).setCellValue("");
        row.createCell(6).setCellValue(fmChangeItem.getSubSubTopic().number);
        String pgnumDate = getPageNumDateFromTitleTag(titleTag);
        if (pgnumDate != null)
            row.createCell(7).setCellValue("(" + pgnumDate + ")");
        else
            row.createCell(7).setCellValue(pgnumDate);
    }

    public void createTableRow(FmChangeItem fmChangeItem, String titleTag, Sheet sheet, int currRow) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue(fmChangeItem.getPageblock());
        row.createCell(1).setCellValue(fmChangeItem.getLotItem().getTableNumber());
        row.createCell(2).setCellValue(fmChangeItem.getLotItem().getTableTitle());
        String pgnumDate = getPageNumDateFromTitleTag(titleTag);
        if (pgnumDate != null)
            row.createCell(3).setCellValue("(" + pgnumDate + ")");
        else
            row.createCell(3).setCellValue(pgnumDate);
    }

    public void createFigureRow(FmChangeItem fmChangeItem, String titleTag, Sheet sheet, int currRow) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue(fmChangeItem.getPageblock());
        row.createCell(1).setCellValue(fmChangeItem.getLoiItem().getFigureNumber());
        row.createCell(2).setCellValue(fmChangeItem.getLoiItem().getFigureTitle());
        String pgnumDate = getPageNumDateFromTitleTag(titleTag);
        if (pgnumDate != null)
            row.createCell(3).setCellValue("(" + pgnumDate + ")");
        else
            row.createCell(3).setCellValue(pgnumDate);
    }

    private String getPageNumDateFromTitleTag(String titleTag) {
        String pattern = "pgnumdate=\"\\d{8}\"";

        // Create a Pattern object
        Pattern regex = Pattern.compile(pattern);

        // Create a Matcher object
        Matcher matcher = regex.matcher(titleTag);

        // Check for a match
        if (matcher.find()) {
            // Return the matched string
            String pgnum = matcher.group();
            return pgnum.substring(11, pgnum.length() - 1);
        } else {
            // Return null if no match is found
            return null;
        }
    }

    public void createIPLTaskRow(FmChangeItem fmChangeItem, Sheet sheet, String line, int[] idxs) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue(fmChangeItem.getPageblock());
        row.createCell(1).setCellValue("TASK");
        row.createCell(2).setCellValue("");
        row.createCell(3).setCellValue("");
        row.createCell(4).setCellValue(fmChangeItem.getSubTopic().subject);
        row.createCell(5).setCellValue(fmChangeItem.getSubTopic().number);
        row.createCell(6).setCellValue("");
        String pgnumDate = null;
        if (idxs != null) {
            String pgdateString = line.substring(idxs[0], idxs[1] + 1);
            int N = pgdateString.length();
            pgnumDate = pgdateString.substring(N - 9, N - 1);
        }
        if (pgnumDate != null)
            row.createCell(7).setCellValue("(" + pgnumDate + ")");
        else
            row.createCell(7).setCellValue(pgnumDate);
    }

    public int getSubtaskNumberFromString(String s) {
        int i = 0;
        int pow = 26;
        int num = 0;
        while (i < s.length() - 1) {
            num = num + (s.charAt(i) - 'A' + 1) * pow;
            pow = pow * 26;
            i++;
        }
        num = num + s.charAt(i) - 'A' + 1;
        return num;
    }

    public Workbook createExcelWorkbookForDisplayChanges() {
        Workbook workbook = new XSSFWorkbook();
        // Created Multiple Sheets
        Sheet sheet1 = workbook.createSheet("TOC");
        Sheet sheet2 = workbook.createSheet("LOT");
        Sheet sheet3 = workbook.createSheet("LOI");
        // Creating Header Row for sheet1
        Row headerRow1 = sheet1.createRow(0);
        createHeaderRow(headerRow1, "NUMBER(OLD)", "TITLE(OLD)", "PAGE_NO(OLD)", "NUMBER(NEW)", "TITLE(NEW)", "PAGE_NO(NEW)"
        );

        // Creating Header Row and Columns for sheet2
        Row headerRow2 = sheet2.createRow(0);
        createHeaderRow(headerRow2, "TABLE_NO(OLD)", "TITLE(OLD)", "PAGE_NO(OLD)", "TABLE_NO(NEW)", "TITLE(NEW)", "PAGE_NO(NEW)");

        // Creating Header Row and Columns for sheet3
        Row headerRow3 = sheet3.createRow(0);
        createHeaderRow(headerRow3, "FIG_NO(OLD)", "TITLE(OLD)", "PAGE_NO(OLD)", "FIG_NO(NEW)", "TITLE(NEW)", "PAGE_NO(NEW)");


        return workbook;
    }

    public CellStyle getChangeStyle(Workbook workbook) {
        // Create a cell style with background color and borders
        CellStyle changeCell = workbook.createCellStyle();
        changeCell.setFillForegroundColor(IndexedColors.CORAL.getIndex());
        changeCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        changeCell.setBorderTop(BorderStyle.THIN);
        changeCell.setBorderBottom(BorderStyle.THIN);
        changeCell.setBorderLeft(BorderStyle.THIN);
        changeCell.setBorderRight(BorderStyle.THIN);
        return changeCell;
    }

    public CellStyle getNoChangeStyle(Workbook workbook) {
        // Create a cell style with background color and borders
        CellStyle noChangeCell = workbook.createCellStyle();
        noChangeCell.setBorderTop(BorderStyle.THIN);
        noChangeCell.setBorderBottom(BorderStyle.THIN);
        noChangeCell.setBorderLeft(BorderStyle.THIN);
        noChangeCell.setBorderRight(BorderStyle.THIN);
        // Apply the style to the cell
        return noChangeCell;
    }

    public void createDisplaySubTaskRow(SubSubTopic oldItem, SubSubTopic newItem, Sheet sheet) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue(oldItem.number);
        row.createCell(1).setCellValue(oldItem.subject);
        row.createCell(2).setCellValue(oldItem.pageNumber);
        row.createCell(3).setCellValue(newItem.number);
        row.createCell(4).setCellValue(newItem.subject);
        row.createCell(5).setCellValue(newItem.pageNumber);
        if (oldItem.number.equals(newItem.number)) {
            row.getCell(0).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(3).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
        } else {
            row.getCell(0).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(3).setCellStyle(getChangeStyle(sheet.getWorkbook()));
        }
        if (oldItem.subject.equals(newItem.subject)) {
            row.getCell(1).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
        } else {
            row.getCell(1).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getChangeStyle(sheet.getWorkbook()));
        }
        if (oldItem.pageNumber.equals(newItem.pageNumber)) {
            row.getCell(2).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
        } else {
            row.getCell(2).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getChangeStyle(sheet.getWorkbook()));
        }
    }

    public void createDisplaySubTaskRow(SubSubTopic item, int type, Sheet sheet) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        if (type == 0) {
            row.createCell(0).setCellValue(item.number);
            row.createCell(1).setCellValue(item.subject);
            row.createCell(2).setCellValue(item.pageNumber);
            row.createCell(3).setCellValue("");
            row.createCell(4).setCellValue("");
            row.createCell(5).setCellValue("");
            row.getCell(0).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(1).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(2).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(3).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));

        } else if (type == 1) {
            row.createCell(0).setCellValue("");
            row.createCell(1).setCellValue("");
            row.createCell(2).setCellValue("");
            row.createCell(3).setCellValue(item.number);
            row.createCell(4).setCellValue(item.subject);
            row.createCell(5).setCellValue(item.pageNumber);
            row.getCell(0).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(1).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(2).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(3).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getChangeStyle(sheet.getWorkbook()));
        }
    }

    public void createDisplayTaskRow(SubTopic oldItem, SubTopic newItem, Sheet sheet) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue(oldItem.number);
        row.createCell(1).setCellValue(oldItem.subject);
        row.createCell(2).setCellValue(oldItem.pageNumber);
        row.createCell(3).setCellValue(newItem.number);
        row.createCell(4).setCellValue(newItem.subject);
        row.createCell(5).setCellValue(newItem.pageNumber);
        if (oldItem.number.equals(newItem.number)) {
            row.getCell(0).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(3).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
        } else {
            row.getCell(0).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(3).setCellStyle(getChangeStyle(sheet.getWorkbook()));
        }
        if (oldItem.subject.equals(newItem.subject)) {
            row.getCell(1).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
        } else {
            row.getCell(1).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getChangeStyle(sheet.getWorkbook()));
        }
        if (oldItem.pageNumber.equals(newItem.pageNumber)) {
            row.getCell(2).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
        } else {
            row.getCell(2).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getChangeStyle(sheet.getWorkbook()));
        }
    }

    public void createDisplayTaskRow(SubTopic item, int type, Sheet sheet) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        if (type == 0) {
            row.createCell(0).setCellValue(item.number);
            row.createCell(1).setCellValue(item.subject);
            row.createCell(2).setCellValue(item.pageNumber);
            row.createCell(3).setCellValue("");
            row.createCell(4).setCellValue("");
            row.createCell(5).setCellValue("");
            row.getCell(0).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(1).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(2).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(3).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));

        } else if (type == 1) {
            row.createCell(0).setCellValue("");
            row.createCell(1).setCellValue("");
            row.createCell(2).setCellValue("");
            row.createCell(3).setCellValue(item.number);
            row.createCell(4).setCellValue(item.subject);
            row.createCell(5).setCellValue(item.pageNumber);
            row.getCell(0).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(1).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(2).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(3).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getChangeStyle(sheet.getWorkbook()));
        }
    }

    public void createTableDisplayRow(LotItem oldItem, LotItem newItem, Sheet sheet) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue(oldItem.getTableNumber());
        row.createCell(1).setCellValue(oldItem.getTableTitle());
        row.createCell(2).setCellValue(oldItem.getPageNumber());
        row.createCell(3).setCellValue(newItem.getTableNumber());
        row.createCell(4).setCellValue(newItem.getTableTitle());
        row.createCell(5).setCellValue(newItem.getPageNumber());
        if (oldItem.getTableNumber().equals(newItem.getTableNumber())) {
            row.getCell(0).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(3).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
        } else {
            row.getCell(0).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(3).setCellStyle(getChangeStyle(sheet.getWorkbook()));
        }
        if (oldItem.getTableTitle().equals(newItem.getTableTitle())) {
            row.getCell(1).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
        } else {
            row.getCell(1).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getChangeStyle(sheet.getWorkbook()));
        }
        if (oldItem.getPageNumber().equals(newItem.getPageNumber())) {
            row.getCell(2).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
        } else {
            row.getCell(2).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getChangeStyle(sheet.getWorkbook()));
        }
    }

    public void createTableDisplayRow(LotItem item, int type, Sheet sheet) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        if (type == 0) {
            row.createCell(0).setCellValue(item.getTableNumber());
            row.createCell(1).setCellValue(item.getTableTitle());
            row.createCell(2).setCellValue(item.getPageNumber());
            row.createCell(3).setCellValue("");
            row.createCell(4).setCellValue("");
            row.createCell(5).setCellValue("");
            row.getCell(0).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(1).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(2).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(3).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));

        } else if (type == 1) {
            row.createCell(0).setCellValue("");
            row.createCell(1).setCellValue("");
            row.createCell(2).setCellValue("");
            row.createCell(3).setCellValue(item.getTableNumber());
            row.createCell(4).setCellValue(item.getTableTitle());
            row.createCell(5).setCellValue(item.getPageNumber());
            row.getCell(0).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(1).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(2).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(3).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getChangeStyle(sheet.getWorkbook()));
        }
    }

    public void createFigureDisplayRow(LoiItem oldItem, LoiItem newItem, Sheet sheet) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue(oldItem.getFigureNumber());
        row.createCell(1).setCellValue(oldItem.getFigureTitle());
        row.createCell(2).setCellValue(oldItem.getPageNumber());
        row.createCell(3).setCellValue(newItem.getFigureNumber());
        row.createCell(4).setCellValue(newItem.getFigureTitle());
        row.createCell(5).setCellValue(newItem.getPageNumber());
        if (oldItem.getFigureNumber().equals(newItem.getFigureNumber())) {
            row.getCell(0).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(3).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
        } else {
            row.getCell(0).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(3).setCellStyle(getChangeStyle(sheet.getWorkbook()));
        }
        if (oldItem.getFigureTitle().equals(newItem.getFigureTitle())) {
            row.getCell(1).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
        } else {
            row.getCell(1).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getChangeStyle(sheet.getWorkbook()));
        }
        if (oldItem.getPageNumber().equals(newItem.getPageNumber())) {
            row.getCell(2).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
        } else {
            row.getCell(2).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getChangeStyle(sheet.getWorkbook()));
        }
    }

    public void createFigureDisplayRow(LoiItem item, int type, Sheet sheet) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        if (type == 0) {
            row.createCell(0).setCellValue(item.getFigureNumber());
            row.createCell(1).setCellValue(item.getFigureTitle());
            row.createCell(2).setCellValue(item.getPageNumber());
            row.createCell(3).setCellValue("");
            row.createCell(4).setCellValue("");
            row.createCell(5).setCellValue("");
            row.getCell(0).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(1).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(2).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(3).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));

        } else if (type == 1) {
            row.createCell(0).setCellValue("");
            row.createCell(1).setCellValue("");
            row.createCell(2).setCellValue("");
            row.createCell(3).setCellValue(item.getFigureNumber());
            row.createCell(4).setCellValue(item.getFigureTitle());
            row.createCell(5).setCellValue(item.getPageNumber());
            row.getCell(0).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(1).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(2).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(3).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getChangeStyle(sheet.getWorkbook()));
        }
    }

    public void createDisplayPageblockRow(PageBlock oldItem, PageBlock newItem, Sheet sheet) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue("");
        row.createCell(1).setCellValue(oldItem.pageBlockName);
        row.createCell(2).setCellValue(oldItem.pageNumber);
        row.createCell(3).setCellValue("");
        row.createCell(4).setCellValue(newItem.pageBlockName);
        row.createCell(5).setCellValue(newItem.pageNumber);
        row.getCell(0).setCellStyle(getNoChangeStylePageblck(sheet.getWorkbook()));
        row.getCell(3).setCellStyle(getNoChangeStylePageblck(sheet.getWorkbook()));
        if (oldItem.pageBlockName.equals(newItem.pageBlockName)) {
            row.getCell(1).setCellStyle(getNoChangeStylePageblck(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getNoChangeStylePageblck(sheet.getWorkbook()));
        } else {
            row.getCell(1).setCellStyle(getChangeStylePageblock(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getChangeStylePageblock(sheet.getWorkbook()));
        }
        if (oldItem.pageNumber.equals(newItem.pageNumber)) {
            row.getCell(2).setCellStyle(getNoChangeStylePageblck(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getNoChangeStylePageblck(sheet.getWorkbook()));
        } else {
            row.getCell(2).setCellStyle(getChangeStylePageblock(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getChangeStylePageblock(sheet.getWorkbook()));
        }
    }

    public void createDisplayPageblockRow(PageBlock item, int type, Sheet sheet) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        if (type == 0) {
            row.createCell(0).setCellValue("");
            row.createCell(1).setCellValue(item.pageBlockName);
            row.createCell(2).setCellValue(item.pageNumber);
            row.createCell(3).setCellValue("");
            row.createCell(4).setCellValue("");
            row.createCell(5).setCellValue("");
            row.getCell(0).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(1).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(2).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(3).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));

        } else if (type == 1) {
            row.createCell(0).setCellValue("");
            row.createCell(1).setCellValue("");
            row.createCell(2).setCellValue("");
            row.createCell(3).setCellValue("");
            row.createCell(4).setCellValue(item.pageBlockName);
            row.createCell(5).setCellValue(item.pageNumber);
            row.getCell(0).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(1).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(2).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(3).setCellStyle(getNoChangeStyle(sheet.getWorkbook()));
            row.getCell(4).setCellStyle(getChangeStyle(sheet.getWorkbook()));
            row.getCell(5).setCellStyle(getChangeStyle(sheet.getWorkbook()));
        }
    }

    public CellStyle getChangeStylePageblock(Workbook workbook) {
        // Create a cell style with background color and borders
        CellStyle changeCell = workbook.createCellStyle();
        changeCell.setFillForegroundColor(IndexedColors.CORAL.getIndex());
        changeCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        changeCell.setBorderTop(BorderStyle.MEDIUM);
        changeCell.setBorderBottom(BorderStyle.MEDIUM);
        changeCell.setBorderLeft(BorderStyle.MEDIUM);
        changeCell.setBorderRight(BorderStyle.MEDIUM);
        changeCell.setAlignment(HorizontalAlignment.CENTER);
        // Apply the style to the cell
        return changeCell;
    }

    public CellStyle getNoChangeStylePageblck(Workbook workbook) {
        // Create a cell style with background color and borders
        CellStyle noChangeCell = workbook.createCellStyle();
        noChangeCell.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        noChangeCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        noChangeCell.setBorderTop(BorderStyle.MEDIUM);
        noChangeCell.setBorderBottom(BorderStyle.MEDIUM);
        noChangeCell.setBorderLeft(BorderStyle.MEDIUM);
        noChangeCell.setBorderRight(BorderStyle.MEDIUM);
        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        noChangeCell.setFont(font);
        // Set text alignment to center
        noChangeCell.setAlignment(HorizontalAlignment.CENTER);
        // Apply the style to the cell
        return noChangeCell;
    }
    public String[] getOrder(){
        return new String[]{"INTRODUCTION","DESCRIPTION AND OPERATION","TESTING AND FAULT ISOLATION",
                "SCHEMATIC AND WIRING DIAGRAMS","DISASSEMBLY","CLEANING","INSPECTION/CHECK","REPAIR","ASSEMBLY",
                "FITS AND CLEARANCES","SPECIAL TOOLS, FIXTURES, EQUIPMENT AND CONSUMABLES","ILLUSTRATED PARTS LIST",
                "SPECIAL PROCEDURES","REMOVAL","INSTALLATION","SERVICING","STORAGE INCLUDING TRANSPORTATION",
                "REWORK","APPENDIX A"};
    }
    public void createPageblockTitleRowForLotOrLoi(String s, Sheet sheet){
        Row row = sheet.createRow(sheet.getLastRowNum()+1);
        sheet.addMergedRegion(new CellRangeAddress(
                row.getRowNum(),  // Starting row
                row.getRowNum(),  // Ending row (same as starting row since it's a single row)
                0,  // Starting column
                5   // Ending column (5 columns - 1, 0-based index)
        ));
        Cell cell = row.createCell(0);
        cell.setCellValue(s);
        // Create a cell style with background color, bold font weight, and center alignment
        CellStyle tableFigurePageblockRowStyle = sheet.getWorkbook().createCellStyle();
        tableFigurePageblockRowStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        tableFigurePageblockRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font tableFigurePageblockRowFont = sheet.getWorkbook().createFont();
        tableFigurePageblockRowFont.setBold(true);
        tableFigurePageblockRowFont.setColor(IndexedColors.WHITE.getIndex());
        tableFigurePageblockRowStyle.setFont(tableFigurePageblockRowFont);
        tableFigurePageblockRowStyle.setAlignment(HorizontalAlignment.CENTER);
        // Apply the style to the cell
        cell.setCellStyle(tableFigurePageblockRowStyle);
    }
    public static boolean isFileAvailable(String path, String filename) {
        File file = new File(path, filename);
        // Check if the file exists
        if (!file.exists()) {
            return true; // File does not exist
        }

        // Check if the file is currently open
        try (FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE)) {
            // Try to acquire a lock on the file
            FileLock lock = channel.tryLock();

            if (lock == null) {
                // File is open
                return false;
            }

            // Release the lock
            lock.release();
        } catch (IOException e) {
            // Handle IOException if needed
            e.printStackTrace();
        }

        return true; // File exists but is not open
    }

    private void showFM_RevisionChangesExcelIsOpened() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("FM_RevisionChanges.xlsx File Is Opened By You");
        alert.setHeaderText(null);
        alert.setContentText("Please check if FM_RevisionChanges.xlsx is already open by you." +"\n"
                + "If open please close the file and try again");
        alert.showAndWait();
    }
}
