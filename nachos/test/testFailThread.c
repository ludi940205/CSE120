#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

void exceedMemory() {
	char str[10000];
}

int main(int argc, char const *argv[])
{
	int i = 0, a = 5, b = 0;
	char* c;
	if (argc != 1)
		exit(2);
	i = argv[0][0] - '0';

	switch (i) {
		case 0:
			a /= b;
		case 1:
			c[0] = 'a';
		case 2:
			exceedMemory();
	}

	return 0;
}