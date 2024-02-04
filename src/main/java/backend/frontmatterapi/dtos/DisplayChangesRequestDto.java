package backend.frontmatterapi.dtos;

import backend.frontmatterapi.models.LoiPageblockItem;
import backend.frontmatterapi.models.LotPageblockItem;
import backend.frontmatterapi.models.PageBlock;

import java.util.ArrayList;
import java.util.HashMap;

public class DisplayChangesRequestDto {
    private HashMap<String, LoiPageblockItem> oldPageblockWiseLoi;
    private HashMap<String, LoiPageblockItem> newPageblockWiseLoi;
    private HashMap<String, LotPageblockItem> oldPageblockWiseLot;
    private HashMap<String, LotPageblockItem> newPageblockWiseLot;
    private ArrayList<PageBlock> oldToc;
    private ArrayList<PageBlock> newToc;

    public HashMap<String, LoiPageblockItem> getOldPageblockWiseLoi() {
        return oldPageblockWiseLoi;
    }

    public void setOldPageblockWiseLoi(HashMap<String, LoiPageblockItem> oldPageblockWiseLoi) {
        this.oldPageblockWiseLoi = oldPageblockWiseLoi;
    }

    public HashMap<String, LoiPageblockItem> getNewPageblockWiseLoi() {
        return newPageblockWiseLoi;
    }

    public void setNewPageblockWiseLoi(HashMap<String, LoiPageblockItem> newPageblockWiseLoi) {
        this.newPageblockWiseLoi = newPageblockWiseLoi;
    }

    public HashMap<String, LotPageblockItem> getOldPageblockWiseLot() {
        return oldPageblockWiseLot;
    }

    public void setOldPageblockWiseLot(HashMap<String, LotPageblockItem> oldPageblockWiseLot) {
        this.oldPageblockWiseLot = oldPageblockWiseLot;
    }

    public HashMap<String, LotPageblockItem> getNewPageblockWiseLot() {
        return newPageblockWiseLot;
    }

    public void setNewPageblockWiseLot(HashMap<String, LotPageblockItem> newPageblockWiseLot) {
        this.newPageblockWiseLot = newPageblockWiseLot;
    }

    public ArrayList<PageBlock> getOldToc() {
        return oldToc;
    }

    public void setOldToc(ArrayList<PageBlock> oldToc) {
        this.oldToc = oldToc;
    }

    public ArrayList<PageBlock> getNewToc() {
        return newToc;
    }

    public void setNewToc(ArrayList<PageBlock> newToc) {
        this.newToc = newToc;
    }
}
