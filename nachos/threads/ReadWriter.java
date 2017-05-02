package nachos.threads;

import java.util.ArrayList;
import java.util.List;

import nachos.machine.*;

public final class ReadWriter {
    ReadWriter() {
    }

    private static class Reader implements Runnable {
        Reader(int num) {
            this.num = num;
        }

        public void run() {
            for (int i = 0; i < this.num; i++) {
                mutex.acquire();
                while (count == 0)
                    full.sleep();
                count--;
//				System.out.println(buffer[read_ptr]);
                newBuffer.add(buffer[read_ptr]);
                read_ptr = (read_ptr + 1) % capacity;
                empty.wake();
                mutex.release();
            }
            mutex.acquire();
            readNotFinished--;
            mutex.release();
        }

        private int num;
    }

    private static class Writer implements Runnable {
        Writer(int num) {
            this.num = num;
        }

        public void run() {
            for (int i = 0; i < this.num; i++) {
                mutex.acquire();
                while (count == capacity)
                    empty.sleep();
                count++;
                currCount++;
                buffer[write_ptr] = currCount;
                write_ptr = (write_ptr + 1) % capacity;
                full.wake();
                mutex.release();
            }
            mutex.acquire();
            writeNotFinished--;
            mutex.release();
        }

        private int num;
    }

    public static void readWriterTest() {
        for (int i = 0; i < writerCount; i++)
            new KThread(new Writer(writeNum)).setName("writer" + (i + 1)).fork();
        for (int i = 0; i < readerCount; i++)
            new KThread(new Reader(readNum)).setName("reader" + (i + 1)).fork();

        while (readNotFinished > 0 || writeNotFinished > 0) {
//			System.out.println(currCount);
            KThread.yield();
        }
        for (int i = 0; i < readNum; i++)
            Lib.assertTrue((i + 1) == newBuffer.get(i));
    }

    private static final int capacity = 10;
    private static final int readNum = 1000;
    private static final int writeNum = 1000;
    private static int currCount = 0;
    private static int writerCount = 2, readerCount = 2;
    private static int[] buffer = new int[capacity];
    private static int write_ptr = 0;
    private static int read_ptr = 0;
    private static int count = 0;
    private static Lock mutex = new Lock();
    private static Condition2 empty = new Condition2(mutex);
    private static Condition2 full = new Condition2(mutex);
    //	private static Condition empty = new Condition(mutex);
//	private static Condition full = new Condition(mutex);
    private static List<Integer> newBuffer = new ArrayList<Integer>();
    private static int readNotFinished = readerCount, writeNotFinished = writerCount;
}
