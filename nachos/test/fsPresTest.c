#include "stdlib.h"
#include "stdio.h"
#include "syscall.h"

int main() {
    char buffer[10000];
    int file, newfile;
    int length;

    memset(buffer, 48, 10000);
    file = creat("testFile.txt");
    write(file, buffer, 10000);
    close(file);

    file = open("syscall.h");
    newfile = creat("syscall_copy.h");
    length = read(file, buffer, 10000);
    write(newfile, buffer, 10000);
    close(file);
    close(newfile);

    return 0;
}