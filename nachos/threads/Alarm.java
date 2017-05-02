package nachos.threads;

import nachos.machine.*;

import javax.crypto.Mac;
import java.util.TreeSet;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 * 
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
		pending = new TreeSet<>();
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
		long currTime = Machine.timer().getTime();
		boolean intStatus = Machine.interrupt().disable();
		while (!pending.isEmpty() &&
				pending.first().wakeTime <= currTime) {
			PendingThread next = pending.first();
			pending.remove(next);

			Lib.assertTrue(next.wakeTime <= currTime);
			next.thread.ready();
		}
		Machine.interrupt().restore(intStatus);
		KThread.currentThread().yield();
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 * 
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 * 
	 * @param x the minimum number of clock ticks to wait.
	 * 
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		// for now, cheat just to get something working (busy waiting is bad)
		long wakeTime = Machine.timer().getTime() + x;
//		while (wakeTime > Machine.timer().getTime())
//			KThread.yield();
		boolean intStatus = Machine.interrupt().disable();
		PendingThread thread = new PendingThread(wakeTime, KThread.currentThread());
		pending.add(thread);
		KThread.sleep();
		Machine.interrupt().restore(intStatus);
	}

	private class PendingThread implements Comparable {
		PendingThread(long wakeTime, KThread thread) {
			this.wakeTime = wakeTime;
			this.thread = thread;
			this.id = numPendingThreadsCreated++;
		}

		public int compareTo(Object o) {
			PendingThread toOccur = (PendingThread) o;

			// can't return 0 for unequal objects, so check all fields
			if (wakeTime < toOccur.wakeTime)
				return -1;
			else if (wakeTime > toOccur.wakeTime)
				return 1;
			else if (id < toOccur.id)
				return -1;
			else if (id > toOccur.id)
				return 1;
			else
				return 0;
		}

		private long wakeTime;
		private KThread thread;
		private long id;
	}

	private class SelfTest {
		SelfTest() {
			case1();
			System.out.println("Alarm test finished.\n");
		}

		private void case1() {
			numTestThreads = 3;
			notFinished = numTestThreads;
			long interval = 100000;
			for (int i = 0; i < numTestThreads; i++) {
				new KThread(new AlarmTest(i, interval)).setName("Alarm Test" + (i + 1)).fork();
				for (int j = 0; j < 10000; j++) {
					boolean intStatus = Machine.interrupt().disable();
					Machine.interrupt().restore(intStatus);
				}
			}

			while (notFinished > 0) {
				boolean intStatus = Machine.interrupt().disable();
				Machine.interrupt().restore(intStatus);
			}
		}

		private class AlarmTest implements Runnable {
			AlarmTest(int which, long interval) {
				this.which = which;
				this.interval = interval;
			}

			public void run() {
				long prevTime = Machine.timer().getTime();
				waitUntil(interval);
				long finishTime = Machine.timer().getTime();
				System.out.println(which + " :Set interval: " + interval + ", actual interval: "
						+ (finishTime - prevTime) + ", start time: " + prevTime);
				Lib.assertTrue((finishTime - prevTime) / interval - 1 < 0.1);
				notFinished--;
			}

			private int which;
			private long interval;
		}

		private int numTestThreads;
		private int notFinished;
	}

	public void selfTest() {
		new SelfTest();
	}

	private long numPendingThreadsCreated = 0;

	private TreeSet<PendingThread> pending;
}
