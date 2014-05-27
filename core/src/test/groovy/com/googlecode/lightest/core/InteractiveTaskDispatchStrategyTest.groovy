package com.googlecode.lightest.core

class InteractiveTaskDispatchStrategyTest extends GroovyTestCase {
    def ln
    def os
    def dispatchStrategy
    
    def childTask
    def childTaskWithBreakpoint
    def childTaskWithFlag
    def topLevelTask
    def topLevelTaskWithBreakpoint
    def topLevelTaskWithFlag
    
    @Override
    void setUp() {
        ln = System.getProperty('line.separator')
        os = new ByteArrayOutputStream()
        dispatchStrategy = createDispatchStrategy(new PrintStream(os))
        
        def resultStub = new TaskResult(null)
        
        childTask = createTask({ r -> }, false, resultStub)
        childTaskWithBreakpoint = createTask({ r -> }, true, resultStub)
        childTaskWithFlag = createTask({ r -> r.flag() }, false, resultStub)
        topLevelTask = createTask({ r -> }, false)
        topLevelTaskWithBreakpoint = createTask({ r -> }, true)
        topLevelTaskWithFlag = createTask({ r -> r.flag() }, false)
    }
    
    private createDispatchStrategy(PrintStream out) {
        def _in = new StringBufferInputStream('fly\n')
        dispatchStrategy = new InteractiveTaskDispatchStrategy(_in, out)
    }
    
    private createTask(Closure doPerformImpl, boolean breakpoint,
        ITaskResult parentResult = null)
    {
        def task = [
            doPerform: doPerformImpl,
            isBreakpoint: { return breakpoint },
            getDescription: { return "description" },
            getParams: { return [ paramName: 'paramValue' ] },
            getShortName: { return "shortName" }
        ] as LightestTask
        
        task.taskResult = new TaskResult(task, parentResult)
        task.parentResult = parentResult
        
        return task
    }
    
    void testCrawlStopsOnChildTask() {
        dispatchStrategy.setMode(InteractiveTaskDispatchStrategy.MODE_CRAWL)
        dispatchStrategy.dispatch(childTask)
        
        // if we get a print out of the current task, we've successfully
        // entered interactive mode
        
        assertTrue(os.toString().startsWith('Current task'))
    }
    
    void testWalkSkipsChildTask() {
        dispatchStrategy.setMode(InteractiveTaskDispatchStrategy.MODE_WALK)
        dispatchStrategy.dispatch(childTask)
        
        // if we don't get a print out of anything, we haven't entered
        // interactive mode
        
        assertEquals("", os.toString())
    }
    
    void testWalkStopsOnChildTaskWithBreakpoint() {
        dispatchStrategy.setMode(InteractiveTaskDispatchStrategy.MODE_WALK)
        dispatchStrategy.dispatch(childTaskWithBreakpoint)
        
        assertTrue(os.toString().startsWith("[breakpoint]${ln}Current task"))
    }
    
    void testRunSkipsTopLevelTask() {
        dispatchStrategy.setMode(InteractiveTaskDispatchStrategy.MODE_RUN)
        dispatchStrategy.dispatch(topLevelTask)
        
        assertEquals("", os.toString())        
    }
    
    void testRunStopsOnChildTaskWithBreakpoint() {
        dispatchStrategy.setMode(InteractiveTaskDispatchStrategy.MODE_RUN)
        dispatchStrategy.dispatch(childTaskWithBreakpoint)
        
        assertTrue(os.toString().startsWith("[breakpoint]${ln}Current task"))
    }
    
    void testRunStopsOnChildTaskWithFlag() {
        dispatchStrategy.setMode(InteractiveTaskDispatchStrategy.MODE_RUN)
        dispatchStrategy.dispatch(childTaskWithFlag)
        
        assertTrue(os.toString().startsWith('Current task'))
    }
    
    void testFlySkipsTopLevelTaskWithBreakpoint() {
        dispatchStrategy.setMode(InteractiveTaskDispatchStrategy.MODE_FLY)
        dispatchStrategy.dispatch(topLevelTaskWithBreakpoint)
        
        assertEquals("", os.toString())
    }
    
    void testFlySkipsTopLevelTaskWithFlag() {
        dispatchStrategy.setMode(InteractiveTaskDispatchStrategy.MODE_FLY)
        dispatchStrategy.dispatch(topLevelTaskWithFlag)
        
        assertEquals("", os.toString())
    }
}