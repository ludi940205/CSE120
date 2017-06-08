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

    boolean insertPage(Pair pair, TranslationEntry entry) {
        lock.acquire();
        put(pair, entry);
        lock.release();
        return true;
    }

    TranslationEntry getPage(Pair pair) {
        lock.acquire();
        TranslationEntry entry  = get(pair);
        if (entry == null || !entry.valid) {
            if (entry.vpn >= 0) {
                CoffSection section =
            }
            swapper.swapFromDiskToMemory(pair);
        }
        lock.release();
        return entry;
    }

    TranslationEntry loadCoff(Coff coff) {

    }

    void pinPage(TranslationEntry entry) {
        pinnedPages.add(entry);
    }

    void unpinPage(TranslationEntry entry) {
        pinnedPages.remove(entry);
    }

    int selectVictim() {
        if (freePages.isEmpty()) {
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
        else
            return freePages.removeFirst();
    }

    private int victim = 0;

    private HashSet<TranslationEntry> pinnedPages;

    private Pair[] invertedPageTable;

    private LinkedList<Integer> freePages;

    private Lock lock;

    private Swapper swapper;
}
