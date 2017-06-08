#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

void exceedMemory() {
	char str[20000];

	str[20000] = '1';
//	int i;
//
//	for (i = 0; i < 20000; i++)
//	    str[i] = 'A';
}

int main(int argc, char const *argv[])
{
	int i = 0, a = 5, b = 0, c;
	char* ch;
	if (argc != 2)
		exit(2);
//	i = argv[1][0] - '0';

	switch (*argv[1]) {
		case '0':
		    printf("c=a/b");
			c = a / b;
			assert(0);
			break;
		case '1':
		    printf("segmentation fault");
			ch[0] = 'a';
			assert(0);
			break;
		case '2':
			exceedMemory();
			break;
	}

	return 0;
}
