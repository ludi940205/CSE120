#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int main() {
    int childPID;
    int* status;
    printf("Inside main\n");
    childPID = exec("sucThread.coff", 0, 0);
    join(childPID, status);
    printf("Finished main\n");

    return 0;
}
