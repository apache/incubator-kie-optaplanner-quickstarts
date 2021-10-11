var autoRefreshCount = 0;
var autoRefreshIntervalId = null;

// Initial date/time (next Monday)
var initialDateTime = moment().startOf('isoWeek').add(1, 'weeks').set('hour', 9);

// DOM element where the Timeline will be attached
var employeeContainer = document.getElementById('employeeVisualization');
var assignedMaintenanceJobsVisDS = new vis.DataSet();
var crewGroupsVisDS = new vis.DataSet();

// Manager timeline element
var managerContainer = document.getElementById('managerVisualization');
var maintenanceJobReadyDueTimesVisDS = new vis.DataSet();
var unitAndJobGroupsVisDS = new vis.DataSet();

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
var employeeTimeline = new vis.Timeline(employeeContainer, assignedMaintenanceJobsVisDS, crewGroupsVisDS,
    employeeTimelineOptions);
var managerTimeline = new vis.Timeline(managerContainer, maintenanceJobReadyDueTimesVisDS, unitAndJobGroupsVisDS,
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
        crewGroupsVisDS.clear();
        $.each(schedule.assignedCrewList, (index, crew) => {
            crewGroupsVisDS.add({
                id: crew.id,
                content: crew.crewName
            });
        });

        // Map jobs to units
        var unitToJobs = {};

        // Add a nested group for each job under its unit
        unitAndJobGroupsVisDS.clear();
        $.each(schedule.maintenanceJobAssignmentList, (index, jobAssignment) => {
            var maintenanceJob = jobAssignment.maintenanceJob;
            unitAndJobGroupsVisDS.add({
                id: jobAssignment.id,
                content: maintenanceJob.jobName
            });

            // Map job to unit
            if (unitToJobs[maintenanceJob.maintainableUnit.unitName]) {
                unitToJobs[maintenanceJob.maintainableUnit.unitName].push(jobAssignment.id)
            } else {
                unitToJobs[maintenanceJob.maintainableUnit.unitName] = [jobAssignment.id]
            }
        });

        // Add a group for each unit
        $.each(schedule.maintainableUnitList, (index, unit) => {
            unitAndJobGroupsVisDS.add({
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

        assignedMaintenanceJobsVisDS.clear();
        maintenanceJobReadyDueTimesVisDS.clear();
        var managerTimelineFocusIds = [];
        $.each(schedule.maintenanceJobAssignmentList, (index, jobAssignment) => {
            var maintenanceJob = jobAssignment.maintenanceJob;
            if (jobAssignment.assignedCrew != null && jobAssignment.startingTimeGrain != null) {
                var startDateTime = moment(initialDateTime).add(jobAssignment.startingTimeGrain.grainIndex, `hours`);
                var endDateTime = moment(initialDateTime).add(jobAssignment.startingTimeGrain.grainIndex +
                    maintenanceJob.durationInGrains, `hours`);

                // Display assigned job in employee view timeline
                assignedMaintenanceJobsVisDS.add({
                    id: jobAssignment.id,
                    group: jobAssignment.assignedCrew.id,
                    content: `<b>` + maintenanceJob.jobName + `</b><br><i>at ` +
                        startDateTime.format(`HH:mm`) + `</i><br>` +
                        maintenanceJob.maintainableUnit.unitName,
                    start: startDateTime,
                    end: endDateTime
                });

                // Display assigned job in manager view timeline
                maintenanceJobReadyDueTimesVisDS.add({
                    id: jobAssignment.id,
                    group: jobAssignment.id,
                    content: `<b>` + jobAssignment.assignedCrew.crewName + `</b><br><i> at ` +
                        startDateTime.format(`HH:mm`) + `</i><br>`,
                    style: `background-color: lightgreen`,
                    start: startDateTime,
                    end: endDateTime
                });

                managerTimelineFocusIds.push(jobAssignment.id);
            } else {

                const jobDiv = $(`<div class="card"/>`);
                const jobDivBody = $(`<div class="card-body p-2"/>`).appendTo(jobDiv);
                jobDivBody.append($(`<p class="card-title m-0"/>`).append($(`<b/>`).text(maintenanceJob.jobName)))
                        .append($(`<p class="card-text m-0"/>`).text(maintenanceJob.maintainableUnit.unitName));
                // Append mutually exclusive tags on jobs
                $.each(jobToMutuallyExclusiveTagMap[maintenanceJob.jobName],
                    (index, mutuallyExclusiveTag) => {
                        const color = pickColor(mutuallyExclusiveTag);
                        jobDivBody.append($(`<span class="badge" style="background-color: ${color}"/>`).text(mutuallyExclusiveTag));
                    });
                unassignedJobs.append(jobDiv);

                // Display unassigned job in manager view timeline
                maintenanceJobReadyDueTimesVisDS.add({
                    id: jobAssignment.id,
                    group: jobAssignment.id,
                    content: `<b>` + maintenanceJob.jobName + `</b><br><i>Unassigned</i><br>`,
                    style: `background-color: red`,
                    start: moment(initialDateTime).add(maintenanceJob.readyTimeGrainIndex, `hours`)
                        .add((maintenanceJob.dueTimeGrainIndex -
                            maintenanceJob.readyTimeGrainIndex) * 1 / 3, `hours`),
                    end: moment(initialDateTime).add(maintenanceJob.readyTimeGrainIndex, `hours`)
                        .add((maintenanceJob.dueTimeGrainIndex -
                            maintenanceJob.readyTimeGrainIndex) * 2 / 3, `hours`)
                })
                managerTimelineFocusIds.push(jobAssignment.id);
            }
            maintenanceJobReadyDueTimesVisDS.add([
                {
                    id: jobAssignment.id + "readyDue",
                    group: jobAssignment.id,
                    start: moment(initialDateTime).add(maintenanceJob.readyTimeGrainIndex, `hours`),
                    end: moment(initialDateTime).add(maintenanceJob.dueTimeGrainIndex, `hours`),
                    type: `background`,
                    style: `background-color: rgba(138, 226, 52, 0.2)`
                },
                {
                    id: jobAssignment.id + "_safetyMargin",
                    group: jobAssignment.id,
                    start: moment(initialDateTime).add(maintenanceJob.dueTimeGrainIndex -
                        maintenanceJob.safetyMarginDurationInGrains, `hours`),
                    end: moment(initialDateTime).add(maintenanceJob.dueTimeGrainIndex, `hours`),
                    type: `background`,
                    style: `background-color: rgba(252, 175, 62, 0.2)`,
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
