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
	// char* executable;
 //    char* argv[3];
 //    int pid[MAXPROCESS], argc, i, j, exitstatus;

 //    executable = "cp.coff";
 //    argv[0] = executable;
 //    argv[1] = "syscall.h";
 //    argv[2] = "syscall_copy.h";
 //    argc = 3;

 //    for (i = 0; i <  MAXPROCESS; i++) {
 //        pid[i] = exec(executable, argc, argv);
 //        // join(pid[i], &exitstatus);
 //        assertMsg(pid[i] != -1, "PID TEST: Unable to exec cp.coff\n");
    
 //        // for (j = 0; j < i; ++j)
 //        //     assertMsg(pid[j] == -1 || pid[j] != pid[i], "PID TEST: pid equals");
 //    }

 //    printf("PID TEST SUCCESS\n");


    char* executable;
    char* argv[3];
    char filename[20] = "syscall_copy0.h";
    int pid, argc, i;

    executable = "cp.coff";
    argv[0] = executable;
    argv[1] = "syscall.h";
    argv[2] = "syscall_copy0.h";
    argc = 3;

    for (i = 0; i < 10; i++) {
    	filename[12] = i + '0';
        argv[2] = filename;
        pid = exec(executable, argc, argv);
        assertMsg(pid != -1, "MULTI THREAD READ WRITE TEST: Unable to exec cp.coff\n");
    }

    // executable = "cat.coff";
    // argv[0] = executable;
    // argv[1] = "syscall_copy0.h";
    // argc = 2;

    // for (i = 0; i < 5; i++) {
    //     filename[12] = i + '0';
    //     argv[2] = filename;
    //     pid = exec(executable, argc, argv);
    //     assertMsg(pid != -1, "MULTI THREAD READ WRITE TEST: Unable to exec cat.coff\n");
    // }

    printf("MULTI THREAD READ WRITE TEST SUCCESS\n");
}
