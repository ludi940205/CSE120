#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int main() {
    int childPID[10], i;
    int* status;
    char name[1][10] = {"T0"};

    printf("Inside main\n");

    for (i = 0; i < 10; i++) {
        name[0][1] = '0' + i;
        printf("Creating %s\n", name[0]);
        childPID[i] = exec("testLoopThread.coff", 1, name);
    }

//    for (i = 0; i < 10; i++)
//        join(childPID[i], status);

    printf("Finished main\n");

    return 0;
}
