package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.io.EOFException;
import java.util.*;

/**
 * Encapsulates the state of a user process that is not contained in its user
 * thread (or threads). This includes its address translation state, a file
 * table, and information about the program being executed.
 * 
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 * 
 * @see nachos.vm.VMProcess
 * @see nachos.network.NetProcess
 */
public class UserProcess {
	/**
	 * Allocate a new process.
	 */
	public UserProcess() {
		fdTable = new FileDescriptorTable();

		pIDLock.acquire();
		pID = currPID++;
		pIDLock.release();

		UserKernel.processTable.addNewProcess(pID, this);
	}

	/**
	 * Allocate and return a new process of the correct class. The class name is
	 * specified by the <tt>nachos.conf</tt> key
	 * <tt>Kernel.processClassName</tt>.
	 * 
	 * @return a new process of the correct class.
	 */
	public static UserProcess newUserProcess() {
		return (UserProcess) Lib.constructObject(Machine.getProcessClassName());
	}

	/**
	 * Execute the specified program with the specified arguments. Attempts to
	 * load the program, and then forks a thread to run it.
	 * 
	 * @param name the name of the file containing the executable.
	 * @param args the arguments to pass to the executable.
	 * @return <tt>true</tt> if the program was successfully executed.
	 */
	public boolean execute(String name, String[] args) {
		if (!load(name, args))
			return false;

		new UThread(this).setName(name).fork();

//		boolean intStatus = Machine.interrupt().disable();
//		KThread.readyQueue.print();
//		System.out.println();
//		Machine.interrupt().restore(intStatus);

		return true;
	}

	/**
	 * Save the state of this process in preparation for a context switch.
	 * Called by <tt>UThread.saveState()</tt>.
	 */
	public void saveState() {
	}

	/**
	 * Restore the state of this process after a context switch. Called by
	 * <tt>UThread.restoreState()</tt>.
	 */
	public void restoreState() {
		Machine.processor().setPageTable(pageTable);
	}

	/**
	 * Read a null-terminated string from this process's virtual memory. Read at
	 * most <tt>maxLength + 1</tt> bytes from the specified address, search for
	 * the null terminator, and convert it to a <tt>java.lang.String</tt>,
	 * without including the null terminator. If no null terminator is found,
	 * returns <tt>null</tt>.
	 * 
	 * @param vaddr the starting virtual address of the null-terminated string.
	 * @param maxLength the maximum number of characters in the string, not
	 * including the null terminator.
	 * @return the string read, or <tt>null</tt> if no null terminator was
	 * found.
	 */
	public String readVirtualMemoryString(int vaddr, int maxLength) {
		Lib.assertTrue(maxLength >= 0);

		byte[] bytes = new byte[maxLength + 1];

		int bytesRead = readVirtualMemory(vaddr, bytes);

		for (int length = 0; length < bytesRead; length++) {
			if (bytes[length] == 0)
				return new String(bytes, 0, length);
		}

		return null;
	}

	/**
	 * Transfer data from this process's virtual memory to all of the specified
	 * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
	 * 
	 * @param vaddr the first byte of virtual memory to read.
	 * @param data the array where the data will be stored.
	 * @return the number of bytes successfully transferred.
	 */
	public int readVirtualMemory(int vaddr, byte[] data) {
		return readVirtualMemory(vaddr, data, 0, data.length);
	}

	/**
	 * Transfer data from this process's virtual memory to the specified array.
	 * This method handles address translation details. This method must
	 * <i>not</i> destroy the current process if an error occurs, but instead
	 * should return the number of bytes successfully copied (or zero if no data
	 * could be copied).
	 * 
	 * @param vaddr the first byte of virtual memory to read.
	 * @param data the array where the data will be stored.
	 * @param offset the first byte to write in the array.
	 * @param length the number of bytes to transfer from virtual memory to the
	 * array.
	 * @return the number of bytes successfully transferred.
	 */
	public int readVirtualMemory(int vaddr, byte[] data, int offset, int length) {
		Lib.assertTrue(offset >= 0 && length >= 0
				&& offset + length <= data.length);

		byte[] memory = Machine.processor().getMemory();

		int vpn = Processor.pageFromAddress(vaddr);
		int pageOffset = Processor.offsetFromAddress(vaddr);
		int ppn = pageTable[vpn].ppn;
		int pAddr = ppn * pageSize + pageOffset;

		if (pAddr < 0 || pAddr >= memory.length)
			return 0;

		int amount = Math.min(length, pageSize - pageOffset);
		System.arraycopy(memory, pAddr, data, offset, amount);

		return amount;
	}

