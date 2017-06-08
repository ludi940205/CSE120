package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Swapper {
    public Swapper() {
        Lib.assertTrue(count++ == 0);
        swapFile = ThreadedKernel.fileSystem.open(swapFileName, true);
        freeList.add(0);
    }

    public void terminate() {
        swapFile.close();
        ThreadedKernel.fileSystem.remove(swapFileName);
    }

    private int findFreePosition() {
        if (freeList.isEmpty())
            freeList.add(currSize);
        return freeList.removeFirst();
    }

    private int selectVictim() {

    }

    public boolean swapFromDiskToMemory(Pair pair) {
        int pos = table.get(pair);

    }

    public boolean swapFromMemoryToDisk(Pair pair) {
        int pos;
        TranslationEntry entry = VMKernel.globalPageTable.getPage();
        if (entry == null)
            return false;
        if (table.containsKey(pair)) {
            if (!entry.dirty)
                return true;
            else
                pos = table.get(pair);
        }
        else
            pos = findFreePosition();

        byte[] buffer =
        swapFile.wr
    }

    private HashMap<Pair, Integer> table = new HashMap<>();

    private LinkedList<Integer> freeList = new LinkedList<>();

    private int currSize = 0;

    private String swapFileName = "swapFile";
    private OpenFile swapFile;

    private int pageSize;

    private static int count = 0;
}
