#include "stdlib.h"
#include "stdio.h"
#include "syscall.h"

int main() {
    char buffer[5000];
    int file, newfile;
    int length;
    int i;

    file = open("not_existed_file");
    if (file == -1)
        printf("file does not exist\n");

    length = read(file, buffer, 100);
    if (length == -1)
        printf("read failed\n");

    i = write(file, buffer, 100);
    if (i == -1)
        printf("write failed\n");

    close(file);
    printf("end\n");

    return 0;
}
