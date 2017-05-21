#include "stdlib.h"
#include "stdio.h"
#include "syscall.h"

int main() {
    int files[20];
    int flag;

    files[0] = creat("f1.txt");
    printf("creating file %d, %d\n", 1, files[0]);
    files[1] = creat("f2.txt");
    printf("creating file %d, %d\n", 2, files[1]);
    files[2] = creat("f3.txt");
    printf("creating file %d, %d\n", 3, files[2]);
    files[3] = creat("f4.txt");
    printf("creating file %d, %d\n", 4, files[3]);
    files[4] = creat("f5.txt");
    printf("creating file %d, %d\n", 5, files[4]);
    files[5] = creat("f6.txt");
    printf("creating file %d, %d\n", 6, files[5]);
    files[6] = creat("f7.txt");
    printf("creating file %d, %d\n", 7, files[6]);
    files[7] = creat("f8.txt");
    printf("creating file %d, %d\n", 8, files[7]);
    files[8] = creat("f9.txt");
    printf("creating file %d, %d\n", 9, files[8]);
    files[9] = creat("f10.txt");
    printf("creating file %d, %d\n", 10, files[9]);
    files[10] = creat("f11.txt");
    printf("creating file %d, %d\n", 11, files[10]);
    files[11] = creat("f12.txt");
    printf("creating file %d, %d\n", 12, files[11]);
    files[12] = creat("f13.txt");
    printf("creating file %d, %d\n", 13, files[12]);
    files[13] = creat("f14.txt");
    printf("creating file %d, %d\n", 14, files[13]);
    files[14] = creat("f15.txt");
    printf("creating file %d, %d\n", 15, files[14]);
    files[15] = creat("f16.txt");
    printf("creating file %d, %d\n", 16, files[15]);
    files[16] = creat("f17.txt");
    printf("creating file %d, %d\n", 17, files[16]);
    files[17] = creat("f18.txt");
    printf("creating file %d, %d\n", 18, files[17]);
    files[18] = creat("f19.txt");
    printf("creating file %d, %d\n", 19, files[18]);
    files[19] = creat("f20.txt");
    printf("creating file %d, %d\n", 20, files[19]);

    flag = close(files[0]);
    printf("closing file %d, %d\n", 1, flag);
    flag = close(files[1]);
    printf("closing file %d, %d\n", 2, files[1]);
    flag = close(files[2]);
    printf("closing file %d, %d\n", 3, files[2]);
    flag = close(files[3]);
    printf("closing file %d, %d\n", 4, files[3]);
    flag = close(files[4]);
    printf("closing file %d, %d\n", 5, files[4]);
    flag = close(files[5]);
    printf("closing file %d, %d\n", 6, files[5]);
    flag = close(files[6]);
    printf("closing file %d, %d\n", 7, files[6]);
    flag = close(files[7]);
    printf("closing file %d, %d\n", 8, files[7]);
    flag = close(files[8]);
    printf("closing file %d, %d\n", 9, files[8]);
    flag = close(files[9]);
    printf("closing file %d, %d\n", 10, files[9]);
    flag = close(files[10]);
    printf("closing file %d, %d\n", 11, files[10]);
    flag = close(files[11]);
    printf("closing file %d, %d\n", 12, files[11]);
    flag = close(files[12]);
    printf("closing file %d, %d\n", 13, files[12]);
    flag = close(files[13]);
    printf("closing file %d, %d\n", 14, files[13]);
    flag = close(files[14]);
    printf("closing file %d, %d\n", 15, files[14]);
    flag = close(files[15]);
    printf("closing file %d, %d\n", 16, files[15]);
    flag = close(files[16]);
    printf("closing file %d, %d\n", 17, files[16]);
    flag = close(files[17]);
    printf("closing file %d, %d\n", 18, files[17]);
    flag = close(files[18]);
    printf("closing file %d, %d\n", 19, files[18]);
    flag = close(files[19]);
    printf("closing file %d, %d\n", 20, files[19]);

    return 0;
}
