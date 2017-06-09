package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

class GlobalPageTable extends HashMap<Pair, TranslationEntry> {
    GlobalPageTable() {
        invertedPageTable = new Pair[Machine.processor().getNumPhysPages()];
        freePages = new LinkedList<>();
        for (int i = 0; i < invertedPageTable.length; i++)
            freePages.add(i);
        pinnedPages = new HashSet<>();
        swapper = new Swapper();
    }

    // insert a page into global page table, if no place available, swap out a page to disk
    void insertPage(Pair pair, TranslationEntry entry) {
        lock.acquire();

        if (freePages.isEmpty()) {
            int ppn = selectVictim();
            Pair original = invertedPageTable[ppn];
            swapper.swapFromMemoryToDisk(original);
            entry.ppn = ppn;
        }
        else {
            entry.ppn = freePages.removeFirst();
        }
        entry.valid = true;
        put(pair, entry);
        invertedPageTable[entry.ppn] = pair;

        lock.release();
    }

    // get a page from global page table, if the page is not in memory, swap in a page to memory
    TranslationEntry getPage(Pair pair) {
        lock.acquire();
        TranslationEntry entry = get(pair);
        if (entry == null)
            return null;
        else if (!entry.valid) {
            if (freePages.isEmpty()) {
                entry.ppn = swapper.swapFromMemoryToDisk(pair);
                entry.ppn = swapper.swapFromDiskToMemory(entry);
                entry.valid = true;
            }
        }
        lock.release();
        return entry;
    }

    void removePage(Pair pair) {
        TranslationEntry entry  = get(pair);
        if (entry == null)
            return;
        if (!entry.valid)
            swapper.freePageFromDisk(pair);
        freePages.add(entry.ppn);
        remove(pair);
        invertedPageTable[entry.ppn] = null;
    }

    void pinPage(Pair pair) {
        pinnedPages.add(pair);
    }

    void unpinPage(Pair pair) {
        pinnedPages.remove(pair);
    }

    private int selectVictim() {
        while (true) {
            TranslationEntry entry = get(invertedPageTable[victim]);
            if (!entry.used && !pinnedPages.contains(invertedPageTable[victim])) {
                int toEvict = victim;
                victim = (victim + 1) % invertedPageTable.length;
                return toEvict;
            }
            entry.used = false;
            victim = (victim + 1) % invertedPageTable.length;
        }
    }

    private int victim = 0;

    private HashSet<Pair> pinnedPages;

    private Pair[] invertedPageTable;

    private LinkedList<Integer> freePages;

    private Lock lock;

    private Swapper swapper;
}
