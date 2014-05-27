package com.googlecode.lightest.core.tutorial.tasks

import com.googlecode.lightest.core.LightestTask
import com.googlecode.lightest.core.ITaskResult
import com.googlecode.lightest.core.tutorial.WorldNotFoundException

class QueryWorld2 extends LightestTask {
    
    void doPerform(ITaskResult result) {
        def world = env.getWorld()
        
        assert world != null
        
        if ('Alderaan'.equalsIgnoreCase(env.getWorld())) {
            throw new WorldNotFoundException("World not found: ${world}")
        }
        
        result.setMessage("Current World: ${env.getWorld()}")
    }
}