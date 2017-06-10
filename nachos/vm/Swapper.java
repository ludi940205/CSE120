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
            freeList.add(currSize++);
        return freeList.removeFirst();
    }

    public void freePageFromDisk(Pair pair) {
        lock.acquire();

        int pos = table.get(pair);
        freeList.add(pos);
        table.remove(pair);

        lock.release();
    }

    //swap from disk to memory, return the resulting ppn
    public boolean swapFromDiskToMemory(Pair inPair, TranslationEntry inEntry) {
        Lib.assertTrue(!inEntry.valid);
        Lib.assertTrue(!VMKernel.globalPageTable.isPageValid(inEntry.ppn));
        Lib.assertTrue(table.containsKey(inPair));

        lock.acquire();

        int ppn = inEntry.ppn;
        if (table.containsKey(inPair)) {
            int diskPos = table.get(inPair);
            byte[] buffer = new byte[pageSize];
            swapFile.read(diskPos * pageSize, buffer, 0, pageSize);
            System.arraycopy(buffer, 0, Machine.processor().getMemory(), ppn * pageSize, pageSize);
        }
//        for (int i = 0; i < Machine.processor().getTLBSize(); i++) {
//            TranslationEntry tlbEntry = Machine.processor().readTLBEntry(i);
//            if (tlbEntry.valid && tlbEntry.vpn == inEntry.vpn)
//                tlbEntry.ppn = ppn;
//            Machine.processor().writeTLBEntry(i, tlbEntry);
//        }
        inEntry.valid = true;
        inEntry.used = false;
        inEntry.dirty = false;
        lock.release();

        Lib.debug(dbgProcess, "swap (" + String.valueOf(inEntry.vpn) + ", " +
                String.valueOf(inEntry.ppn) + ") from disk to memory");
        logTLB();

        return true;
    }

    public boolean swapFromMemoryToDisk(Pair outPair, TranslationEntry outEntry) {
        lock.acquire();

        int diskPos;

        try {
            if (outEntry == null)
                return false;
            if (table.containsKey(outPair)) {
                if (!outEntry.dirty) {
                    return true;
                } else
                    diskPos = table.get(outPair);
            } else
                diskPos = findFreePosition();

            byte[] buffer = new byte[pageSize];
            System.arraycopy(Machine.processor().getMemory(), outEntry.ppn * pageSize, buffer, 0, pageSize);
            swapFile.write(diskPos * pageSize, buffer, 0, pageSize);
            table.put(outPair, diskPos);
            return true;
        }
        finally {
            for (int i = 0; i < Machine.processor().getTLBSize(); i++) {
                TranslationEntry tlbEntry = Machine.processor().readTLBEntry(i);
                if (tlbEntry.vpn == outEntry.vpn)
                    tlbEntry.valid = false;
                Machine.processor().writeTLBEntry(i, tlbEntry);
            }
            outEntry.valid = false;
            outEntry.dirty = false;
            outEntry.used = false;
            lock.release();

            Lib.debug(dbgProcess, "swap (" + String.valueOf(outEntry.vpn) + ", " +
                    String.valueOf(outEntry.ppn) + ") from memory to disk");
            logTLB();
        }
    }

    private void logTLB() {
        Processor processor = Machine.processor();
        Lib.debug(dbgProcess, "current TLB config:");
        for (int i = 0; i < processor.getTLBSize(); i++) {
            TranslationEntry tlbEntry = processor.readTLBEntry(i);
            if (tlbEntry.valid) {
                Lib.debug(dbgProcess, String.valueOf(tlbEntry.vpn) + " -> " + String.valueOf(tlbEntry.ppn));
            }
        }
    }

    private HashMap<Pair, Integer> table = new HashMap<>();

    private LinkedList<Integer> freeList = new LinkedList<>();

    private int currSize = 1;

    private String swapFileName = "swap";
    private OpenFile swapFile;

    private int pageSize = Processor.pageSize;

    private static int count = 0;

    private Lock lock = new Lock();

    private static final char dbgProcess = 'a';
}
