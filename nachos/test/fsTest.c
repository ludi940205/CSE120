#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

int main() {
	printf("Hello world!\n");

	int newFile, oldFile;
	char buffer[16];

	printf("Creating file\n");
	newFile = creat("TestFile.txt");

	printf("Writing file\n");
	write(newFile, "Hello world!!!\n", 15);

	printf("Closing file\n");
	close(newFile);

	printf("Opening file\n");
	oldFile = open("TestFile.txt");

	printf("Reading file\n");
	read(oldFile, buffer, 16);

	printf(buffer);

	printf("Closing file\n");
	close(oldFile);

	return 0;
}

