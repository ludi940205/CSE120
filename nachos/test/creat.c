#include "syscall.h"
#include "stdlib.h"

int main() {
    char* fileName = "test.txt";
    creat(fileName);

    return 0;
}
