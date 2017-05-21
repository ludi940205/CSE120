#include "stdlib.h"
#include "stdio.h"
#include "syscall.h"

int main() {
    int files[20];
    int flag;

    files[0] = creat("testFiles/f1.txt");
    printf("creating file %d, %d\n", 1, files[0]);

    flag = close(files[0]);
    printf("closing file %d, %d\n", 1, flag)

    return 0;
}