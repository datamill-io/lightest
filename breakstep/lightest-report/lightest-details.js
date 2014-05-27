/**
 * Exactly the same as the default toggle() method, with the exception that
 * three states may be toggled between. Three functions should be passed into
 * this method.
 */
jQuery.fn.toggle3way = function(fn) {
    // Save reference to arguments for access in closure
    var args = arguments, i = 2;

    // link all the functions, so any of them can unbind this click handler
    while (i < args.length)
        jQuery.event.proxy(fn, args[i++]);

    return this.click(jQuery.event.proxy(fn, function(event) {
        // Figure out which function to execute
        this.lastToggle = (this.lastToggle || 0) % i;

        // Make sure that clicks stop
        event.preventDefault();

        // and execute the function
        return args[ this.lastToggle++ ].apply(this, arguments) || false;
    }));
};

var TASK_DISPLAY_LEVEL = [
    ""
    , String.fromCharCode(19968)  // 1
    , String.fromCharCode(20108)  // 2
    , String.fromCharCode(19977)  // 3
];

var expandAll = false;

$(document).ready(function() {
    // add toggle for expanding all tasks
    
    $('#test-name').click(function() {
        if (expandAll) {
            shrinkAllTasks();
            expandAll = false;
        }
        else {
            expandAllTasks();
            expandAll = true;
        }
    });
    
    // add toggle for info source tables
    
    $('.info-source-title .highlight-on-hover').click(function() {
        $(this).parent().next('.info-source-table').toggle(400);
    });
    
    // add hover highlighting
    
    $('.highlight-on-hover').hover(
        function() {
            $(this).css('border', 'solid 1px yellow');
            $(this).css('background-color', 'lightyellow');
        }
        , function() {
            $(this).css('border', "");
            $(this).css('background-color', "");
        }
    );
    
    // hide the fully-qualified path of the class
    
    $('.task-name').each(function() {
        var taskClass = $(this).text();
        $(this).text(taskClass.replace(/.*\./, ""));
    });
    
    $('.task-duration').each(function() {
        var durationMs = $(this).text();
        $(this).text(formatDuration(durationMs));
    });
    
    $('.task-emoticon').each(function() {
        var taskStatus = $(this).text();
        $(this).text(getEmoticon(taskStatus));
    });
    
    // hide the passing nested results initially. Results with non-passing
    // statuses are not hidden.
    
    $('.task').each(function() {
        showByStatus(this, 1, 0);
        setDisplayLevel(this, 2);
    });
    
    if ($.browser.mozilla) {
        $('.task-info > td').each(function() {
            var td = $(this);
            
            td.css('fontSize', '0%');
            td.css('padding', '0');
        });
    }
    else {
        $('.task-info > td').each(function() {
            $(this).hide();
        });
    }
    
    // hide the info source tables initially
    
    $('.info-source-table').hide()
    
    // add toggle for nested tasks
    
    $('.task-header').toggle3way(
        function() {
            // show all
            showByStatus($(this).parents('.task')[0], -1);
            setDisplayLevel(this, 3);
        }
        , function() {
            // hide all
            showByStatus($(this).parents('.task')[0], 100);
            setDisplayLevel(this, 1);
        }
        , function() {
            // show only flagged and higher priority
            showByStatus($(this).parents('.task')[0], 1);
            setDisplayLevel(this, 2);
        }
    );
    
    addHoverToTasks();
    
    // style the task result header
    
    $('.task-result').each(function() {
        styleResultHeader(this);
    });
});

/**
 * Returns a more readable value for a given duration, as specified in
 * milliseconds.
 *
 * @param durationMs  the value to convert and return
 */
