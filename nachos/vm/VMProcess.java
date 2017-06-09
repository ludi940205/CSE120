package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;
import org.omg.CORBA.FREE_MEM;
import sun.misc.VM;

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
		invalidateTLB();
	}

	/**
	 * Restore the state of this process after a context switch. Called by
	 * <tt>UThread.restoreState()</tt>.
	 */
	public void restoreState() {
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
		super.unloadSections();
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

	protected boolean lazyLoad(int vpn) {
		if (vpn < 0 || vpn >= numPages)
			return false;
		TranslationEntry entry = VMKernel.globalPageTable.getPage(new Pair(processID(), vpn));
		if (entry == null) {
			if (vpn < numPages - stackPages - 1) {
				CoffSection section = coff.getSection(vpn);

				TranslationEntry newEntry = new TranslationEntry(vpn, -1,
						true, section.isReadOnly(), false, false);
				VMKernel.globalPageTable.insertPage(new Pair(processID(), vpn), newEntry);
				section.loadPage(vpn - section.getFirstVPN(), newEntry.ppn);
			}
			else {
				TranslationEntry newEntry = new TranslationEntry(vpn, -1,
						true, false, false, false);
				VMKernel.globalPageTable.insertPage(new Pair(processID(), vpn), newEntry);
				Arrays.fill(Machine.processor().getMemory(),
						newEntry.ppn * pageSize, (newEntry.ppn+1) * pageSize, (byte) 0);
			}
		}
		return true;
	}

	@Override
	protected int pinVirtualPage(int vpn, boolean isUserWrite) {
		if (vpn < 0 || vpn >= pageTable.length)
			return -1;
		if (!lazyLoad(vpn))
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

		return entry.ppn;
	}

	@Override
	protected void unpinVirtualPage(int vpn) {
		VMKernel.globalPageTable.unpinPage(new Pair(processID(), vpn));
	}

	private void synchronizeTLB() {
		Processor processor = Machine.processor();
		for (int i = 0; i < processor.getTLBSize(); i++) {
			TranslationEntry tlbEntry = processor.readTLBEntry(i);
			TranslationEntry pageTableEntry = pageTable[tlbEntry.vpn];
			tlbEntry = new TranslationEntry(pageTableEntry);
			processor.writeTLBEntry(i, tlbEntry);
		}
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
		if (pageEntry.valid)
			processor.writeTLBEntry(tlbVictim, pageEntry);
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

		switch (cause) {
			case Processor.exceptionTLBMiss:
				handleTLBMiss(processor.readRegister(Processor.regBadVAddr));
				break;
		default:
			super.handleException(cause);
			break;
		}
	}

	private Lock

	private static final int pageSize = Processor.pageSize;

	private static final char dbgProcess = 'a';

	private static final char dbgVM = 'v';
}
