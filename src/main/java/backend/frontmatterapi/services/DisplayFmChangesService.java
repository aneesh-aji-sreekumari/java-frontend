package backend.frontmatterapi.services;
import backend.frontmatterapi.dtos.DisplayChangesRequestDto;
import backend.frontmatterapi.models.*;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class DisplayFmChangesService {
    private ExcelService excelService;
    public DisplayFmChangesService(){
        this.excelService = new ExcelService();
    }
    public Optional<Workbook> getOutputExcel(DisplayChangesRequestDto displayChangesRequestDto) {
        try{
            Workbook workbook = excelService.createExcelWorkbookForDisplayChanges();
            compareOldAndNewTocForDisplay(displayChangesRequestDto.getOldToc(), displayChangesRequestDto.getNewToc(), workbook);
            compareLotDisplayChanges(displayChangesRequestDto.getOldPageblockWiseLot(), displayChangesRequestDto.getNewPageblockWiseLot(), workbook);
            compareLoiDisplayChanges(displayChangesRequestDto.getOldPageblockWiseLoi(), displayChangesRequestDto.getNewPageblockWiseLoi(), workbook);
            return Optional.of(workbook);
        }catch (Exception e){
           e.printStackTrace();
           return Optional.empty();
        }
    }
    public void compareOldAndNewTocForDisplay(ArrayList<PageBlock> oldToc, ArrayList<PageBlock> newToc, Workbook workbook) {
        ArrayList<String> ans = new ArrayList<>();
        int N = oldToc.size();
        Sheet sheet = workbook.getSheetAt(0);
        for (int i = 0; i < N; i++) {
            PageBlock oldPageBlock = oldToc.get(i);
            PageBlock newPageBlock = newToc.get(i);
            excelService.createDisplayPageblockRow(oldPageBlock, newPageBlock, sheet);
            ArrayList<SubTopic> oldSubTopicList = oldPageBlock.subTopicList;
            ArrayList<SubTopic> newSubTopicList = newPageBlock.subTopicList;
            int oldLen = oldSubTopicList.size();
            int newLen = newSubTopicList.size();
            int j = 0, k = 0;
            while (j < oldLen && k < newLen) {
                SubTopic oldSubTopic = oldSubTopicList.get(j);
                SubTopic newSubTopic = newSubTopicList.get(k);
                excelService.createDisplayTaskRow(oldSubTopic, newSubTopic, sheet);
                ArrayList<SubSubTopic> oldSubSubTopicList = oldSubTopic.subSubTopicList;
                ArrayList<SubSubTopic> newSubSubTopicList = newSubTopic.subSubTopicList;
                int x = oldSubSubTopicList.size();
                int y = newSubSubTopicList.size();
                int l = 0, m = 0;
                while (l < x && m < y) {
                    SubSubTopic oldSubSubTopic = oldSubSubTopicList.get(l);
                    SubSubTopic newSubSubTopic = newSubSubTopicList.get(m);
                    excelService.createDisplaySubTaskRow(oldSubSubTopic, newSubSubTopic, sheet);
                    l++;
                    m++;
                }
                while(l < x){
                    SubSubTopic oldSubSubTopic = oldSubSubTopicList.get(l);
                    excelService.createDisplaySubTaskRow(oldSubSubTopic, 0, sheet);
                    l++;
                }
                while (m < y) {
                    SubSubTopic newSubSubTopic = newSubSubTopicList.get(m);
                    excelService.createDisplaySubTaskRow(newSubSubTopic, 1, sheet);
                    m++;
                }
                j++;
                k++;
            }
            while (j < oldLen) {
                SubTopic oldSubTopic = oldSubTopicList.get(j);
                excelService.createDisplayTaskRow(oldSubTopic, 0, sheet);
                ArrayList<SubSubTopic> oldSubSubTopicList = oldSubTopic.subSubTopicList;
                int len = oldSubSubTopicList.size();
                for (int t = 0; t < len; t++) {
                    SubSubTopic oldSubSubTopic = oldSubSubTopicList.get(t);
                    excelService.createDisplaySubTaskRow(oldSubSubTopic, 0, sheet);
                }
                j++;
            }
            while (k < newLen) {
                SubTopic newSubTopic = newSubTopicList.get(k);
                excelService.createDisplayTaskRow(newSubTopic, 1, sheet);
                ArrayList<SubSubTopic> newSubSubTopicList = newSubTopic.subSubTopicList;
                int len = newSubSubTopicList.size();
                for (int t = 0; t < len; t++) {
                    SubSubTopic newSubSubTopic = newSubSubTopicList.get(t);
                    excelService.createDisplaySubTaskRow(newSubSubTopic, 1, sheet);
                }
                k++;
            }

        }
    }
    public void compareLotDisplayChanges(HashMap<String, LotPageblockItem> oldLot, HashMap<String, LotPageblockItem> newLot, Workbook workbook){
       String[] order = excelService.getOrder();
       Sheet sheet = workbook.getSheetAt(1);
       for(int t=0; t<order.length; t++){
           String s = order[t];
           if(oldLot.containsKey(s) && newLot.containsKey(s)){
               LotPageblockItem oldLotPageblockItem = oldLot.get(s);
               excelService.createPageblockTitleRowForLotOrLoi(s, sheet);
               LotPageblockItem newLotPageblockItem = newLot.get(s);
               ArrayList<LotItem> oldListOfTables = oldLotPageblockItem.getListOfTables();
               ArrayList<LotItem> newListOfTables = newLotPageblockItem.getListOfTables();
               int n = oldListOfTables.size();
               int m = newListOfTables.size();
               int i=0, j=0;
               while(i<n && j<m){
                   LotItem lotItemOld = oldListOfTables.get(i);
                   LotItem lotItemNew = newListOfTables.get(j);
                   excelService.createTableDisplayRow(lotItemOld, lotItemNew, sheet);
                   i++;
                   j++;

               }
               while(i<n){
                   LotItem lotItem = oldListOfTables.get(i);
                   excelService.createTableDisplayRow(lotItem, 0, sheet);
                   i++;
               }
               while(j<m){
                   LotItem lotItem = newListOfTables.get(j);
                   excelService.createTableDisplayRow(lotItem, 1, sheet);
                   j++;
               }
           }
           else if(oldLot.containsKey(s)){
               LotPageblockItem oldLotPageblockItem = oldLot.get(s);
               excelService.createPageblockTitleRowForLotOrLoi(s, sheet);
               ArrayList<LotItem> oldListOfTables = oldLotPageblockItem.getListOfTables();
               int n = oldListOfTables.size();
               int i=0;
               while(i<n){
                   LotItem lotItemOld = oldListOfTables.get(i);
                   excelService.createTableDisplayRow(lotItemOld, 0, sheet);
                   i++;
               }
           }
           else if(newLot.containsKey(s)){
               LotPageblockItem newLotPageblockItem = newLot.get(s);
               excelService.createPageblockTitleRowForLotOrLoi(s, sheet);
               ArrayList<LotItem> newListOfTables = newLotPageblockItem.getListOfTables();
               int n = newListOfTables.size();
               int i=0;
               while(i<n){
                   LotItem lotItemOld = newListOfTables.get(i);
                   excelService.createTableDisplayRow(lotItemOld, 1, sheet);
                   i++;
               }
           }
       }
    }
    public void compareLoiDisplayChanges(HashMap<String, LoiPageblockItem> oldLoi, HashMap<String, LoiPageblockItem> newLoi, Workbook workbook){
        String[] order = excelService.getOrder();
        Sheet sheet = workbook.getSheetAt(2);
        for(int t=0; t<order.length; t++){
            String s = order[t];
            if(oldLoi.containsKey(s) && newLoi.containsKey(s)){
                LoiPageblockItem oldLoiPageblockItem = oldLoi.get(s);
                excelService.createPageblockTitleRowForLotOrLoi(s, sheet);
                LoiPageblockItem newLoiPageblockItem = newLoi.get(s);
                ArrayList<LoiItem> oldListOfFigures = oldLoiPageblockItem.getListOfIllustrations();
                ArrayList<LoiItem> newListOfFigures = newLoiPageblockItem.getListOfIllustrations();
                int n = oldListOfFigures.size();
                int m = newListOfFigures.size();
                int i=0, j=0;
                while(i<n && j<m){
                    LoiItem loiItemOld = oldListOfFigures.get(i);
                    LoiItem loiItemNew = newListOfFigures.get(j);
                    excelService.createFigureDisplayRow(loiItemOld, loiItemNew, sheet);
                    i++;
                    j++;

                }
                while(i<n){
                    LoiItem loiItem = oldListOfFigures.get(i);
                    excelService.createFigureDisplayRow(loiItem, 0, sheet);
                    i++;
                }
                while(j<m){
                    LoiItem loiItem = newListOfFigures.get(j);
                    excelService.createFigureDisplayRow(loiItem, 1, sheet);
                    j++;
                }
            }
            else if(oldLoi.containsKey(s)){
                LoiPageblockItem oldLoiPageblockItem = oldLoi.get(s);
                excelService.createPageblockTitleRowForLotOrLoi(s, sheet);
                ArrayList<LoiItem> oldListOfFigures = oldLoiPageblockItem.getListOfIllustrations();
                int n = oldListOfFigures.size();
                int i=0;
                while(i<n){
                    LoiItem loiItemOld = oldListOfFigures.get(i);
                    excelService.createFigureDisplayRow(loiItemOld, 0, sheet);
                    i++;
                }
            }
            else if(newLoi.containsKey(s)){
                LoiPageblockItem newLoiPageblockItem = newLoi.get(s);
                excelService.createPageblockTitleRowForLotOrLoi(s, sheet);
                ArrayList<LoiItem> newListOfFigures = newLoiPageblockItem.getListOfIllustrations();
                int n = newListOfFigures.size();
                int i=0;
                while(i<n){
                    LoiItem loiItemOld = newListOfFigures.get(i);
                    excelService.createFigureDisplayRow(loiItemOld, 1, sheet);
                    i++;
                }
            }
        }
    }
}
