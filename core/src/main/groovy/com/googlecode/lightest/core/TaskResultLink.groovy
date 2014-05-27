package com.googlecode.lightest.core

/**
 * Represents a link that an ITaskResult wishes to share with the reporting
 * service.*/
class TaskResultLink implements Serializable {
    String href
    String rel
    String title
    String text
}
