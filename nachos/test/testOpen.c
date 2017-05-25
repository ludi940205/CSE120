#include "stdio.h"

int main(int argc, char* argv[]) {
	int fd;

    // printf ("opening %s...\n", fname);
    fd = open (fname);
    if (fd >= 0) {
	   // printf ("...passed (fd = %d)\n", fd);
    } else {
	   // printf ("...failed (%d)\n", fd);
	   exit (-1001);
    }
    return fd;
}
