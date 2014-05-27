package com.googlecode.lightest.core.tutorial

import org.testng.annotations.*

class TutorialLocal extends TutorialBase {
    
    @Test
    void testPointlessBureaucracy() {
        def money = 3
        
        context.'monies' = money
        
        HelloWorld (greeting: "I have ${money} awesome dollars!")
        
        EntrenchedBureaucracy () {
            EntrenchedBureaucracy () {
                EntrenchedBureaucracy () {
                    EntrenchedBureaucracy ()
                }
            }
        }
    }
}