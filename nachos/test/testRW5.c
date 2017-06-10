#include "stdio.h"

#define NULL        0
#define MAXPROCESS  9

void assertMsg(int arg, char* msg) {
    if (arg == 0) {
        printf("%s\n", msg);
        exit(-1);
    }
}