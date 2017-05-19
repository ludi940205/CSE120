#include "syscall.h"
#include "stdlib.h"

int main(int argc,char *argv[]) {
    if (argc == 0)
        return 0;
    if (argc == 1)
        return 0;
    char fileName[100];
    strcpy(fileName, argv[1]);
    creat(fileName);

    return 0;
}