	/**
	 * Transfer all data from the specified array to this process's virtual
	 * memory. Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
	 * 
	 * @param vaddr the first byte of virtual memory to write.
	 * @param data the array containing the data to transfer.
	 * @return the number of bytes successfully transferred.
	 */
	public int writeVirtualMemory(int vaddr, byte[] data) {
		return writeVirtualMemory(vaddr, data, 0, data.length);
	}

	/**
	 * Transfer data from the specified array to this process's virtual memory.
	 * This method handles address translation details. This method must
	 * <i>not</i> destroy the current process if an error occurs, but instead
	 * should return the number of bytes successfully copied (or zero if no data
	 * could be copied).
	 * 
	 * @param vaddr the first byte of virtual memory to write.
	 * @param data the array containing the data to transfer.
	 * @param offset the first byte to transfer from the array.
	 * @param length the number of bytes to transfer from the array to virtual
	 * memory.
	 * @return the number of bytes successfully transferred.
	 */
	public int writeVirtualMemory(int vaddr, byte[] data, int offset, int length) {
		Lib.assertTrue(offset >= 0 && length >= 0
				&& offset + length <= data.length);

		byte[] memory = Machine.processor().getMemory();

		int vpn = Processor.pageFromAddress(vaddr);
		int pageOffset = Processor.offsetFromAddress(vaddr);
		int ppn = pageTable[vpn].ppn;
		int pAddr = ppn * pageSize + pageOffset;

		// for now, just assume that virtual addresses equal physical addresses
		if (pAddr < 0 || pAddr >= memory.length)
			return 0;
		if (pageTable[vpn].readOnly)
			return 0;

		int amount = Math.min(length, pageSize - offset);
		System.arraycopy(data, offset, memory, pAddr, amount);

		return amount;
	}

