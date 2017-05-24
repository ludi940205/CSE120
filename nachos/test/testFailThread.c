#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

void exceedMemory() {
	char str[20000];

	str[0] = '1';
//	int i;
//
//	for (i = 0; i < 20000; i++)
//	    str[i] = 'A';
}

int main(int argc, char const *argv[])
{
	int i = 0, a = 5, b = 0, c;
	char* ch;
	if (argc != 1)
		exit(2);
	i = argv[0][0] - '0';
	printf("%d\n", i);

	switch (i) {
		case 0:
			c = a / b;
			printf("%d / %d = %d\n", a, b, c);
			break;
		case 1:
			ch[0] = 'a';
			printf("Segmentation fault\n");
			break;
		case 2:
			exceedMemory();
			break;
	}

	return 0;
}
