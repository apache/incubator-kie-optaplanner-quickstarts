var autoRefreshCount = 0;
var autoRefreshIntervalId = null;

// Initial date/time (next Monday)
var initialDateTime = moment().startOf('isoWeek').add(1, 'weeks').set('hour', 9);

// DOM element where the Timeline will be attached
var employeeContainer = document.getElementById('employeeVisualization');
var assignedMaintenanceJobs = new vis.DataSet();
var crewGroups = new vis.DataSet();

// Manager timeline element
var managerContainer = document.getElementById('managerVisualization');
var maintenanceJobReadyDueTimes = new vis.DataSet();
var unitAndJobGroups = new vis.DataSet();

// Configuration for the employee timeline
var employeeTimelineOptions = {
    // Make jobs editable through UI
    // editable: true,

    // Hide weekends using any weekend and repeat weekly
    hiddenDates: [{
        start: '2020-02-29 00:00:00',
        end: '2020-03-02 00:00:00',
        repeat: 'weekly'
    },
    // Hide non-working hours outside of 9am to 5pm using any two days and repeat daily
    {
        start: '2020-02-29 17:00:00',
        end: '2020-03-02 09:00:00',
        repeat: 'daily'
    }
    ],

    // Always snap to full hours, independent of the scale
    snap: function (date, scale, step) {
        var hour = 60 * 60 * 1000;
        return Math.round(date / hour) * hour;
    },

    // Prevent jobs from stacking in timeline
    stack: false
};

// Configuration for the manager timeline
var managerTimelineOptions = {
    // Make jobs editable through UI
    // editable: true,

    // Hide weekends using any weekend and repeat weekly
    hiddenDates: [{
        start: '2020-02-29 00:00:00',
        end: '2020-03-02 00:00:00',
        repeat: 'weekly'
    },
    // Hide non-working hours outside of 9am to 5pm using any two days and repeat daily
    {
        start: '2020-02-29 17:00:00',
        end: '2020-03-02 09:00:00',
        repeat: 'daily'
    }
    ],

    // Prevent jobs from stacking in timeline
    stack: false
};

// Create timelines displaying maintenance jobs
var employeeTimeline = new vis.Timeline(employeeContainer, assignedMaintenanceJobs, crewGroups,
    employeeTimelineOptions);
var managerTimeline = new vis.Timeline(managerContainer, maintenanceJobReadyDueTimes, unitAndJobGroups,
    managerTimelineOptions);

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
        refreshSchedule();
    });
    $("#solveButton").click(function () {
        solve();
    });
    $("#stopSolvingButton").click(function () {
        stopSolving();
    });
    $("#employeeViewTab").click(function () {
        setTimeout(() => { refreshSchedule() }, 500);
    });
    $("#managerViewTab").click(function () {
        setTimeout(() => { refreshSchedule() }, 500);
    });
    $("#addJobButton").click(function () {
        dropdownMaintenanceUnits();
    });

    refreshSchedule();
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
    refreshSchedule();
    autoRefreshCount--;
    if (autoRefreshCount <= 0) {
        clearInterval(autoRefreshIntervalId);
        autoRefreshIntervalId = null;
    }
}