	/**
	 * Load the executable with the specified name into this process, and
	 * prepare to pass it the specified arguments. Opens the executable, reads
	 * its header information, and copies sections and arguments into this
	 * process's virtual memory.
	 * 
	 * @param name the name of the file containing the executable.
	 * @param args the arguments to pass to the executable.
	 * @return <tt>true</tt> if the executable was successfully loaded.
	 */
	private boolean load(String name, String[] args) {
		Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");

		OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
		if (executable == null) {
			Lib.debug(dbgProcess, "\topen failed");
			return false;
		}

		try {
			coff = new Coff(executable);
		}
		catch (EOFException e) {
			executable.close();
			Lib.debug(dbgProcess, "\tcoff load failed");
			return false;
		}

		// make sure the sections are contiguous and start at page 0
		numPages = 0;
		for (int s = 0; s < coff.getNumSections(); s++) {
			CoffSection section = coff.getSection(s);
			if (section.getFirstVPN() != numPages) {
				coff.close();
				Lib.debug(dbgProcess, "\tfragmented executable");
				return false;
			}
			numPages += section.getLength();
		}

		// make sure the argv array will fit in one page
		byte[][] argv = new byte[args.length][];
		int argsSize = 0;
		for (int i = 0; i < args.length; i++) {
			argv[i] = args[i].getBytes();
			// 4 bytes for argv[] pointer; then string plus one for null byte
			argsSize += 4 + argv[i].length + 1;
		}
		if (argsSize > pageSize) {
			coff.close();
			Lib.debug(dbgProcess, "\targuments too long");
			return false;
		}

		// program counter initially points at the program entry point
		initialPC = coff.getEntryPoint();

		// next comes the stack; stack pointer initially points to top of it
		numPages += stackPages;
		initialSP = numPages * pageSize;

		// and finally reserve 1 page for arguments
		numPages++;

		if (!loadSections())
			return false;

		// store arguments in last page
		int entryOffset = (numPages - 1) * pageSize;
		int stringOffset = entryOffset + args.length * 4;

		this.argc = args.length;
		this.argv = entryOffset;

		for (int i = 0; i < argv.length; i++) {
			byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
			Lib.assertTrue(writeVirtualMemory(entryOffset, stringOffsetBytes) == 4);
			entryOffset += 4;
			Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) == argv[i].length);
			stringOffset += argv[i].length;
			Lib.assertTrue(writeVirtualMemory(stringOffset, new byte[] { 0 }) == 1);
			stringOffset += 1;
		}

		return true;
	}

	/**
	 * Allocates memory for this process, and loads the COFF sections into
	 * memory. If this returns successfully, the process will definitely be run
	 * (this is the last step in process initialization that can fail).
	 * 
	 * @return <tt>true</tt> if the sections were successfully loaded.
	 */
	protected boolean loadSections() {
		if (numPages > Machine.processor().getNumPhysPages()) {
			coff.close();
			Lib.debug(dbgProcess, "\tinsufficient physical memory");
			return false;
		}

		if (numPages > UserKernel.pageTable.getNumFreePages()) {
			coff.close();
			Lib.debug(dbgProcess, "\tinsufficient physical memory");
			return false;
		}

		// allocate the page table with numPages entries
		pageTable = new TranslationEntry[numPages];

		// load sections
		for (int s = 0; s < coff.getNumSections(); s++) {
			CoffSection section = coff.getSection(s);

			Lib.debug(dbgProcess, "\tinitializing " + section.getName()
					+ " section (" + section.getLength() + " pages)");

			for (int i = 0; i < section.getLength(); i++) {
				int vpn = section.getFirstVPN() + i;

				int ppn = UserKernel.pageTable.getFreePage();
				if (ppn == -1)
					return false;

				section.loadPage(i, ppn);

				pageTable[vpn] = new TranslationEntry(vpn, ppn, true, section.isReadOnly(), true, false);
			}
		}

		// allocate pages for stacks and arguments
		for (int i = numPages - 1 - stackPages; i < numPages; i++) {
			int ppn = UserKernel.pageTable.getFreePage();
			if (ppn == -1)
				return false;
			pageTable[i] = new TranslationEntry(i, ppn, true, false, true, false);
		}

		return true;
	}

	/**
	 * Release any resources allocated by <tt>loadSections()</tt>.
	 */
	protected void unloadSections() {
		for (int i = 0; i < numPages; i++) {
			int ppn = pageTable[i].ppn;
			UserKernel.pageTable.addFreePage(ppn);
			pageTable[i] = null;
		}
		pageTable = null;
	}

	/**
	 * Initialize the processor's registers in preparation for running the
	 * program loaded into this process. Set the PC register to point at the
	 * start function, set the stack pointer register to point at the top of the
	 * stack, set the A0 and A1 registers to argc and argv, respectively, and
	 * initialize all other registers to 0.
	 */
	public void initRegisters() {
		Processor processor = Machine.processor();

		// by default, everything's 0
		for (int i = 0; i < processor.numUserRegisters; i++)
			processor.writeRegister(i, 0);

		// initialize PC and SP according
		processor.writeRegister(Processor.regPC, initialPC);
		processor.writeRegister(Processor.regSP, initialSP);

		// initialize the first two argument registers to argc and argv
		processor.writeRegister(Processor.regA0, argc);
		processor.writeRegister(Processor.regA1, argv);
	}

	/**
	 * Handle the halt() system call.
	 */
	private int handleHalt() {
		if (pID != 1)
			return -1;

		Machine.halt();

		Lib.assertNotReached("Machine.halt() did not halt machine!");
		return 0;
	}

	private void handleExit(int status) {
		fdTable.clean();
		unloadSections();
		coff.close();

		for (int childPID : childrenExitStatus.keySet()) {
			if (UserKernel.processTable.stillRunning(childPID))
				UserKernel.processTable.getProcess(childPID).parentPID = -1;
		}

		UserProcess parentProcess = UserKernel.processTable.getProcess(parentPID);
		if (parentProcess != null) {
			parentProcess.joinLock.acquire();
			parentProcess.childrenExitStatus.put(pID, status);
			parentProcess.joinCondition.wake();
			parentProcess.joinLock.release();
		}

//		System.out.println("exit status" + pID + status);

		UserKernel.processTable.removeProcess(pID);
		if (UserKernel.processTable.getProcessNum() == 0) {
			Kernel.kernel.terminate();
		}
		UThread.finish();
	}

	private int handleExec(int pFile, int argc, int ppArgv) {
		String fileName = readVirtualMemoryString(pFile, maxStrLen);
		if (fileName == null || fileName.length() < 5
				|| !fileName.substring(fileName.length() - 5).equals(".coff"))
			return -1;

		String[] args = new String[argc];
		for (int i = 0; i < argc; i++) {
			byte[] buffer = new byte[4];
			if (readVirtualMemory(ppArgv + 4 * i, buffer) != 4)
				return -1;
			int pArgv = Lib.bytesToInt(buffer, 0);
			args[i] = readVirtualMemoryString(pArgv, maxStrLen);
			if (args[i] == null)
				return -1;
		}

		UserProcess childProcess = newUserProcess();
		if (!childProcess.execute(fileName, args)) {
			UserKernel.processTable.removeProcess(childProcess.pID);
			return -1;
		}

		childrenExitStatus.put(childProcess.pID, null);
		childProcess.parentPID = this.pID;

		return childProcess.pID;
	}

	private int handleJoin(int childPID, int pStatus) {
		if (!childrenExitStatus.containsKey(childPID))
			return -1;

//		UserProcess childProcess = UserKernel.processTable.getProcess(childPID);
//		if (childProcess == null)
//			return -1;

		joinLock.acquire();
		Integer exitStatus = childrenExitStatus.get(childPID);
		if (exitStatus == null) {
			joinCondition.sleep();
			exitStatus = childrenExitStatus.get(childPID);
		}
		joinLock.release();

		byte[] buffer = Lib.bytesFromInt(exitStatus);
		if (writeVirtualMemory(pStatus, buffer) != 4)
			return -1;

		return exitStatus == 0 ? 1 : 0;
	}

	private int handleCreate(int a0) {
		String fileName = readVirtualMemoryString(a0, maxStrLen);
		if (fileName == null)
			return -1;
		FileDescriptor fd = fdTable.create(fileName);
		if (fd != null && fd.isValid()) {
//			UserKernel.fileTable.increFileRefCount(fileName, pID);
			return fd.getPosition();
		}
		return -1;
	}

	private int handleOpen(int a0) {
		String fileName = readVirtualMemoryString(a0, maxStrLen);
		FileDescriptor fd = fdTable.open(fileName);
		if (fd != null && fd.isValid()) {
//			UserKernel.fileTable.increFileRefCount(fileName, pID);
			return fd.getPosition();
		}
		return -1;
	}

	private int handleRead(int fileDescriptor, int bufferVAddr, int count) {
		FileDescriptor fd = fdTable.get(fileDescriptor);
		if (fd == null || !fd.isValid())
			return -1;
		OpenFile file = fd.getFile();

		final int bufferSize = pageSize;
		byte[] dummyBuffer = new byte[bufferSize];

		int pos = 0;
		while (pos < count && pos < file.length()) {
			if (fd.getPosition() == STDIN) {
				if (file.read(dummyBuffer, 0, bufferSize) == -1)
					return -1;
			}
			else {
				if (file.read(pos, dummyBuffer, 0, bufferSize) == -1)
					return -1;
			}
			int amount = writeVirtualMemory(bufferVAddr + pos, dummyBuffer, 0, Math.min(bufferSize, count - pos));
			if (amount == 0)
				return -1;
			pos += amount;
		}
		return 0;
	}

	private int handleWrite(int fileDescriptor, int bufferVAddr, int count) {
		FileDescriptor fd = fdTable.get(fileDescriptor);
		if (fd == null || !fd.isValid())
			return -1;
		OpenFile file = fd.getFile();

		final int bufferSize = pageSize;
		byte[] dummyBuffer = new byte[bufferSize];

		int pos = 0;
		while(pos < count) {
			int amount = readVirtualMemory(bufferVAddr + pos, dummyBuffer, 0, Math.min(bufferSize, count - pos));
			if (amount == 0)
				return -1;

			if (fd.getPosition() > STDOUT) {
				if (file.write(pos, dummyBuffer, 0, amount) == -1)
					return -1;
			}
			else {
				if (file.write(dummyBuffer, 0, amount) == -1)
					return -1;
			}

			pos += amount;
		}

		return 0;
	}

	private int handleClose(int fd) {
		if (fdTable.close(fd)) {
			return 0;
		}
		return -1;
	}

	private int handleUnlink(int vAddr) {
		String fileName = readVirtualMemoryString(vAddr, maxStrLen);
		if (fdTable.unlink(fileName))
			return 0;
		return -1;
	}

	private static final int syscallHalt = 0, syscallExit = 1, syscallExec = 2,
			syscallJoin = 3, syscallCreate = 4, syscallOpen = 5,
			syscallRead = 6, syscallWrite = 7, syscallClose = 8,
			syscallUnlink = 9;

	/**
	 * Handle a syscall exception. Called by <tt>handleException()</tt>. The
	 * <i>syscall</i> argument identifies which syscall the user executed:
	 * 
	 * <table>
	 * <tr>
	 * <td>syscall#</td>
	 * <td>syscall prototype</td>
	 * </tr>
	 * <tr>
	 * <td>0</td>
	 * <td><tt>void halt();</tt></td>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td><tt>void exit(int status);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>2</td>
	 * <td><tt>int  exec(char *name, int argc, char **argv);
	 * 								</tt></td>
	 * </tr>
	 * <tr>
	 * <td>3</td>
	 * <td><tt>int  join(int pid, int *status);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>4</td>
	 * <td><tt>int  creat(char *name);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>5</td>
	 * <td><tt>int  open(char *name);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>6</td>
	 * <td><tt>int  read(int fd, char *buffer, int size);
	 * 								</tt></td>
	 * </tr>
	 * <tr>
	 * <td>7</td>
	 * <td><tt>int  write(int fd, char *buffer, int size);
	 * 								</tt></td>
	 * </tr>
	 * <tr>
	 * <td>8</td>
	 * <td><tt>int  close(int fd);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>9</td>
	 * <td><tt>int  unlink(char *name);</tt></td>
	 * </tr>
	 * </table>
	 * 
	 * @param syscall the syscall number.
	 * @param a0 the first syscall argument.
	 * @param a1 the second syscall argument.
	 * @param a2 the third syscall argument.
	 * @param a3 the fourth syscall argument.
	 * @return the value to be returned to the user.
	 */
	public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
		switch (syscall) {
			case syscallHalt:
				return handleHalt();
			case syscallExit:
				handleExit(a0);
				Lib.assertNotReached("Not exited");
				return 0;
			case syscallExec:
				return handleExec(a0, a1, a2);
			case syscallJoin:
				return handleJoin(a0, a1);
			case syscallCreate:
				return handleCreate(a0);
			case syscallOpen:
				return handleOpen(a0);
			case syscallRead:
				return handleRead(a0, a1, a2);
			case syscallWrite:
				return handleWrite(a0, a1, a2);
			case syscallClose:
				return handleClose(a0);
			case syscallUnlink:
				return handleUnlink(a0);


		default:
			Lib.debug(dbgProcess, "Unknown syscall " + syscall);
			Lib.assertNotReached("Unknown system call!");
		}
		return 0;
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
			case Processor.exceptionSyscall:
				int result = handleSyscall(processor.readRegister(Processor.regV0),
						processor.readRegister(Processor.regA0),
						processor.readRegister(Processor.regA1),
						processor.readRegister(Processor.regA2),
						processor.readRegister(Processor.regA3));
				processor.writeRegister(Processor.regV0, result);
				processor.advancePC();
				break;

		default:
			Lib.debug(dbgProcess, "Unexpected exception: "
					+ Processor.exceptionNames[cause]);
			handleExit(-1);
			Lib.assertNotReached("Unexpected exception");
		}
	}


	public class FileDescriptor {
		public FileDescriptor() {
			clean();
		}

		public FileDescriptor(String name, int pos, boolean create) {
			if (pos == 0)
				file = UserKernel.console.openForReading();
			else if (pos == 1)
				file = UserKernel.console.openForWriting();
			else
				file = UserKernel.fileSystem.open(name, create);

			if (file == null)
				clean();
			else {
				fileName = name;
				position = pos;
				valid = true;
			}
		}

		public void clean() {
			valid = false;
			fileName = "";
			if (file != null) {
				file.close();
				file = null;
			}
			position = -1;
		}

		public boolean isValid() {
			return valid;
		}

		public OpenFile getFile() {
			return file;
		}

		public int getPosition() {
			return position;
		}

		public String getName() {
			return fileName;
		}

		private String fileName;
		private OpenFile file;
		private int position;
		private boolean valid;
	}

	private class FileDescriptorTable {
		public FileDescriptorTable() {
			table[STDIN] = new FileDescriptor("stdin", STDIN, false);
			table[STDOUT] = new FileDescriptor("stdout", STDOUT, false);
			for (int i = 2; i < maxFileCount; i++)
				table[i] = new FileDescriptor();
		}

		public boolean isFull() {
			return count == maxFileCount;
		}

		public FileDescriptor create(String fileName) {
			if (isFull())
				return null;
			for (int i = 2; i < maxFileCount; i++) {
				if (!table[i].isValid()) {
					FileDescriptor fd = new FileDescriptor(fileName, i, true);
					if (fd.isValid()) {
						table[i] = fd;
						count++;
						UserKernel.fileTable.increFileRefCount(fileName);
						return table[i];
					}
					else
						return null;
				}
			}
			return null;
		}

		public FileDescriptor open(String fileName) {
			if (isFull())
				return null;
			for (int i = 2; i < maxFileCount; i++) {
				if (!table[i].isValid()) {
					FileDescriptor fd = new FileDescriptor(fileName, i, false);
					if (fd.isValid()) {
						table[i] = fd;
						count++;
						UserKernel.fileTable.increFileRefCount(fileName);
						return table[i];
					}
					else
						return null;
				}
			}
			return null;
		}

		public FileDescriptor get(int pos) {
			if (pos < 0 || pos >= maxFileCount)
				return null;
			return table[pos];
		}

		public boolean unlink(String fileName) {
			for (int i = 2; i < maxFileCount; i++) {
				if (table[i].getName().equals(fileName)) {
					UserKernel.fileTable.decreFileRefCount(fileName, true);
				}
			}
			return UserKernel.fileSystem.remove(fileName);
		}

		public boolean close(int pos) {
			if (pos < 0 || pos >= maxFileCount)
				return false;
			UserKernel.fileTable.decreFileRefCount(table[pos].getName(), false);
			table[pos].clean();
			count--;
			return true;
		}

		public void clean() {
			for (int i = 0; i < table.length; i++) {
				UserKernel.fileTable.decreFileRefCount(table[i].fileName, false);
				table[i].clean();
				table[i] = null;
			}
			count = 2;
		}

		private final int maxFileCount = 16;
		private FileDescriptor[] table = new FileDescriptor[maxFileCount];
		private int count = 2;
	}

	/** The program being run by this process. */
	protected Coff coff;

	/** This process's page table. */
	protected TranslationEntry[] pageTable;

	/** The number of contiguous pages occupied by the program. */
	protected int numPages;

	/** The number of pages in the program's stack. */
	protected final int stackPages = 8;

	private int initialPC, initialSP;

	private int argc, argv;

	private FileDescriptorTable fdTable;

	private int pID;

	private int parentPID = -1;

	private Map<Integer, Integer> childrenExitStatus = new HashMap<>();

	private Lock joinLock = new Lock();

	private Condition joinCondition = new Condition(joinLock);

	private static Lock pIDLock = new Lock();

	private static int currPID = 1;

	private static final int pageSize = Processor.pageSize;

	private static final char dbgProcess = 'a';

	private static final int maxStrLen = 256;

	private static final int STDIN = 0;

	private static final int STDOUT = 1;
}
