#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

void exceedMemory() {
	char str[10000];
}

int main(int argc, char const *argv[])
{
	int i = 0, a = 5, b = 1, c;
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
//			exceedMemory();
			break;
	}

	return 0;
}
