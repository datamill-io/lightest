package com.googlecode.lightest.core

import static org.easymock.classextension.EasyMock.*
import static org.easymock.EasyMock.*

import org.easymock.classextension.IMocksControl

class TaskDispatcherTest extends GroovyTestCase {
    def mc
    def mockApi
    def mockStrategy
    def mockListener
    def mockTask
    def mockChildTask

    def node
    def result
    def childResult
    def testcase
    def dispatcher

    @Override
    void setUp() {
        super.setUp()

        mc = createControl()
        createMocks(mc)

        node = new TaskNode(null, 'foo')
        result = new TaskResult(mockTask)
        childResult = new TaskResult(mockChildTask, result)

        testcase = new LightestTestCase()
        testcase.setApi(mockApi)
    }

    private void createMocks(IMocksControl mc) {
        mockApi = mc.createMock(IDomainSpecificApi.class)
        mockStrategy = mc.createMock(ITaskDispatchStrategy.class)
        mockListener = mc.createMock(ILightestTestListener.class)
        mockTask = mc.createMock(ITask.class)
        mockChildTask = mc.createMock(ITask.class)

        mockListener.addEnvironmentMapping(anyObject(), isNull())
        expectLastCall().anyTimes()
        expect(mockTask
            .configure(isA(Node.class), anyObject()))
            .anyTimes()
        expect(mockTask
            .setDispatcher(isA(TaskDispatcher.class)))
            .anyTimes()
        expect(mockChildTask
            .configure(isA(Node.class), anyObject()))
            .anyTimes()
        expect(mockChildTask
            .setDispatcher(isA(TaskDispatcher.class)))
            .anyTimes()
    }
    
    /**
     * We trigger some mock expectations when setting up the dispatcher, so
     * this method must be called in replay mode.
     */
    private createDispatcher() {
        def classLoader = this.class.classLoader
        def contextFactory = new LightestContextFactory(
            LightestContextFactory.DEFAULT_CONTEXT_CLASS, classLoader, null)
        def context = new ThreadedLightestContext(contextFactory)
        
        dispatcher = new TaskDispatcher(context)
        dispatcher.setListener(mockListener)
        dispatcher.setTestCase(testcase)
        dispatcher.setStrategy(mockStrategy)
    }

    void testDispatchThrowsMME() {
        expect(mockApi.getTask(eq('foo')))
            .andReturn(null)

        mc.replay()
        
        createDispatcher()

        shouldFail(MissingMethodException.class, {
            dispatcher.dispatch(node, null, null)
        })

        mc.verify()
    }

    void testDispatchDoomedResultThrowsException() {
        expect(mockApi
            .getTask(eq(node.name())))
            .andReturn(mockTask)
        expect(mockStrategy
            .dispatch(eq(mockTask)))
            .andReturn(result)

        result.doom()
        result.setMessage('bar')

        mc.replay()

        createDispatcher()

        shouldFail(TaskDoomedException.class) {
            dispatcher.dispatch(node, null, null)
        }

        mc.verify()
    }

    void testDispatchFailedResultSkipsNestedTasks() {
        def childNode = new TaskNode(node, 'bar')

        expect(mockApi
            .getTask(eq(node.name())))
            .andReturn(mockTask)
        expect(mockStrategy
            .dispatch(eq(mockTask)))
            .andReturn(result)
        expect(mockListener
            .onTaskComplete(result, null))

        result.fail()

        mc.replay()

        createDispatcher()

        def taskResult = dispatcher.dispatch(node, null, null)

        // getTask() is never called on mockApi for the 'bar' task; if it were,
        // we'd get a missing expectation failure from EasyMock

        assertEquals(result, taskResult)

        mc.verify()
    }

    void testDispatchNestedTasksArePerformed() {
        def childNode = new TaskNode(node, 'bar', 'baz')

        expect(mockApi
            .getTask(eq(node.name())))
            .andReturn(mockTask)
        expect(mockStrategy
            .dispatch(eq(mockTask)))
            .andReturn(result)
        expect(mockApi
            .getTask(eq(childNode.name())))
            .andReturn(mockChildTask)
        expect(mockStrategy
            .dispatch(eq(mockChildTask)))
            .andReturn(childResult)
        expect(mockListener
            .onTaskComplete(childResult, null))
        expect(mockListener
            .onTaskComplete(result, null))

        mc.replay()

        createDispatcher()

        def taskResult = dispatcher.dispatch(node, null, null)

        assertEquals(result, taskResult)
        assertEquals(1, taskResult.children().size())

        def childTaskResult = taskResult.children()[0]

        assertEquals(childResult, childTaskResult)
        assertEquals(result, childTaskResult.parent)
        assertEquals(0, childTaskResult.children().size())

        mc.verify()
    }
}
