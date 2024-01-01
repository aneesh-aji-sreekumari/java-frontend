package backend.frontmatterapi.controllers;

import backend.frontmatterapi.models.FrontmatterComparisonResult;
import backend.frontmatterapi.models.LoiPageblockItem;
import backend.frontmatterapi.models.LotPageblockItem;
import backend.frontmatterapi.models.Tuple;
import backend.frontmatterapi.multithreading.TupleThread;
import backend.frontmatterapi.services.FrontMatterComparisonService;
import backend.frontmatterapi.services.LOIComparisonService;
import backend.frontmatterapi.services.LOTComparisonService;
import backend.frontmatterapi.services.TOCComparisonService;
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
            Optional<ArrayList<String>> filteredOldLoi = lotComparisonService.filterUnwantedLinesFromLatestLotLoi(oldLoiList.get());
            Optional<ArrayList<String>> filteredNewLoi = lotComparisonService.filterUnwantedLinesFromLatestLotLoi(newLoiList.get());
            Optional<ArrayList<String>> filteredOldLot = lotComparisonService.filterUnwantedLinesFromLatestLotLoi(oldLotList.get());
            Optional<ArrayList<String>> filteredNewLot = lotComparisonService.filterUnwantedLinesFromLatestLotLoi(newLotList.get());
            /* Checking if the filtered Arrays have null value or not*/
            if(filteredOldLoi.isEmpty() || filteredNewLoi.isEmpty() || filteredOldLot.isEmpty() || filteredNewLot.isEmpty())
                throw new InputMismatchException("There was some issues with the selected Files");
            /* Merging multiline title to single line */
            Optional<ArrayList<String>> formattedOldLot = lotComparisonService.makeMultilineTitlesSingleline(filteredOldLot.get());
            Optional<ArrayList<String>> formattedNewLot = lotComparisonService.makeMultilineTitlesSingleline(filteredNewLot.get());
            Optional<ArrayList<String>> formattedOldLoi = loiComparisonService.makeMultilineTitlesSingleline(filteredOldLoi.get());
            Optional<ArrayList<String>> formattedNewLoi = loiComparisonService.makeMultilineTitlesSingleline(filteredNewLoi.get());
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
}
