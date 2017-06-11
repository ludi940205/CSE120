#include "stdio.h"

#define NULL        0
#define MAXPROCESS  9

void assertMsg(int arg, char* msg) {
    if (arg == 0) {
        printf("%s\n", msg);
        exit(-1);
    }
}

int main() {
    char* executable;
    char* argv[3];
    int pid, argc, i;

    executable = "cp.coff";
    argv[0] = executable;
    argv[1] = "write.out";
    argv[2] = "write_copy0.out";
    argc = 3;

    for (i = 0; i < 5; i++) {
        argv[2][10] = i + '0';
        pid = exec(executable, argc, argv);
        assertMsg(pid != -1, "MULTI THREAD READ WRITE TEST: Unable to exec cp.coff\n");
    }

    executable = "cat.coff";
    argv[0] = executable;
    argv[1] = "write_copy0.out";
    argc = 2;

    for (i = 0; i < 5; i++) {
        argv[2][10] = i + '0';
        pid = exec(executable, argc, argv);
        assertMsg(pid != -1, "MULTI THREAD READ WRITE TEST: Unable to exec cat.coff\n");
    }

    printf("MULTI THREAD READ WRITE TEST SUCCESS\n");
}
