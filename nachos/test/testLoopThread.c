#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int main(int argc, char* argv[]) {
    int i;

    if (argc != 1)
        exit(1);

   printf("Inside thread %s\n", argv[0]);
    for (i = 0; i < 1000000; i++);
    printf("Finished thread %s\n", argv[0]);

    return 0;
}
