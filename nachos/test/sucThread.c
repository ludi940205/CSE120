#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

int main() {
    int i;
    printf("Inside thread 1\n");
    for (i = 0; i < 10000; i++);
    printf("Finished thread 1\n");

    return 0;
}