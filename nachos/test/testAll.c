#include "stdio.h"

#define NULL        0
#define MAXPROCESS  9

void assertMsg(int arg, char* msg) {
    if (arg == 0) {
        printf("%s\n", msg);
        exit(-1);
    }
}

int tryOpen(char* fileName) {
    int fd;

    fd = open(fileName);
    close(fd);
    if (fd == -1)
        return 0;
    else
        return 1;
}

int forkCreat(char* fileName) {
    char* executable;
    char* argv[2];
    int pid, argc;

    executable = "testCreat.coff";
    argc = 2;
    argv[0] = executable;
    argv[1] = fileName;

    pid = exec(executable, argc, argv);

    return pid;
}

int forkOpen(char* fileName) {
    char* executable;
    char* argv[2];
    int pid, argc;

    executable = "testOpen.coff";
    argc = 2;
    argv[0] = executable;
    argv[1] = fileName;

    pid = exec(executable, argc, argv);

    return pid;
}

void testExit() {
    char* executable;
    char* argv[2];
    int pid, argc;

    executable = "testExit.coff";
    argv[0] = executable;
    argv[1] = NULL;
    argc = 1;
    pid = exec(executable, argc, argv);
    assertMsg(pid != -1, "EXIT TEST: Unable to exec exittest.coff\n");

    executable = "testLoopThread.coff";
    argv[0] = executable;
    argv[1] = NULL;
    argc = 1;
    pid = exec(executable, argc, argv);
    assertMsg(pid != -1, "EXIT TEST: Unable to exec exittest.coff\n");
    printf("EXIT TEST SUCCESS\n");
}

void testPID() {
    char* executable;
    char* argv[3];
    int pid[MAXPROCESS], argc, i, j, exitstatus;

    executable = "cp.coff";
    argv[0] = executable;
    argv[1] = "syscall.h";
    argv[2] = "syscall_copy.h";
    argc = 3;

    for (i = 0; i <  MAXPROCESS; i++) {
        pid[i] = exec(executable, argc, argv);
        join(pid[i], &exitstatus);
        assertMsg(pid[i] != -1, "PID TEST: Unable to exec cp.coff\n");
    
        for (j = 0; j < i; ++j)
            assertMsg(pid[j] == -1 || pid[j] != pid[i], "PID TEST: pid equals");
    }

    printf("PID TEST SUCCESS\n");
}

void testJoin() {
    char* executable;
    char* argv[2];
    int pid, argc, exitstatus, joinRet, i;

    executable = "testExit.coff";
    argv[0] = executable;
    argv[1] = NULL;
    argc = 1;

    pid = exec(executable, argc, argv);
    assertMsg(pid > 1, "JOIN TEST: unable to exec exittest.coff\n");
    joinRet = join(pid, &exitstatus);
    assertMsg(joinRet == 1, "JOIN TEST: Join return value not right\n");
    assertMsg(exitstatus == 0, "JOIN TEST: exit status not right\n");
    
    joinRet = join(pid, &exitstatus);
    assertMsg(joinRet == 1, "JOIN TEST: Join twice return value not right\n");
    assertMsg(exitstatus == 0, "JOIN TEST: join twice exit status not right\n");

    joinRet = join(0, &exitstatus);
    assertMsg(joinRet == -1, "JOIN TEST: join to a non child not right\n");

    joinRet = join(1, &exitstatus);
    assertMsg(joinRet == -1, "JOIN TEST: join to a myself not right\n");

    executable = "testException.coff";
    argv[0] = executable;
    argv[1] = NULL;
    argc = 1; 

    for (i = 0; i < 3; i++) {
        argv[1] = '0' + i;
        pid = exec(executable, argc, argv);
        assertMsg(pid > 1, "JOIN TEST: unable to exec testException.coff\n");
        joinRet = join(pid, &exitstatus);
        assertMsg(joinRet == 0, "JOIN TEST: Join a failed thread, return value not right\n");
        assertMsg(exitstatus != 0, "JOIN TEST: Join a failed thread, exit status not right\n");
    }

    printf("JOIN TEST SUCCESS\n");
}

