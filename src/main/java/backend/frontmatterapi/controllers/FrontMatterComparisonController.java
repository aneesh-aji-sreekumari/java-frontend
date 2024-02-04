package backend.frontmatterapi.controllers;

import backend.frontmatterapi.dtos.DisplayChangesRequestDto;
import backend.frontmatterapi.models.*;
import backend.frontmatterapi.multithreading.TupleThread;
import backend.frontmatterapi.services.*;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FrontMatterComparisonController {
    final
    FrontMatterComparisonService frontMatterComparisonService;
    final
    TOCComparisonService tocComparisonService;
    final
    LOTComparisonService lotComparisonService;
    final
    LOIComparisonService loiComparisonService;

    public FrontMatterComparisonController() {
        this.frontMatterComparisonService = new FrontMatterComparisonService();
        this.tocComparisonService = new TOCComparisonService();
        this.lotComparisonService = new LOTComparisonService();
        this.loiComparisonService = new LOIComparisonService();
    }

    public FrontmatterComparisonResult processPDF(File pdfFile1, File pdfFile2) {
        FrontmatterComparisonResult frontmatterComparisonResult = new FrontmatterComparisonResult();
        try {
            /* The tuple will contain the raw lines from toc, lot and loi as arrayList */
//            Optional<Tuple> oldFile = frontMatterComparisonService.readPDF(pdfFile1);
//            Optional<Tuple> newFile = frontMatterComparisonService.readPDF(pdfFile2);
            ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            System.out.println(" Available Processors :" + Runtime.getRuntime().availableProcessors());
            TupleThread tupleThreadOld = new TupleThread(frontMatterComparisonService, es, pdfFile1);
            TupleThread tupleThreadNew = new TupleThread(frontMatterComparisonService, es, pdfFile2);
            Future<Optional<Tuple>> promiseOfOld = es.submit(tupleThreadOld);
            Future<Optional<Tuple>> promiseOfNew = es.submit(tupleThreadNew);
            Optional<Tuple> oldFile = promiseOfOld.get();
            Optional<Tuple> newFile = promiseOfNew.get();
            if(oldFile.isEmpty() || newFile.isEmpty())
                return frontmatterComparisonResult;

            /* Raw lines extracted from PDF from TOC, LOI, LOT Respectively */
            Optional<List<String>> oldTocList = Optional.of(oldFile.get().getTocList());
            Optional<List<String>> newTocList = Optional.of(newFile.get().getTocList());
            Optional<List<String>> oldLoiList = Optional.of(oldFile.get().getLoiList());
            Optional<List<String>> newLoiList = Optional.of(newFile.get().getLoiList());
            Optional<List<String>> oldLotList = Optional.of(oldFile.get().getLotList());
            Optional<List<String>> newLotList = Optional.of(newFile.get().getLotList());

            /* Filtering Unwanted Files From the lines Extracted From TOC/LOT/LOI*/
            Optional<ArrayList<String>> filteredOldLoi = loiComparisonService.filterUnwantedLinesFromLatestLotLoi(oldLoiList.get());
            Optional<ArrayList<String>> filteredNewLoi = loiComparisonService.filterUnwantedLinesFromLatestLotLoi(newLoiList.get());
            Optional<ArrayList<String>> filteredOldLot = lotComparisonService.filterUnwantedLinesFromLatestLotLoi(oldLotList.get());
            Optional<ArrayList<String>> filteredNewLot = lotComparisonService.filterUnwantedLinesFromLatestLotLoi(newLotList.get());
            /* Checking if the filtered Arrays have null value or not*/
            if(filteredOldLoi.isEmpty() || filteredNewLoi.isEmpty() || filteredOldLot.isEmpty() || filteredNewLot.isEmpty())
                throw new InputMismatchException("There was some issues with the selected Files");
            /* Merging multiline title to single line */
            Optional<ArrayList<String>> formattedOldLot = lotComparisonService.makeMultilineTitlesSinglelineVersion1(filteredOldLot.get());
            Optional<ArrayList<String>> formattedNewLot = lotComparisonService.makeMultilineTitlesSinglelineVersion1(filteredNewLot.get());
            Optional<ArrayList<String>> formattedOldLoi = loiComparisonService.makeMultilineTitlesSinglelineVersion1(filteredOldLoi.get());
            Optional<ArrayList<String>> formattedNewLoi = loiComparisonService.makeMultilineTitlesSinglelineVersion1(filteredNewLoi.get());
             if (formattedOldLot.isEmpty() || formattedNewLot.isEmpty() || formattedOldLoi.isEmpty() || formattedNewLoi.isEmpty())
                 throw new InputMismatchException("There was some issues with the selected Files");

             Optional<HashMap<String, LoiPageblockItem>> oldPageblockWiseLoi = loiComparisonService.getPageblockwiseIllustrations(formattedOldLoi.get());
            Optional<HashMap<String, LoiPageblockItem>> newPageblockWiseLoi = loiComparisonService.getPageblockwiseIllustrations(formattedNewLoi.get());
            Optional<ArrayList<String>> loiComparisonResult = loiComparisonService.compareLoi(oldPageblockWiseLoi.get(), newPageblockWiseLoi.get());

            Optional<HashMap<String, LotPageblockItem>> oldPageblockWiseLot = lotComparisonService.getPageblockwiseTables(formattedOldLot.get());
            Optional<HashMap<String, LotPageblockItem>> newPageblockWiseLot = lotComparisonService.getPageblockwiseTables(formattedNewLot.get());
            Optional<ArrayList<String>> lotComparisonResult = lotComparisonService.compareLot(oldPageblockWiseLot.get(), newPageblockWiseLot.get());




            /* Getting the revision changes comparing old TOC and new TOC */
            Optional<ArrayList<String>> tocOutput
                    = tocComparisonService.getRevisionChangesInToc(oldTocList, newTocList);
            if(loiComparisonResult.isPresent())
                frontmatterComparisonResult.setLoiComparisonResult(loiComparisonResult);

            if(lotComparisonResult.isPresent())
                frontmatterComparisonResult.setLotComparisonResult(lotComparisonResult);
           //*********************NewCode Start **************************************
            if(tocOutput.isPresent())
                frontmatterComparisonResult.setTocComparisonResult(tocOutput);
            //*********************NewCode End **************************************
            return frontmatterComparisonResult;
        } catch (Exception e) {
            return new FrontmatterComparisonResult();
        }
    }
    public Optional<HashMap<String, ArrayList<FmChangeItem>>> processPDFAutomation(File pdfFile1, File pdfFile2) {
        try {
            /* The tuple will contain the raw lines from toc, lot and loi as arrayList */
//            Optional<Tuple> oldFile = frontMatterComparisonService.readPDF(pdfFile1);
//            Optional<Tuple> newFile = frontMatterComparisonService.readPDF(pdfFile2);
            ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            System.out.println(" Available Processors :" + Runtime.getRuntime().availableProcessors());
            TupleThread tupleThreadOld = new TupleThread(frontMatterComparisonService, es, pdfFile1);
            TupleThread tupleThreadNew = new TupleThread(frontMatterComparisonService, es, pdfFile2);
            Future<Optional<Tuple>> promiseOfOld = es.submit(tupleThreadOld);
            Future<Optional<Tuple>> promiseOfNew = es.submit(tupleThreadNew);
            Optional<Tuple> oldFile = promiseOfOld.get();
            Optional<Tuple> newFile = promiseOfNew.get();
            if(oldFile.isEmpty() || newFile.isEmpty()){
                //Handle the error
            }
            /* Raw lines extracted from PDF from TOC, LOI, LOT Respectively */
            Optional<List<String>> oldTocList = Optional.of(oldFile.get().getTocList());
            Optional<List<String>> newTocList = Optional.of(newFile.get().getTocList());
            Optional<List<String>> oldLoiList = Optional.of(oldFile.get().getLoiList());
            Optional<List<String>> newLoiList = Optional.of(newFile.get().getLoiList());
            Optional<List<String>> oldLotList = Optional.of(oldFile.get().getLotList());
            Optional<List<String>> newLotList = Optional.of(newFile.get().getLotList());

            /* Filtering Unwanted Files From the lines Extracted From LOT/LOI*/
            Optional<ArrayList<String>> filteredOldLoi = loiComparisonService.filterUnwantedLinesFromLatestLotLoi(oldLoiList.get());
            Optional<ArrayList<String>> filteredNewLoi = loiComparisonService.filterUnwantedLinesFromLatestLotLoi(newLoiList.get());
            Optional<ArrayList<String>> filteredOldLot = lotComparisonService.filterUnwantedLinesFromLatestLotLoi(oldLotList.get());
            Optional<ArrayList<String>> filteredNewLot = lotComparisonService.filterUnwantedLinesFromLatestLotLoi(newLotList.get());
            /* Checking if the filtered Arrays have null value or not*/
            if(filteredOldLoi.isEmpty() || filteredNewLoi.isEmpty() || filteredOldLot.isEmpty() || filteredNewLot.isEmpty())
                throw new InputMismatchException("There was some issues with the selected Files");
            /* Merging multiline title to single line */
            Optional<ArrayList<String>> formattedOldLot = lotComparisonService.makeMultilineTitlesSinglelineVersion1(filteredOldLot.get());
            Optional<ArrayList<String>> formattedNewLot = lotComparisonService.makeMultilineTitlesSinglelineVersion1(filteredNewLot.get());
            Optional<ArrayList<String>> formattedOldLoi = loiComparisonService.makeMultilineTitlesSinglelineVersion1(filteredOldLoi.get());
            Optional<ArrayList<String>> formattedNewLoi = loiComparisonService.makeMultilineTitlesSinglelineVersion1(filteredNewLoi.get());
            if (formattedOldLot.isEmpty() || formattedNewLot.isEmpty() || formattedOldLoi.isEmpty() || formattedNewLoi.isEmpty())
                throw new InputMismatchException("There was some issues with the selected Files");

            Optional<HashMap<String, LoiPageblockItem>> oldPageblockWiseLoi = loiComparisonService.getPageblockwiseIllustrations(formattedOldLoi.get());
            Optional<HashMap<String, LoiPageblockItem>> newPageblockWiseLoi = loiComparisonService.getPageblockwiseIllustrations(formattedNewLoi.get());

            Optional<HashMap<String, LotPageblockItem>> oldPageblockWiseLot = lotComparisonService.getPageblockwiseTables(formattedOldLot.get());
            Optional<HashMap<String, LotPageblockItem>> newPageblockWiseLot = lotComparisonService.getPageblockwiseTables(formattedNewLot.get());
            HashMap<String, ArrayList<FmChangeItem>> map = new HashMap<>();
            tocComparisonService.getRevisionChangesInTocAutomation(oldTocList, newTocList, map);
            loiComparisonService.compareLoiAutomation(oldPageblockWiseLoi.get(), newPageblockWiseLoi.get(), map);
            lotComparisonService.compareLotAutomation(oldPageblockWiseLot.get(), newPageblockWiseLot.get(), map);
            if(map.isEmpty())
                map.put("empty", null);
            return Optional.of(map);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    public Optional<Workbook> processPDFDisplayInExcel(File pdfFile1, File pdfFile2) {
        try {
            /* The tuple will contain the raw lines from toc, lot and loi as arrayList */
//            Optional<Tuple> oldFile = frontMatterComparisonService.readPDF(pdfFile1);
//            Optional<Tuple> newFile = frontMatterComparisonService.readPDF(pdfFile2);
            ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            TupleThread tupleThreadOld = new TupleThread(frontMatterComparisonService, es, pdfFile1);
            TupleThread tupleThreadNew = new TupleThread(frontMatterComparisonService, es, pdfFile2);
            Future<Optional<Tuple>> promiseOfOld = es.submit(tupleThreadOld);
            Future<Optional<Tuple>> promiseOfNew = es.submit(tupleThreadNew);
            Optional<Tuple> oldFile = promiseOfOld.get();
            Optional<Tuple> newFile = promiseOfNew.get();
            if(oldFile.isEmpty() || newFile.isEmpty())
                return Optional.empty();

            /* Raw lines extracted from PDF from TOC, LOI, LOT Respectively */
            Optional<List<String>> oldTocList = Optional.of(oldFile.get().getTocList());
            Optional<List<String>> newTocList = Optional.of(newFile.get().getTocList());
            Optional<List<String>> oldLoiList = Optional.of(oldFile.get().getLoiList());
            Optional<List<String>> newLoiList = Optional.of(newFile.get().getLoiList());
            Optional<List<String>> oldLotList = Optional.of(oldFile.get().getLotList());
            Optional<List<String>> newLotList = Optional.of(newFile.get().getLotList());

            /* Filtering Unwanted Files From the lines Extracted From TOC/LOT/LOI*/
            Optional<ArrayList<String>> filteredOldLoi = loiComparisonService.filterUnwantedLinesFromLatestLotLoi(oldLoiList.get());
            Optional<ArrayList<String>> filteredNewLoi = loiComparisonService.filterUnwantedLinesFromLatestLotLoi(newLoiList.get());
            Optional<ArrayList<String>> filteredOldLot = lotComparisonService.filterUnwantedLinesFromLatestLotLoi(oldLotList.get());
            Optional<ArrayList<String>> filteredNewLot = lotComparisonService.filterUnwantedLinesFromLatestLotLoi(newLotList.get());
            /* Checking if the filtered Arrays have null value or not*/
            if(filteredOldLoi.isEmpty() || filteredNewLoi.isEmpty() || filteredOldLot.isEmpty() || filteredNewLot.isEmpty())
                throw new InputMismatchException("There was some issues with the selected Files");
            /* Merging multiline title to single line */
            Optional<ArrayList<String>> formattedOldLot = lotComparisonService.makeMultilineTitlesSinglelineVersion1(filteredOldLot.get());
            Optional<ArrayList<String>> formattedNewLot = lotComparisonService.makeMultilineTitlesSinglelineVersion1(filteredNewLot.get());
            Optional<ArrayList<String>> formattedOldLoi = loiComparisonService.makeMultilineTitlesSinglelineVersion1(filteredOldLoi.get());
            Optional<ArrayList<String>> formattedNewLoi = loiComparisonService.makeMultilineTitlesSinglelineVersion1(filteredNewLoi.get());
            if (formattedOldLot.isEmpty() || formattedNewLot.isEmpty() || formattedOldLoi.isEmpty() || formattedNewLoi.isEmpty())
                throw new InputMismatchException("There was some issues with the selected Files");

            Optional<HashMap<String, LoiPageblockItem>> oldPageblockWiseLoi = loiComparisonService.getPageblockwiseIllustrations(formattedOldLoi.get());
            Optional<HashMap<String, LoiPageblockItem>> newPageblockWiseLoi = loiComparisonService.getPageblockwiseIllustrations(formattedNewLoi.get());
            //Optional<ArrayList<String>> loiComparisonResult = loiComparisonService.compareLoi(oldPageblockWiseLoi.get(), newPageblockWiseLoi.get());

            Optional<HashMap<String, LotPageblockItem>> oldPageblockWiseLot = lotComparisonService.getPageblockwiseTables(formattedOldLot.get());
            Optional<HashMap<String, LotPageblockItem>> newPageblockWiseLot = lotComparisonService.getPageblockwiseTables(formattedNewLot.get());
            //Optional<ArrayList<String>> lotComparisonResult = lotComparisonService.compareLot(oldPageblockWiseLot.get(), newPageblockWiseLot.get());
            Optional<HashMap<String, ArrayList<PageBlock>>> tocMap = tocComparisonService.getRevisionTOCLists(oldTocList, newTocList);
            if(tocMap.isEmpty())
                return Optional.empty();
            DisplayChangesRequestDto displayChangesRequestDto = new DisplayChangesRequestDto();
            displayChangesRequestDto.setNewToc(tocMap.get().get("new"));
            displayChangesRequestDto.setOldToc(tocMap.get().get("old"));
            displayChangesRequestDto.setNewPageblockWiseLoi(newPageblockWiseLoi.get());
            displayChangesRequestDto.setOldPageblockWiseLoi(oldPageblockWiseLoi.get());
            displayChangesRequestDto.setNewPageblockWiseLot(newPageblockWiseLot.get());
            displayChangesRequestDto.setOldPageblockWiseLot(oldPageblockWiseLot.get());
            DisplayFmChangesService displayFmChangesService = new DisplayFmChangesService();
            Optional<Workbook> outputExcel = displayFmChangesService.getOutputExcel(displayChangesRequestDto);
            return outputExcel;
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
