#include "stdio.h"

int main(int argc, char* argv[]) {
	int fd;

    // printf ("creating %s...\n", fname);
    fd = creat (fname);
    if (fd >= 0) {
		// printf ("...passed (fd = %d)\n", fd);
    } else {
		// printf ("...failed (%d)\n", fd);
		exit (-1001);
    }
    return fd;
}
