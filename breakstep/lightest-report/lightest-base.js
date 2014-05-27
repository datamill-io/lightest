// namespacing variable
var lightestBase = {
    lastExpandedDetails: null
};

$(document).ready(function() {
    decorateToggleDetailsVisibility();
});

function togglePendingMethodsTable(toggle) {
    var pendingMethods = $('#pending-methods');
    
    if ($(toggle).attr('checked')) {
        pendingMethods.show();
    }
    else {
        pendingMethods.hide();
    }
}

/**
 * Decorates the TestNG-XSLT javascript function toggleDetailsVisibility() to
 * add some behaviors.
 */
function decorateToggleDetailsVisibility() {
    var original = toggleDetailsVisibility;
    
    window.toggleDetailsVisibility = function(elementId) {
        if (/_details$/.test(elementId)) {
            var exceptionId = elementId.replace(/details$/, 'exception');
            var element = $(document.getElementById(elementId));
            var resultId = element.attr('result-id');
            
            if (lightestBase.lastExpandedDetails != null &&
                lightestBase.lastExpandedDetails != element) {
                $(lightestBase.lastExpandedDetails)
                    .removeClass('testMethodDetailsVisible')
                    .addClass('testMethodDetails');
            }
            
            if (! element.hasClass('testMethodDetailsVisible')) {
                // this element will be expanded
                lightestBase.lastExpandedDetails = element;
            }
            
            // set frame
            if (resultId) {
                top.frames['details'].location =
                    'test-result-' + resultId + '.xml';
            }
        }
        
        original(elementId);
    }
}

