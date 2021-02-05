var autoRefreshCount = 0;
var autoRefreshIntervalId = null;

// Initial date/time (next Monday)
var initialDateTime = moment().utc().startOf('isoWeek').add(1, 'weeks').set('hour', 9);

// DOM element where the Timeline will be attached
var container = document.getElementById('visualization');
var assignedMaintenanceJobs = new vis.DataSet();
var crewGroups = new vis.DataSet();

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
var timeline = new vis.Timeline(container, assignedMaintenanceJobs, crewGroups, options);

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

        // Add a group for each crew
        crewGroups.clear();
        $.each(schedule.assignedCrewList, (index, crew) => {
            crewGroups.add({
                id: crew.id,
                content: crew.crewName
            });
        });

        // Label each mutex job with tag
        const jobToMutexTagMap = {};
        $.each(schedule.mutuallyExclusiveJobsList, (index, mutuallyExclusiveJobs) => {
            $.each(mutuallyExclusiveJobs.mutexJobs, (index, job) => {
                if (jobToMutexTagMap[job.jobName] == null) {
                    jobToMutexTagMap[job.jobName] = [mutuallyExclusiveJobs.exclusiveTag];
                }
                else {
                    jobToMutexTagMap[job.jobName].push(mutuallyExclusiveJobs.exclusiveTag);
                }
            });
        });

        assignedMaintenanceJobs.clear();
        $.each(schedule.maintenanceJobList, (index, job) => {
            if (job.assignedCrew != null && job.startingTimeGrain != null) {
                assignedMaintenanceJobs.add({
                    id: job.id,
                    group: job.assignedCrew.id,
                    content: `<b>` + job.jobName + `</b><br><i>by ` + job.assignedCrew.crewName + `</i><br>` +
                        job.maintainableUnit.unitName,
                    start: moment(initialDateTime).add(job.startingTimeGrain.grainIndex, `hours`),
                    end: moment(initialDateTime).add(job.startingTimeGrain.grainIndex + job.durationInGrains, `hours`)
                });
            }
            else {
                const unassignedJobElement = $(`<div class="card bg-secondary"/>`)
                    .append($(`<div class="card-body p-2"/>`)
                    .append($(`<b h6 class="card-title"/>`).text(job.jobName))
                    .append($(`<br><span class="badge" style="background-color: white"/>`)
                        .text(job.maintainableUnit.unitName)));
                // Append mutex tags on jobs
                $.each(jobToMutexTagMap[job.jobName], (index, mutexTag) => {
                    const color = pickColor(mutexTag);
                    unassignedJobElement.append($(`<span class="badge badge-info m-2" style="background-color: ${color}"/>`)
                        .text(mutexTag));
                });
                unassignedJobs.append(unassignedJobElement);
            }
        });

        if (fitSchedule) {
            timeline.fit();
        }
    });
}

// ****************************************************************************
// TangoColorFactory
// ****************************************************************************

const SEQUENCE_1 = [0x8AE234, 0xFCE94F, 0x729FCF, 0xE9B96E, 0xAD7FA8];
const SEQUENCE_2 = [0x73D216, 0xEDD400, 0x3465A4, 0xC17D11, 0x75507B];

var colorMap = new Map;
var nextColorCount = 0;

function pickColor(object) {
    let color = colorMap[object];
    if (color !== undefined) {
        return color;
    }
    color = nextColor();
    colorMap[object] = color;
    return color;
}

function nextColor() {
    let color;
    let colorIndex = nextColorCount % SEQUENCE_1.length;
    let shadeIndex = Math.floor(nextColorCount / SEQUENCE_1.length);
    if (shadeIndex === 0) {
        color = SEQUENCE_1[colorIndex];
    } else if (shadeIndex === 1) {
        color = SEQUENCE_2[colorIndex];
    } else {
        shadeIndex -= 3;
        let floorColor = SEQUENCE_2[colorIndex];
        let ceilColor = SEQUENCE_1[colorIndex];
        let base = Math.floor((shadeIndex / 2) + 1);
        let divisor = 2;
        while (base >= divisor) {
            divisor *= 2;
        }
        base = (base * 2) - divisor + 1;
        let shadePercentage = base / divisor;
        color = buildPercentageColor(floorColor, ceilColor, shadePercentage);
    }
    nextColorCount++;
    return "#" + color.toString(16);
}

function buildPercentageColor(floorColor, ceilColor, shadePercentage) {
    let red = (floorColor & 0xFF0000) + Math.floor(shadePercentage * ((ceilColor & 0xFF0000) - (floorColor & 0xFF0000))) & 0xFF0000;
    let green = (floorColor & 0x00FF00) + Math.floor(shadePercentage * ((ceilColor & 0x00FF00) - (floorColor & 0x00FF00))) & 0x00FF00;
    let blue = (floorColor & 0x0000FF) + Math.floor(shadePercentage * ((ceilColor & 0x0000FF) - (floorColor & 0x0000FF))) & 0x0000FF;
    return red | green | blue;
}
