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
    public int swapFromDiskToMemory(Pair pair) {
        TranslationEntry entry = VMKernel.globalPageTable.getPage(pair);
        Lib.assertTrue(!entry.valid);

        int pos = table.get(pair);
        byte[] memory = Machine.processor().getMemory();
        byte[] buffer;

    }

    public boolean swapFromMemoryToDisk(Pair pair) {
        int pos;
        TranslationEntry entry = VMKernel.globalPageTable.getPage(pair);
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

        byte[] buffer = new byte[pageSize];
        System.arraycopy(Machine.processor().getMemory(), entry.ppn * pageSize, buffer, 0, pageSize);
        swapFile.write(pos, buffer, 0, pageSize);
        table.put(pair, pos);
        return true;
    }

    private HashMap<Pair, Integer> table = new HashMap<>();

    private LinkedList<Integer> freeList = new LinkedList<>();

    private int currSize = 0;

    private String swapFileName = "swapFile";
    private OpenFile swapFile;

    private int pageSize = Processor.pageSize;

    private static int count = 0;
}