void testExec() {
    char* executable;
    char* argv[2];
    int pid, argc;

    executable = "inexistent.coff";
    argv[0] = executable;
    argv[1] = NULL;
    argc = 1; 
    pid = exec(executable, argc, argv);
    assertMsg(pid == -1, "EXEC TEST: invoke exec with nonextent executable success\n");

    executable = "testExit.coff";
    argv[0] = executable;
    argv[1] = NULL;
    argc = 100; 
    pid = exec(executable, argc, argv);
    assertMsg(pid == -1, "EXEC TEST: invoke exec with unmatched argc success\n");

    printf("EXEC TEST SUCCESS\n");
}

void testUnlink() {
    char* fileName = "testUnlink.txt";
    int pid, fd[10], i, status;

    pid = forkCreat(fileName);
    assertMsg(pid != -1, "UNLINK TEST: Not create\n");
    assertMsg(join(pid, &status) == 1, "UNLINK TEST: Not join\n");

    for (i = 0; i < 9; i++) {
        fd[i] = open(fileName);
        assertMsg(fd[i] != -1, "UNLINK TEST: Not open\n");
    }

    for (i = 0; i < 3; i++) {
        pid = forkOpen(fileName);
        assertMsg(pid != -1, "UNLINK TEST: Failed to fork thread to open\n");
    }

    assertMsg(unlink(fileName) == 0, "UNLINK TEST: Unable to unlink\n");
    assertMsg(tryOpen(fileName) == 0, "UNLINK TEST: Can still open after unlink\n");

    assertMsg(unlink(fileName) == -1, "UNLINK TEST: Failed when issue unlink twice before actually deteting\n");

    for (i = 0; i < 9; i++)
        close(fd[i]);
    printf("UNLINK TEST SUCCESS\n");
}

void testFileSystemFail() {
    char buffer[100];
    int file, length, i;

    file = open("not_existed_file");
    assertMsg(file == -1, "FILE SYSTEM FAIL TEST: file should not exist\n");

    length = read(file, buffer, 100);
    assertMsg(length == -1, "FILE SYSTEM FAIL TEST: read should fail\n");

    i = write(file, buffer, 100);
    assertMsg(i == -1, "FILE SYSTEM FAIL TEST: write should fail\n");

    i = close(file);
    assertMsg(i == -1, "FILE SYSTEM FAIL TEST: close should fail\n");

    printf("FILE SYSTEM FAIL TEST SUCCESS\n");
}

void testFileSystemPressure() {
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

    for (i = 0; i < 14; i++) {
        name[2] = '0' + ((i + 1) % 10);
        name[1] = i < 9 ? '0' : '1';
        files[i] = creat(name);
        assertMsg(files[i] != -1, "FILE SYSTEM PRESSURE TEST: unable to create\n");
    }

    for (i = 14; i < 20; i++) {
        name[2] = '0' + ((i + 1) % 10);
        name[1] = i < 9 ? '0' : '1';
        files[i] = creat(name);
        assertMsg(files[i] == -1, "FILE SYSTEM PRESSURE TEST: should be unable to create\n");
    }

    for (i = 0; i < 6; i++) {
        flag = close(files[i]);
        assertMsg(flag == 0, "FILE SYSTEM PRESSURE TEST: unable to close\n");
    }

    for (i = 14; i < 20; i++) {
        name[2] = '0' + (i + 1) % 10;
        name[1] = i < 9 ? '0' : '1';
        files[i] = creat(name);
        assertMsg(files[i] != -1, "FILE SYSTEM PRESSURE TEST: unable to create again\n");
    }

    for (i = 0; i < 20; i++) {
        flag = close(files[i]);
        assertMsg(flag == 0, "FILE SYSTEM PRESSURE TEST: unable to close\n");
    }

    printf("FILE SYSTEM PRESSURE TEST SUCCESS\n");
}

void testRoute(int id) {
    switch (id) {
        case 0:
            testExit();
            break;
        case 1:
            testPID();
            break;
        case 2:
            testJoin();
            break;
        case 3:
            testExec();
            break;
        case 4:
            testUnlink();
            break;
        case 5:
            testFileSystemFail();
            break;
        case 6:
            testFileSystemPressure();
            break;
    }
}

int main(int argc, char* argv[]) {
    int i;
	for (i = 0; i < 7; i++)
        testRoute(i);
}
