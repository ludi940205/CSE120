#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int main(int argc, char* argv[]) {
    int i;
    char name[10]

    if (argc != 1)
        exit(1);
    name = argv[0];

    printf("Inside thread %s\n", name);
    for (i = 0; i < 10000; i++);
    printf("Finished thread %s\n", name);

    return 0;
}
