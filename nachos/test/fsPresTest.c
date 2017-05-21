#include "stdlib.h"
#include "stdio.h"
#include "syscall.h"

int main() {
//    char buffer[5000];
    int file, newfile;
    int length, i, flag;
    int files[20];
    char* name = "testFiles/f00.txt";

//    memset(buffer, 48, 5000);
//    file = creat("testFile.txt");
//    write(file, buffer, 5000);
//    close(file);
//
//    file = open("syscall.h");
//    newfile = creat("syscall_copy.h");
//    length = read(file, buffer, 5000);
//    write(newfile, buffer, 5000);
//    close(file);
//    close(newfile);
//
//    printf("finished first part\n");

    for (i = 0; i < 20; i++) {
        name[12] = '0' + (i + 1) % 10;
        name[11] = i < 9 ? '0' : '1';
        files[i] = creat(name);
        printf("creating file %d, %d\n", i + 1, files[i]);
    }

    for (i = 0; i < 20; i++) {
        flag = close(files[i]);
        printf("closingf file %d, %d\n", i + 1, flag);
    }

    return 0;
}
