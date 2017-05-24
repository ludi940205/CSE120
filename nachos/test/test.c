#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

void loopWait(int loopNum) {
	int i;
	for (i = 0; i < loopNum; i++);
}

void exceptionTest() {
	char* argv[3][1] = {"0", "1", "2"};
	int pid[10], i, status, joinRet;

	for (i = 0; i < 3; i++) {
		pid[i] = exec("testFailThread.coff", 1, argv[i]);
		printf("Exception test PID: %d\n", pid[i]);
		joinRet = join(pid[i], &status);
		printf("Exit status: %d\n", status);
		printf("Join return value: %d\n", joinRet);
	}
}

void fileSystemBasicTest() {
	printf("Hello world!\n");

	int newFile, oldFile;
	char buffer[16];

	printf("Creating file\n");
	newFile = creat("TestFile.txt");

	printf("Writing file\n");
	write(newFile, "Hello world!!!\n", 15);

	printf("Closing file\n");
	close(newFile);

	printf("Opening file\n");
	oldFile = open("TestFile.txt");

	printf("Reading file\n");
	read(oldFile, buffer, 16);

	printf(buffer);

	printf("Closing file\n");
	close(oldFile);
}

void fileSystemPressureTest() {
	char buffer[5000];
    int file, newfile;
    int length, i, flag;
    int files[20];
    char name[8] = "f00.txt";

    memset(buffer, 48, 5000);
    file = creat("testFile.txt");
    write(file, buffer, 5000);
    close(file);

    file = open("syscall.h");
    newfile = creat("syscall_copy.h");
    length = read(file, buffer, 5000);
    write(newfile, buffer, 5000);
    close(file);
    close(newfile);

    printf("finished first part\n");

    for (i = 0; i < 20; i++) {
        name[2] = '0' + ((i + 1) % 10);
        name[1] = i < 9 ? '0' : '1';
        files[i] = creat(name);
        printf("creating file %s, %d\n", name, files[i]);
    }

    for (i = 0; i < 6; i++) {
        flag = close(files[i]);
        printf("closing file %d, %d\n", i + 1, flag);
    }

    for (i = 14; i < 20; i++) {
        name[2] = '0' + (i + 1) % 10;
        name[1] = i < 9 ? '0' : '1';
        files[i] = creat(name);
        printf("creating file %s, %d\n", name, files[i]);
    }

    for (i = 0; i < 20; i++) {
        flag = close(files[i]);
        printf("closing file %d, %d\n", i + 1, flag);
    }
}

void fileSystemFailTest() {
	char buffer[5000], largeBuffer[20000];
    int file, newfile;
    int length;
    int i;

    file = open("not_existed_file");
    if (file == -1)
        printf("file does not exist\n");

    length = read(file, buffer, 100);
    if (length == -1)
        printf("read failed\n");

    i = write(file, buffer, 100);
    if (i == -1)
        printf("write failed\n");

    newFile = open("syscall.h");
    if (newFile == -1)
        printf("file does not exist\n");

    length = read(newFile, largeBuffer, 20000);
    if (length == -1)
        printf("read failed\n");
    else
        printf("read %d bytes", length);

    close(file);
    printf("end\n");
}

void syscallBasicTest() {
	int childPID[10], i, joinRet, status;
    char* name[10][1] = {"T0", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9",};

    printf("Inside main\n");

    for (i = 0; i < 10; i++) {
        printf("Creating %s\n", name[i][0]);
        childPID[i] = exec("testLoopThread.coff", 1, name[i]);
        printf("Child PID: %d\n", childPID[i]);
    }

   for (i = 0; i < 10; i++) {
        joinRet = join(childPID[i], &status);
        printf("Exit status: %d\n", status);
        printf("Join return value: %d\n", joinRet);
   }

    printf("Finished main\n");
}

void testRoute(int id) {
	switch (id) {
		case 0:
			/* exception test */
			exceptionTest();
			break;
		case 1:
			fileSystemBasicTest();
			break;
		case 2:
			fileSystemPressureTest();
			break;
		case 3:
			fileSystemFailTest();
			break;
		case 4:
			syscallBasicTest();
			break;
	}
}

int main() {
	int i;
	for (i = 0; i < 5; i++)
		testRoute(i);

	return 0;
}