function stopSolving() {
    $.post("/schedule/stopSolving", function () {
        refreshSolvingButtons(false);
        refreshSchedule();
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

function refreshSchedule() {
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

        // Map jobs to units
        var unitToJobs = {};

        // Add a nested group for each job under its unit
        unitAndJobGroups.clear();
        $.each(schedule.maintenanceJobList, (index, job) => {
            unitAndJobGroups.add({
                id: job.id,
                content: job.jobName
            });

            // Map job to unit
            if (unitToJobs[job.maintainableUnit.unitName]) {
                unitToJobs[job.maintainableUnit.unitName].push(job.id)
            } else {
                unitToJobs[job.maintainableUnit.unitName] = [job.id]
            }
        });

        // Add a group for each unit
        $.each(schedule.maintainableUnitList, (index, unit) => {
            unitAndJobGroups.add({
                id: unit.id,
                content: `Vehicle ` + unit.unitName,
                nestedGroups: unitToJobs[unit.unitName]
            });
        });

        // Label each mutually exclusive job with tag
        const jobToMutuallyExclusiveTagMap = {};
        $.each(schedule.mutuallyExclusiveJobsList, (index, mutuallyExclusiveJobs) => {
            $.each(mutuallyExclusiveJobs.mutuallyExclusiveJobList, (index, job) => {
                if (jobToMutuallyExclusiveTagMap[job.jobName] == null) {
                    jobToMutuallyExclusiveTagMap[job.jobName] = [mutuallyExclusiveJobs.exclusiveTag];
                }
                else {
                    jobToMutuallyExclusiveTagMap[job.jobName].push(mutuallyExclusiveJobs.exclusiveTag);
                }
            });
        });

        assignedMaintenanceJobs.clear();
        maintenanceJobReadyDueTimes.clear();
        var managerTimelineFocusIds = [];
        $.each(schedule.maintenanceJobList, (index, job) => {
            if (job.assignedCrew != null && job.startingTimeGrain != null) {
                var startDateTime = moment(initialDateTime).add(job.startingTimeGrain.grainIndex, `hours`);
                var endDateTime = moment(initialDateTime).add(job.startingTimeGrain.grainIndex + 
                    job.durationInGrains, `hours`);

                // Display assigned job in employee view timeline
                assignedMaintenanceJobs.add({
                    id: job.id,
                    group: job.assignedCrew.id,
                    content: `<b>` + job.jobName + `</b><br><i>at ` + startDateTime.format(`HH:mm`) + `</i><br>` + 
                        job.maintainableUnit.unitName,
                    start: startDateTime,
                    end: endDateTime
                });

                // Display assigned job in manager view timeline
                maintenanceJobReadyDueTimes.add({
                    id: job.id,
                    group: job.id,
                    content: `<b>` + job.assignedCrew.crewName + `</b><br><i> at ` + 
                        startDateTime.format(`HH:mm`) + `</i><br>`,
                    style: `background-color: lightgreen`,
                    start: startDateTime,
                    end: endDateTime
                });

                managerTimelineFocusIds.push(job.id);
            }
            else {
                const unassignedJobElement = $(`<div class="card bg-secondary"/>`)
                    .append($(`<div class="card-body p-2"/>`)
                    .append($(`<b h6 class="card-title"/>`).text(job.jobName))
                    .append($(`<br><span class="badge" style="background-color: white"/>`)
                        .text(job.maintainableUnit.unitName)));
                // Append mutually exclusive tags on jobs
                $.each(jobToMutuallyExclusiveTagMap[job.jobName], (index, mutuallyExclusiveTag) => {
                    const color = pickColor(mutuallyExclusiveTag);
                    unassignedJobElement.append($(`<span class="badge badge-info m-2" style="background-color: ${color}"/>`)
                        .text(mutuallyExclusiveTag));
                });
                unassignedJobs.append(unassignedJobElement);

                // Display unassigned job in manager view timeline
                maintenanceJobReadyDueTimes.add({
                    id: job.id,
                    group: job.id,
                    content: `<b>` + job.jobName + `</b><br><i>Unassigned</i><br>`,
                    style: `background-color: red`,
                    start: moment(initialDateTime).add(job.readyTimeGrainIndex, `hours`)
                        .add((job.dueTimeGrainIndex - job.readyTimeGrainIndex) * 1/3, `hours`),
                    end: moment(initialDateTime).add(job.readyTimeGrainIndex, `hours`)
                        .add((job.dueTimeGrainIndex - job.readyTimeGrainIndex) * 2/3, `hours`)
                })
                managerTimelineFocusIds.push(job.id);
            }
            maintenanceJobReadyDueTimes.add([
                // Ready/due date
                {
                    group: job.id,
                    content: ``,
                    type: `background`,
                    style: `background-color: lightgrey`,
                    start: moment(initialDateTime).subtract(1, `years`),
                    end: moment(initialDateTime).add(job.readyTimeGrainIndex, `hours`)
                },
                {
                    group: job.id,
                    content: ``,
                    type: `background`,
                    style: `background-color: lightgrey`,
                    start: moment(initialDateTime).add(job.dueTimeGrainIndex, `hours`),
                    end: moment(initialDateTime).add(1, `years`)
                },
                // Safety margin
                {
                    group: job.id,
                    content: ``,
                    type: `background`,
                    style: `background-color: orange`,
                    start: moment(initialDateTime).add(job.dueTimeGrainIndex - job.safetyMarginDurationInGrains, `hours`),
                    end: moment(initialDateTime).add(job.dueTimeGrainIndex, `hours`)
                }
            ]);
        });

        employeeTimeline.fit();
        managerTimeline.focus(managerTimelineFocusIds);
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
