package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

import java.io.File;
import java.util.HashMap;

/**
 * A kernel that can support multiple demand-paging user processes.
 */
public class VMKernel extends UserKernel {
	/**
	 * Allocate a new VM kernel.
	 */
	public VMKernel() {
		super();
	}

	/**
	 * Initialize this kernel.
	 */
	public void initialize(String[] args) {
		super.initialize(args);
		swapFile = ThreadedKernel.fileSystem.open(swapFileName, true);
		globalPageTable = new PageTable();
	}

	/**
	 * Test this kernel.
	 */
	public void selfTest() {
		super.selfTest();
	}

	/**
	 * Start running user programs.
	 */
	public void run() {
		super.run();
	}

	/**
	 * Terminate this kernel. Never returns.
	 */
	public void terminate() {
		swapFile.close();
		ThreadedKernel.fileSystem.remove(swapFileName);
		super.terminate();
	}

	class PageTable extends HashMap<Integer[], TranslationEntry> {
		boolean insertPage(Integer[] pidAndVpn, TranslationEntry entry) {
			lock.acquire();
			put(pidAndVpn, entry);
			lock.release();
			return true;
		}
		private Lock lock;
	}

	// dummy variables to make javac smarter
	private static VMProcess dummy1 = null;

	private static final char dbgVM = 'v';

	private static String swapFileName = "swap";
	private static OpenFile swapFile;

	static PageTable globalPageTable;
}
