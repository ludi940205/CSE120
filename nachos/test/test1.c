#include "stdio.h"

#define LOG     printf
#define NULL        0
#define NUMVARS     8
#define NAN         (0xEFFFFFFF)
#define MAXARGC     20
#define MAXPROCESS  9


int main() {
    int pid[10];                    /* array to store pid                                    */
    char *executable;               /* executable file name for exec()                       */
    char *_argv[MAXARGC];           /* argv for testing executable                           */
    int  _argc;                     /* argc for testing executable                           */
    // int  i,j;                       /* counter for loop                                      */
    int  exitstatus;                /* exit status of child process                          */
    int  retval;                    /* return value of system call                           */


    LOG("++ISPRMGR VAR7: [STARTED]\n");
    LOG("++ISPRMGR VAR7: test your syscall join to be invoked more than once\n");

    executable = "exittest.coff";
    _argv[0] = executable;
    _argv[1] = NULL;
    _argc = 1;
    LOG("++ISPRMGR VAR7: exec %s\n", executable);
    pid[0] = exec(executable, _argc, _argv);
    LOG("++ISPRMGR VAR7: Child process id is %d\n", pid[0]);
    LOG("++ISPRMGR VAR7: Issue join to get exit status of chile process\n");
    retval = join(pid[0], &exitstatus);
    if (retval != 0) {
        LOG("++ISPRMGR VAR7: [ENDED] FAIL\n");
    }
    LOG("++ISPRMGR VAR7: first time invoke join successfully\n");

    LOG("++ISPRMGR VAR7: Issue join again to get exit status of chile process %d\n", pid[0]);
    retval = join(pid[0], &exitstatus);
    if (retval == 0) {
        LOG("++ISPRMGR VAR7: [ENDED] FAILED to join process %d\n", pid[0]);
    }
    LOG("++ISPRMGR VAR7: failed to invoke join second time as exptected\n");

    LOG("++ISPRMGR VAR7: [ENDED] SUCCESS\n");

    return 0;
}