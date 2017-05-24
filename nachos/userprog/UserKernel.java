package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.util.*;

/**
 * A kernel that can support multiple user processes.
 */
public class UserKernel extends ThreadedKernel {
	/**
	 * Allocate a new user kernel.
	 */
	public UserKernel() {
		super();
	}

	/**
	 * Initialize this kernel. Creates a synchronized console and sets the
	 * processor's exception handler.
	 */
	public void initialize(String[] args) {
		super.initialize(args);

		console = new SynchConsole(Machine.console());

		Machine.processor().setExceptionHandler(new Runnable() {
			public void run() {
				exceptionHandler();
			}
		});

		pageTable = new PageTable();
		processTable = new ProcessTable();
		fileTable = new FileTable();
	}

	/**
	 * Test the console device.
	 */
	public void selfTest() {
		super.selfTest();

//		System.out.println("Testing the console device. Typed characters");
//		System.out.println("will be echoed until q is typed.");
//
//		char c;
//
//		do {
//			c = (char) console.readByte(true);
//			console.writeByte(c);
//		} while (c != 'q');

		System.out.println("");
	}

	/**
	 * Returns the current process.
	 * 
	 * @return the current process, or <tt>null</tt> if no process is current.
	 */
	public static UserProcess currentProcess() {
		if (!(KThread.currentThread() instanceof UThread))
			return null;

		return ((UThread) KThread.currentThread()).process;
	}

	/**
	 * The exception handler. This handler is called by the processor whenever a
	 * user instruction causes a processor exception.
	 * 
	 * <p>
	 * When the exception handler is invoked, interrupts are enabled, and the
	 * processor's cause register contains an integer identifying the cause of
	 * the exception (see the <tt>exceptionZZZ</tt> constants in the
	 * <tt>Processor</tt> class). If the exception involves a bad virtual
	 * address (e.g. page fault, TLB miss, read-only, bus error, or address
	 * error), the processor's BadVAddr register identifies the virtual address
	 * that caused the exception.
	 */
	public void exceptionHandler() {
		Lib.assertTrue(KThread.currentThread() instanceof UThread);

		UserProcess process = ((UThread) KThread.currentThread()).process;
		int cause = Machine.processor().readRegister(Processor.regCause);
		process.handleException(cause);
	}

	/**
	 * Start running user programs, by creating a process and running a shell
	 * program in it. The name of the shell program it must run is returned by
	 * <tt>Machine.getShellProgramName()</tt>.
	 * 
	 * @see nachos.machine.Machine#getShellProgramName
	 */
	public void run() {
		super.run();

		UserProcess process = UserProcess.newUserProcess();

		String shellProgram = Machine.getShellProgramName();
		Lib.assertTrue(process.execute(shellProgram, new String[] {}));

		KThread.currentThread().finish();
	}

	/**
	 * Terminate this kernel. Never returns.
	 */
	public void terminate() {
		super.terminate();
	}

	public class PageTable extends LinkedList<Integer> {
		public PageTable() {
			super();
			for (int i = 0; i < Machine.processor().getNumPhysPages(); i++) {
				offer(i);
			}
		}

		public int getNumFreePages() {
			lock.acquire();
			int size = size();
			lock.release();
			return size;
		}

		public int getFreePage() {
			lock.acquire();
			Integer next = poll();
			lock.release();
			return next == null ? -1 : next;
		}

		public void addFreePage(int i) {
			lock.acquire();
			offer(i);
			lock.release();
		}

		private Lock lock = new Lock();
	}

	public class ProcessTable extends HashMap<Integer, UserProcess> {
		public ProcessTable() {
			super();
		}

		public UserProcess addNewProcess(Integer pID, UserProcess process) {
			lock.acquire();
			UserProcess ret = put(pID, process);
			lock.release();
			return ret;
		}

		public UserProcess getProcess(Integer pID) {
			lock.acquire();
			UserProcess process = get(pID);
			lock.release();
			return process;
		}

		public UserProcess removeProcess(Integer pID) {
			lock.acquire();
			UserProcess process = remove(pID);
			lock.release();
			return process;
		}

		public int getProcessNum() {
			lock.acquire();
			int size = size();
			lock.release();
			return size;
		}

		public boolean stillRunning(Integer pID) {
			lock.acquire();
			boolean ret = containsKey(pID);
			lock.release();
			return ret;
		}

		Lock lock = new Lock();
	}

	/**
	 *  Record opening files.
	 *  The key of the table are filenames; the value of the table are their reference count
	 *  and a unlink flag. When the unlink flag is set to true, the file is deleted from the
	 *  file system when the reference count equals 0.
	 */
	public class FileTable extends HashMap<String, Integer[]> {
		public FileTable() {
		}

		public void increFileRefCount(String fileName) {
			lock.acquire();
			if (!containsKey(fileName))
				put(fileName, new Integer[2]);
			else {
				Integer[] pair = get(fileName);
				pair[1]++;
				put(fileName, pair);
			}
			lock.release();
		}

		public int decreFileRefCount(String fileName, boolean unlink) {
			lock.acquire();
			int refCount;
			try {
				if (fileName.equals("") || !containsKey(fileName))
					return -1;
				refCount = get(fileName)[0];
				boolean newUnlink = (get(fileName)[1] == 1) | unlink;
				if (refCount <= 0)
					return -1;
				Integer[] pair = new Integer[]{--refCount, newUnlink ? 1 : 0};
				put(fileName, pair);
				if (newUnlink && refCount == 0) {
					ThreadedKernel.fileSystem.remove(fileName);
				}
			}
			finally {
				lock.release();
			}

			return refCount;
		}

		private Lock lock = new Lock();
	}

	public static ProcessTable processTable;

	public static FileTable fileTable;

	/** Globally accessible reference to the synchronized console. */
	public static SynchConsole console;

	// dummy variables to make javac smarter
	private static Coff dummy1 = null;

	public static PageTable pageTable;
}
