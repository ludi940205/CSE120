package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

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
		for (int i = 0; i < Machine.processor().getTLBSize(); i++) {
			TranslationEntry tlbEntry = Machine.processor().readTLBEntry(i);
			tlbEntry.valid = false;
			Machine.processor().writeTLBEntry(i, tlbEntry);
		}
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
		return super.loadSections();
	}

	/**
	 * Release any resources allocated by <tt>loadSections()</tt>.
	 */
	protected void unloadSections() {
		super.unloadSections();
	}

	private void handleTLBMiss(int vAddr) {
		Processor processor = Machine.processor();
		int tlbSize = processor.getTLBSize();
		int tlbVictimPos = -1;
		for (int i = 0; i < tlbSize; i++) {
			TranslationEntry tlbEntry = processor.readTLBEntry(i);
			if (!tlbEntry.valid) {
				tlbVictimPos = i;
				break;
			}
		}
		if (tlbVictimPos == -1) {
			tlbVictimPos = Lib.random(tlbSize);
		}

		int vpn = Processor.pageFromAddress(vAddr);
		TranslationEntry pageEntry = pageTable[vpn];
		processor.writeTLBEntry(tlbVictimPos, pageEntry);
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
		default:
			super.handleException(cause);
			break;
		}
	}

	private static final int pageSize = Processor.pageSize;

	private static final char dbgProcess = 'a';

	private static final char dbgVM = 'v';
}
