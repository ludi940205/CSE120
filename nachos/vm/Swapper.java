package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import sun.misc.VM;

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
            freeList.add(currSize++);
        return freeList.removeFirst();
    }

    public void freePageFromDisk(Pair pair) {
        int pos = table.get(pair);
        freeList.add(pos);
        table.remove(pair);
    }

    //swap from disk to memory, return the resulting ppn
    public boolean swapFromDiskToMemory(Pair inPair, TranslationEntry inEntry) {
        Lib.assertTrue(!inEntry.valid);
        Lib.assertTrue(!VMKernel.globalPageTable.isPageValid(inEntry.ppn));

        int ppn = inEntry.ppn;
        if (!table.containsKey(inPair) || inEntry.dirty) {
            int diskPos = table.get(inPair);
            byte[] buffer = new byte[pageSize];
            swapFile.read(diskPos, buffer, 0, pageSize);
            System.arraycopy(buffer, 0, Machine.processor().getMemory(), ppn * pageSize, pageSize);
        }
        inEntry.valid = true;
        inEntry.used = false;
        inEntry.dirty = false;

        return true;
    }

    public boolean swapFromMemoryToDisk(Pair outPair, TranslationEntry outEntry) {
        int diskPos;
        if (outEntry == null)
            return false;

        int ppn = outEntry.ppn;
        if (table.containsKey(outPair)) {
            if (!outEntry.dirty)
                return true;
            else
                diskPos = table.get(outPair);
        }
        else
            diskPos = findFreePosition();
        for (int i = 0; i < Machine.processor().getTLBSize(); i++) {
            TranslationEntry tlbEntry = Machine.processor().readTLBEntry(i);
            if (tlbEntry.vpn == outEntry.vpn)
                tlbEntry.valid = false;
            Machine.processor().writeTLBEntry(i, tlbEntry);
        }
        outEntry.valid = false;
        outEntry.dirty = false;
        outEntry.used = false;

        byte[] buffer = new byte[pageSize];
        System.arraycopy(Machine.processor().getMemory(), ppn * pageSize, buffer, 0, pageSize);
        swapFile.write(diskPos, buffer, 0, pageSize);
        table.put(outPair, diskPos);
        return true;
    }

    private HashMap<Pair, Integer> table = new HashMap<>();

    private LinkedList<Integer> freeList = new LinkedList<>();

    private int currSize = 1;

    private String swapFileName = "swapFile";
    private OpenFile swapFile;

    private int pageSize = Processor.pageSize;

    private static int count = 0;
}
