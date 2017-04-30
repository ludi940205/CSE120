package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>, and multiple
 * threads can be waiting to <i>listen</i>. But there should never be a time
 * when both a speaker and a listener are waiting, because the two threads can
 * be paired off at this point.
 */
public class Communicator {
	/**
	 * Allocate a new communicator.
	 */
	public Communicator() {
		mutex = new Lock();
		listener = new Condition(mutex);
		activeSpeaker = new Condition(mutex);
		waitSpeaker = new Condition(mutex);
	}

	/**
	 * Wait for a thread to listen through this communicator, and then transfer
	 * <i>word</i> to the listener.
	 * 
	 * <p>
	 * Does not return until this thread is paired up with a listening thread.
	 * Exactly one listener should receive <i>word</i>.
	 * 
	 * @param word the integer to transfer.
	 */
	public void speak(int word) {
		mutex.acquire();
		while (speaking) {
			waitSpeaker.sleep();
		}
//		System.out.println("Inside speaker");
		speaking = true;
		message = word;
		listener.wake();
		activeSpeaker.sleep();
		mutex.release();
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return the
	 * <i>word</i> that thread passed to <tt>speak()</tt>.
	 * 
	 * @return the integer transferred.
	 */
	public int listen() {
		mutex.acquire();
		while (!speaking) {
			listener.sleep();
		}
		int ret = message;
		activeSpeaker.wake();
		waitSpeaker.wake();
		speaking = false;
		mutex.release();
		return ret;
	}

	private static Communicator communicator = new Communicator();
	private static int num = 1;
	private static int notFinished = 2 * num;

	private static class Speaker implements Runnable {
		Speaker(int which) {
			this.which = which;
		}

		public void run() {
			communicator.speak(which);
			System.out.println("Speaker " + which + " speaks " + which);
			notFinished--;
		}

		private int which;
	}

	private static class Listener implements Runnable {
		Listener(int which) {
			this. which = which;
		}

		public void run() {
			int receive = communicator.listen();
			System.out.println("Listener " + which + " receive " + receive);
			notFinished--;
		}

		private int which;
	}

	public static void selfTest() {
		int num1 = num / 2;
		for (int i = 0; i < num1; i++) {
			new KThread(new Speaker(i)).setName("speaker " + i).fork();
		}
		for (int i = 0; i < num; i++) {
			new KThread(new Listener(i)).setName("listener " + i).fork();
		}
		for (int i = 0; i < num - num1; i++) {
			new KThread(new Speaker(num1 + i)).setName("speaker " + i).fork();
		}
		while (notFinished > 0)
			KThread.yield();
	}

	private Lock mutex;

	private Condition listener, activeSpeaker, waitSpeaker;

	private boolean speaking = false;

//	private int speakers = 0, listeners = 0, messages = 0;

	private int message;
}
