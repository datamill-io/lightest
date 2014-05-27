package com.googlecode.lightest.core.tutorial.tasks

import com.googlecode.lightest.core.LightestTask
import com.googlecode.lightest.core.ITaskResult

class QueryWorld3 extends LightestTask {
    
    void doPerform(ITaskResult result) {
        def world = env.getWorld()
        
        assert world != null
        
        if ('Alderaan'.equalsIgnoreCase(env.getWorld())) {
            //throw new WorldNotFoundException("World not found: ${world}")
            result.setMessage("World not found: ${world}")
            result.fail()
        }
        else if ('Atlantis'.equalsIgnoreCase(env.getWorld())) {
            result.setMessage("World quite difficult to find: ${world}")
            result.flag()
        }
        else if ('Hades'.equalsIgnoreCase(env.getWorld())) {
            result.setMessage("Unable to find more worlds: ${world}")
            result.doom()
        }
        else {
            result.setMessage("Current World: ${env.getWorld()}")
        }
    }
}