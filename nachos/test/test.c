#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

void loopWait(int loopNum) {
	int i;
	for (i = 0; i < loopNum; i++);
}

void exceptionTest() {
	char argv[1][10] = {"0"};
	int pid[10], i, *status;

	for (i = 0; i < 3; i++) {
	    argv[0][0] = '0' + i;
		pid[i] = exec("testFailThread.coff", 1, argv);
		printf("Exception test PID: %d\n", pid[i]);
		join(pid[i], status);
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
	char buffer[5000];
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

    close(file);
    printf("end\n");
}

void syscallBasicTest() {
	int childPID[10], i;
    int* status;
    char name[1][10] = {"T0"};

    printf("Inside main\n");

    for (i = 0; i < 10; i++) {
        name[0][1] = '0' + i;
        printf("Creating %s\n", name[0]);
        childPID[i] = exec("testLoopThread.coff", 1, name);
        printf("Child PID: %d\n", childPID[i]);
    }

   for (i = 0; i < 10; i++)
       join(childPID[i], status);

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
