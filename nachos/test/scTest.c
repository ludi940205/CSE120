#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int main() {
    int childPID, i;
    int* status;
    char name[1][10] = {"T0"};

    printf("Inside main\n");

    for (i = 0; i < 10; i++) {
        name[0][1] = '0' + i;
        childPID = exec("sucThread.coff", 1, name);
        join(childPID, status);
    }

    printf("Finished main\n");

    return 0;
}
