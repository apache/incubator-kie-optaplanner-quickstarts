function refreshQuickstartsPanel() {
    $.getJSON("/quickstart", function (quickstarts) {

        $.each(quickstarts, (index, quickstart) => {
            const quickstartPorts = $("#" + quickstart.id + "-ports");
            quickstartPorts.children().remove();
            $.each(quickstart.runningPorts, (index, runningPort) => {
                quickstartPorts
                        .append($(`<div class="card" style="max-width:8rem"/>`)
                                .append($(`<div class="card-header"/>`).text("Port " + runningPort))
                                .append($(`<button type="button" class="btn btn-secondary"/>`)
                                        .append($(`<span class="fas fa-play"/>`))
                                        .text("Show")
                                        .click(() => window.open("//localhost:" + runningPort, '_blank')))
                                .append($(`<button type="button" class="btn btn-danger"/>`)
                                            .append($(`<span class="fas fa-stop"/>`))
                                            .text("Delete")
                                            .click(() => stopQuickstart(quickstart.id, runningPort))));
            });
        });
    });
}

function launchQuickstart(quickstartId) {
    $.post("/quickstart/" + quickstartId + "/launch", function () {
        refreshQuickstartsPanel();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Launching quickstart (" + quickstartId + ") failed.", xhr);
    });
}

function stopQuickstart(quickstartId, runningPort) {
    $.delete("/quickstart/" + quickstartId + "/stop/" + runningPort, function () {
        refreshQuickstartsPanel();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Stopping quickstart (" + quickstartId + ") on port (" + runningPort + ") failed.", xhr);
    });
}

function showError(title, xhr) {
    const serverErrorMessage = !xhr.responseJSON ? `${xhr.status}: ${xhr.statusText}` : xhr.responseJSON.message;
    console.error(title + "\n" + serverErrorMessage);
    const notification = $(`<div class="toast" role="alert" role="alert" aria-live="assertive" aria-atomic="true" style="min-width: 30rem"/>`)
            .append($(`<div class="toast-header bg-danger">
                            <strong class="mr-auto text-dark">Error</strong>
                            <button type="button" class="ml-2 mb-1 close" data-dismiss="toast" aria-label="Close">
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>`))
            .append($(`<div class="toast-body"/>`)
                    .append($(`<p/>`).text(title))
                    .append($(`<pre/>`)
                            .append($(`<code/>`).text(serverErrorMessage))
                    )
            );
    $("#notificationPanel").append(notification);
    notification.toast({delay: 30000});
    notification.toast('show');
}

$(document).ready( function() {
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }
    });
    // Extend jQuery to support $.put() and $.delete()
    jQuery.each( [ "put", "delete" ], function( i, method ) {
        jQuery[method] = function (url, data, callback, type) {
            if (jQuery.isFunction(data)) {
                type = type || callback;
                callback = data;
                data = undefined;
            }
            return jQuery.ajax({
                url: url,
                type: method,
                dataType: type,
                data: data,
                success: callback
            });
        };
    });
    $("#quarkus-school-timetabling-launch").click(function() {
        launchQuickstart("quarkus-school-timetabling");
    });
    $("#quarkus-facility-location-launch").click(function() {
        launchQuickstart("quarkus-facility-location");
    });
    $("#quarkus-factorio-layout-launch").click(function() {
        launchQuickstart("quarkus-factorio-layout");
    });

    refreshQuickstartsPanel();
});
