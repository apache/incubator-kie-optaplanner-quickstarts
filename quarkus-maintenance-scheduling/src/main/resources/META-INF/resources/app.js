var autoRefreshCount = 0;
var autoRefreshIntervalId = null;

// Initial date/time
var initialTimeString = "2020-01-01T00:00:00Z";

// DOM element where the Timeline will be attached
var container = document.getElementById('visualization');
var assignedMaintenanceJobs = new vis.DataSet();

// Configuration for the Timeline
var options = {
    // Make jobs editable through UI
    editable: true,
    // Set time zone to UTC
    moment: function (date) {
        return vis.moment(date).utc();
    },
    // Always snap to full hours, independent of the scale
    snap: function (date, scale, step) {
        var hour = 60 * 60 * 1000;
        return Math.round(date / hour) * hour;
    },

};

// Create timeline displaying maintenance jobs
var timeline = new vis.Timeline(container, assignedMaintenanceJobs, options);

$(document).ready(function () {
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }
    });
    // Extend jQuery to support $.put() and $.delete()
    jQuery.each(["put", "delete"], function (i, method) {
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

    $("#refreshButton").click(function () {
        refreshSchedule(true);
    });
    $("#solveButton").click(function () {
        solve();
    });
    $("#stopSolvingButton").click(function () {
        stopSolving();
    });
    $("#addJobButton").click(function () {
        dropdownMaintenanceUnits();
    });

    refreshSchedule(true);
});

function solve() {
    $.post("/schedule/solve", function () {
        refreshSolvingButtons(true);
        autoRefreshCount = 16;
        if (autoRefreshIntervalId == null) {
            autoRefreshIntervalId = setInterval(autoRefresh, 2000);
        }
    }).fail(function (xhr, ajaxOptions, thrownError) {
        // TODO: Implement showError
        // showError("Start solving failed.", xhr);
    });
}

function refreshSolvingButtons(solving) {
    if (solving) {
        $("#solveButton").hide();
        $("#stopSolvingButton").show();
    } else {
        $("#solveButton").show();
        $("#stopSolvingButton").hide();
    }
}

function autoRefresh() {
    refreshSchedule(true);
    autoRefreshCount--;
    if (autoRefreshCount <= 0) {
        clearInterval(autoRefreshIntervalId);
        autoRefreshIntervalId = null;
    }
}

function stopSolving() {
    $.post("/schedule/stopSolving", function () {
        refreshSolvingButtons(false);
        refreshSchedule(true);
        clearInterval(autoRefreshIntervalId);
        autoRefreshIntervalId = null;
    }).fail(function (xhr, ajaxOptions, thrownError) {
        //showError("Stop solving failed.", xhr);
    });
}

function dropdownMaintenanceUnits() {
    // Fetch maintenance units and display in a dropdown
    var unitDropdown = $("#maintenance_unit_dropdown");
    unitDropdown.empty();
    $.getJSON("/units", function (units) {
        $.each(units, (index, unit) => {
            $("<option>")
                .html(unit.unitName)
                .appendTo(unitDropdown);
        });
    });
}

function refreshSchedule(fitSchedule) {
    $.getJSON("/schedule", function (schedule) {
        refreshSolvingButtons(schedule.solverStatus != null && schedule.solverStatus !== "NOT_SOLVING");
        $("#score").text("Score: " + (schedule.score == null ? "?" : schedule.score));

        const unassignedJobs = $("#unassignedJobs");
        unassignedJobs.children().remove();

        assignedMaintenanceJobs.clear();
        $.each(schedule.maintenanceJobList, (index, job) => {
            if (job.assignedCrew != null && job.startingTimeGrain != null) {
                assignedMaintenanceJobs.add({
                    id: job.id,
                    content: "<b>" + job.jobName + "</b><br><i>by " + job.assignedCrew.crewName + "</i><br>" +
                        job.maintainableUnit.unitName,
                    start: moment(initialTimeString).add(job.startingTimeGrain.grainIndex, 'hours'),
                    end: moment(initialTimeString).add(job.startingTimeGrain.grainIndex + job.durationInGrains, 'hours')
                });
            }
            else {
                const unassignedJobElement = $(`<div class="card bg-secondary"/>`)
                    .append($(`<div class="card-body p-2"/>`)
                        .append($(`<b h6 class="card-title mb-1"/>`).text(job.jobName))
                        .append($(`<p class="card-text"/>`).text(job.maintainableUnit.unitName)));
                unassignedJobs.append(unassignedJobElement);
            }
        });

        if (fitSchedule) {
            timeline.fit();
        }
    });
}