function formatDuration(durationMs) {
    var remainingMs = durationMs
    var durationUnits = []
    
    if (remainingMs > 86400000) {
        durationUnits.push(Math.round(remainingMs / 86400000) + 'd');
        remainingMs %= 86400000;
    }
    if (remainingMs > 3600000) {
        durationUnits.push(Math.round(remainingMs / 3600000) + 'h');
        remainingMs %= 3600000;
    }
    if (remainingMs > 60000) {
        durationUnits.push(Math.round(remainingMs / 60000) + 'm');
        remainingMs %= 60000;
    }
    if (remainingMs > 1000) {
        durationUnits.push(Math.round(remainingMs / 1000) + 's');
        remainingMs %= 1000;
    }
    if (durationMs < 1000) {
        durationUnits.push(remainingMs + 'ms');
    }
    
    return durationUnits.join(' ');
}

/**
 * Returns an emoticon string for the given numeric task status.
 *
 * @param status  the numeric status for which to retrieve the appropriate
 *                emoticon
 */
function getEmoticon(status) {
    status = parseInt(status);
    
    switch (status) {
        case 0: return ':)';
        case 1: return ':?';
        case 2: return ':(';
        case 3: return ':O';
    }
    
    return ""
}

/**
 * Adds behavior to tasks which expands them when moused-over. A delay is
 * introduced before the expanding happens, to avoid strange convulsions of
 * the display.
 */
function addHoverToTasks() {
    // add expander
    
    var config = {
        interval: 400,
        
        over: function() {
            if (expandAll) {
                return;
            }
            expandTask($(this));
        }
    };
    
    $('.task').hoverIntent(config);
}

function expandTask(task) {
    if (task.parents('.task').length == 0) {
        task.find('.task-info > td').each(function() {
            if ($.browser.mozilla) {
                $(this).animate({
                    fontSize: '100%',
                    padding: '2px'
                });
            }
            else {
                $(this).show();
            }
        });
    }
}

function shrinkTask(task) {
    if (task.parents('.task').length == 0) {
        task.find('.task-info > td').each(function() {
            if ($.browser.mozilla) {
                $(this).animate({
                    fontSize: '0%',
                    padding: '0'
                });
            }
            else {
                $(this).hide();
            }
        });
    }
}

function expandAllTasks() {
    $('.task').each(function() {
        expandTask($(this));
    });
}

function shrinkAllTasks() {
    $('.task').each(function() {
        shrinkTask($(this));
    });
}

/**
 * Styles the task result header with a color appropriate to its status. The
 * display level indicator is styled with the same color if the task contains
 * nested results, and leaves it the original color otherwise.
 *
 * @param taskResult  the result whose header to style
 */
function styleResultHeader(taskResult) {
    var statusClass;
    
    switch (getStatus(taskResult)) {
        case 1:  statusClass = 'flagged'; break;
        case 2:  statusClass = 'failed';  break;
        case 3:  statusClass = 'doomed';  break;                            
        default: statusClass = 'passed';  break;
    }
    
    var header = $(taskResult).find('.task-header');
    var nestedResults = $(taskResult).next('.nested-results')
        .find('.task-result');
    
    header.addClass(statusClass);
    
    if (nestedResults.length > 0) {
        $(header).find('.task-display-level').addClass(statusClass);
    }
}

/**
 * Returns a numeric status for a given task result node.
 *
 * @param taskResult  the task-result node
 */
function getStatus(taskResult) {
    var taskStatus = $(taskResult).find('.task-status')[0];
    return parseInt(taskStatus.firstChild.nodeValue);
}

/**
 * Hides tasks recursively based on if their status values are less than or
 * equal to a status threshold. Shows them otherwise.
 *
 * @param task
 * @param statusThreshold  the minimum status to hide
 * @param showSpeed        the speed to use when showing or hiding. The default
 *                         is 'slow'. A number indicating ms may be provided.
 */
function showByStatus(task, statusThreshold, showSpeed) {
    if (showSpeed == null) {
        showSpeed = 'slow';
    }
    $(task).children('.nested-results').children('.task').each(function() {
        if (getStatus($(this).find('.task-result')[0]) >= statusThreshold) {
            $(this).show(showSpeed);
        }
        else {
            $(this).hide(showSpeed);
        }
    });
}

function setDisplayLevel(task, level) {
    var displayLevel = $(task).find('.task-display-level')[0];
    $(displayLevel).text(TASK_DISPLAY_LEVEL[level]);
}


