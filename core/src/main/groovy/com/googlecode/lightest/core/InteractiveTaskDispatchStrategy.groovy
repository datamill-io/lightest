package com.googlecode.lightest.core

class InteractiveTaskDispatchStrategy
        implements IInterruptibleTaskDispatchStrategy {
    public static final int MODE_FLY = 0
    public static final int MODE_RUN = 1
    public static final int MODE_WALK = 2
    public static final int MODE_CRAWL = 3

    public static final def HELP_MESSAGE =
            """
You are currently in interactive mode. You may step through test execution task
by task and view the results of tasks as they are performed.

Available commands:

    a, again  Try the current task again.

    c, crawl  Move to the next task, descending into child tasks (if any).

    w, walk   Move to the next top-level task, skipping all child tasks.

    r, run    Exit interactive mode and resume normal execution of tasks, until
              either a failure or breakpoint is encountered.

    f, fly    Exit interactive mode and resume normal execution of tasks. Don't
              stop for anything less than user input.
    
    h, help   Show this help message.
"""

    int mode
    boolean interruptible

    private interrupted
    private scanner
    private out

    InteractiveTaskDispatchStrategy() {
        mode = MODE_FLY
        interruptible = true
        interrupted = false
    }

    /**
     * @param _in the input stream to read when in interactive mode
     * @param out the output stream to write messages to the interactive user
     */
    InteractiveTaskDispatchStrategy(InputStream _in, PrintStream out) {
        this()
        setIn(_in)
        setOut(out)
    }

    void setIn(InputStream _in) {
        scanner = new Scanner(_in)
    }

    void setOut(PrintStream out) {
        this.out = out
    }

    /**
     * A directive to indicating interactive execution of tasks should be
     * allowed as soon as possible. This is typically invoked as a response to
     * user input.*/
    void interrupt() {
        if (interruptible == true) {
            interrupted = true
        }
    }

    /**
     * Dispatches the task. If in interactive mode, the user can manage the
     * performing of the task, allowing dynamic task retry, for example. If
     * the task is a breakpoint task, interactive mode may be automatically
     * entered.
     *
     * @param task
     */
    ITaskResult dispatch(ITask task) {
        def result = task.perform()

        if (interrupted) {
            out.println '[entering interactive mode]'
            mode = MODE_CRAWL
            interrupted = false
        }

        loop:
        while (mode > MODE_FLY) {
            if (task instanceof IBreakpointTask && task.isBreakpoint()) {
                out.println '[breakpoint]'
            } else {
                switch (mode) {
                    case MODE_RUN:
                        if (result.getStatus() == ITaskResult.STATUS_OK) {
                            break loop
                        }
                        break

                    case MODE_WALK:
                        if (result.parent) {
                            break loop
                        }
                        break
                }
            }

            def status = ITaskResult.HUMAN_READABLE_STATUS[result.getStatus()]
            def entry = LightestUtils.getTestCaseStackElement()

            out.println "Current task: \"${task.getDescription()}\""
            out.println "${task.getShortName()} ${task.getParams()}" + (entry ? " ... at ${entry?.getMethodName()}(), " + "line ${entry?.getLineNumber()}" : "")
            out.println "${status}, \"${result.getMessage()}\""
            out.print '>>> '

            def command = scanner.nextLine()

            switch (command) {
                case 'a':
                case 'again':
                    out.println '[again]'
                    result = task.perform()
                    break

                case 'c':
                case 'crawl':
                    out.println '[crawl]'
                    mode = MODE_CRAWL
                    break loop

                case 'w':
                case 'walk':
                    out.println '[walk]'
                    mode = MODE_WALK
                    break loop

                case 'r':
                case 'run':
                    out.println '[run]'
                    mode = MODE_RUN
                    break loop

                case 'f':
                case 'fly':
                    out.println '[fly]'
                    mode = MODE_FLY
                    break loop

                case 'h':
                case 'help':
                    out.println '[help]'
                    out.println HELP_MESSAGE
                    break

                default:
                    out.println "[unknown command \"${command}\"]"
                    out.println HELP_MESSAGE
                    break
            }
        }

        return result
    }
}