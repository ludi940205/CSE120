package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * A <tt>UserProcess</tt> that supports demand-paging.
 */
public class VMProcess extends UserProcess {
	/**
	 * Allocate a new process.
	 */
	public VMProcess() {
		super();
	}

	/**
	 * Save the state of this process in preparation for a context switch.
	 * Called by <tt>UThread.saveState()</tt>.
	 */
	public void saveState() {
		super.saveState();
		previousTLBState = new TranslationEntry[Machine.processor().getTLBSize()];
		for (int i = 0; i < previousTLBState.length; i++)
			previousTLBState[i] = new TranslationEntry(Machine.processor().readTLBEntry(i));
		invalidateTLB();
	}

	/**
	 * Restore the state of this process after a context switch. Called by
	 * <tt>UThread.restoreState()</tt>.
	 */
	public void restoreState() {
		if (previousTLBState != null) {
			for (int i = 0; i < previousTLBState.length; i++) {
				TranslationEntry pageTableEntry = VMKernel.globalPageTable.get(
						new Pair(processID(), previousTLBState[i].vpn));
				if (pageTableEntry != null)
					previousTLBState[i].valid &= pageTableEntry.valid;
				Machine.processor().writeTLBEntry(i, previousTLBState[i]);
			}
		}
		synchronizeFromTLB();
	}

	/**
	 * Initializes page tables for this process so that the executable can be
	 * demand-paged.
	 * 
	 * @return <tt>true</tt> if successful.
	 */
	protected boolean loadSections() {
		return true;
	}

	/**
	 * Release any resources allocated by <tt>loadSections()</tt>.
	 */
	protected void unloadSections() {
//		super.unloadSections();
		invalidateTLB();
		for (int i = 0; i < numPages; i++)
			VMKernel.globalPageTable.removePage(new Pair(processID(), i));
	}

	private void invalidateTLB() {
		Processor processor = Machine.processor();
		for (int i = 0; i < processor.getTLBSize(); i++) {
			TranslationEntry tlbEntry = processor.readTLBEntry(i);
			tlbEntry.valid = false;
			processor.writeTLBEntry(i, tlbEntry);
		}
	}

	protected TranslationEntry lazyLoad(int vpn) {
		lazyLoadLock.acquire();
		if (vpn < 0 || vpn >= numPages)
			return null;
		TranslationEntry entry = VMKernel.globalPageTable.getPage(new Pair(processID(), vpn));
		if (entry == null) {
			TranslationEntry newEntry = new TranslationEntry(vpn, -1,
					true, false, false, false);
			if (vpn < numPages - stackPages - 1) {
				int s = 0;
				for (; s < coff.getNumSections(); s++)
					if (coff.getSection(s).getFirstVPN() > vpn)
						break;
				CoffSection section = coff.getSection(s - 1);
				newEntry.readOnly = section.isReadOnly();
				VMKernel.globalPageTable.insertPage(new Pair(processID(), vpn), newEntry);
				section.loadPage(vpn - section.getFirstVPN(), newEntry.ppn);
			}
			else {
				VMKernel.globalPageTable.insertPage(new Pair(processID(), vpn), newEntry);
				Arrays.fill(Machine.processor().getMemory(),
						newEntry.ppn * pageSize, (newEntry.ppn+1) * pageSize, (byte) 0);
			}
			entry = newEntry;
		}
		lazyLoadLock.release();
		return entry;
	}

	@Override
	protected int pinVirtualPage(int vpn, boolean isUserWrite) {
		if (vpn < 0 || vpn >= numPages)
			return -1;
		if (lazyLoad(vpn) == null)
			return -1;

		Pair pair = new Pair(processID(), vpn);
		TranslationEntry entry = VMKernel.globalPageTable.getPage(pair);
		VMKernel.globalPageTable.pinPage(pair);
		if (!entry.valid || entry.vpn != vpn)
			return -1;

		if (isUserWrite) {
			if (entry.readOnly)
				return -1;
			entry.dirty = true;
		}

		entry.used = true;

//		synchronizeToTLB();

		return entry.ppn;
	}

	@Override
	protected void unpinVirtualPage(int vpn) {
		VMKernel.globalPageTable.unpinPage(new Pair(processID(), vpn));
	}

	private void synchronizeToTLB() {
		VMKernel.globalPageTable.synchronizeToTLB(processID());
	}

	private void synchronizeFromTLB() {
		VMKernel.globalPageTable.synchronizeFromTLB(processID());
	}

	private int getTLBVictim() {
		Processor processor = Machine.processor();
		int tlbSize = processor.getTLBSize();
		for (int i = 0; i < tlbSize; i++) {
			TranslationEntry tlbEntry = processor.readTLBEntry(i);
			if (!tlbEntry.valid) {
				return i;
			}
		}
		return Lib.random(tlbSize);
	}

	private void handleTLBMiss(int vAddr) {
		Processor processor = Machine.processor();
		int tlbVictim = getTLBVictim();
		int vpn = Processor.pageFromAddress(vAddr);
		TranslationEntry pageEntry = VMKernel.globalPageTable.getPage(new Pair(processID(), vpn));
		if (pageEntry == null)
			pageEntry = lazyLoad(vpn);
		if (pageEntry.valid)
			processor.writeTLBEntry(tlbVictim, pageEntry);
//		Lib.assertTrue(checkTLB());
	}

	public boolean checkTLB() {
		Processor processor = Machine.processor();
		for (int i = 0; i < processor.getTLBSize(); i++) {
			TranslationEntry tEntry = processor.readTLBEntry(i);
			TranslationEntry pEntry = VMKernel.globalPageTable.get(new Pair(VMKernel.currentProcess().processID(), tEntry.vpn));
			if (tEntry.valid) {
//				System.out.println(String.valueOf(tEntry.valid) + String.valueOf(pEntry.valid));
//				System.out.println(String.valueOf(tEntry.vpn) + String.valueOf(pEntry.vpn));
//				System.out.println(String.valueOf(tEntry.ppn) + String.valueOf(pEntry.ppn));
				try {
					Lib.assertTrue(pEntry.valid);
					Lib.assertTrue(tEntry.vpn == pEntry.vpn);
					Lib.assertTrue(tEntry.ppn == pEntry.ppn);
					Lib.assertTrue(tEntry.dirty == pEntry.dirty);
					Lib.assertTrue(tEntry.used == pEntry.used);
				}
				catch (Error e) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Handle a user exception. Called by <tt>UserKernel.exceptionHandler()</tt>
	 * . The <i>cause</i> argument identifies which exception occurred; see the
	 * <tt>Processor.exceptionZZZ</tt> constants.
	 * 
	 * @param cause the user exception that occurred.
	 */
	public void handleException(int cause) {
		Processor processor = Machine.processor();
		synchronizeFromTLB();

		switch (cause) {
			case Processor.exceptionTLBMiss:
				handleTLBMiss(processor.readRegister(Processor.regBadVAddr));
				break;
		default:
			super.handleException(cause);
			break;
		}
	}

	private Lock lazyLoadLock = new Lock();

	private TranslationEntry[] previousTLBState;

	private static final int pageSize = Processor.pageSize;

	private static final char dbgProcess = 'a';

	private static final char dbgVM = 'v';
}
