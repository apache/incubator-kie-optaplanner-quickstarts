var autoRefreshIntervalId = null;

function refreshFactorioLayout() {
    $.getJSON("/factorioLayout", function (factorioLayout) {
        refreshSolvingButtons(factorioLayout.solverStatus != null && factorioLayout.solverStatus !== "NOT_SOLVING");
        $("#score").text("Score: "+ (factorioLayout.score == null ? "?" : factorioLayout.score));

        const areaAssemblies = $("#areaAssemblies");
        areaAssemblies.children().remove();
        const unassignedAssembliesDiv = $("#unassignedAssemblies");
        unassignedAssembliesDiv.children().remove();

        const recipeMap = new Map(factorioLayout.recipeList.map(recipe => [recipe.id, recipe]));
        const assemblyMap = new Map(factorioLayout.assemblyList.map(assembly => [assembly.id, assembly]));
        for (const assembly of factorioLayout.assemblyList) {
            assembly.recipe = recipeMap.get(assembly.recipe);
            let newInputAssemblyList = [];
            for (const inputAssembly of assembly.inputAssemblyList) {
                newInputAssemblyList.push(assemblyMap.get(inputAssembly));
            }
            assembly.inputAssemblyList = newInputAssemblyList;
        }

        const canvas = $(`<svg style="width:${factorioLayout.areaWidth * 50}px; height:${factorioLayout.areaHeight * 50}px;"/>`);
        canvas.append($(`<defs><marker id="arrowhead" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto"><polygon points="0 0, 10 3.5, 0 7" /></marker></defs>`));
        for (let x = 0; x < factorioLayout.areaWidth; x++) {
            for (let y = 0; y < factorioLayout.areaHeight; y++) {
                canvas.append($(`<rect x="${x * 50}" y="${y * 50}" width="50" height="50" stroke="gray" fill-opacity="0"/>`));
            }
        }

        let nextUnassignedX = 0;
        let nextUnassignedY = 0;
        for (const assembly of factorioLayout.assemblyList) {
            const color = pickColor(assembly.recipe.id);
            let x;
            let y;
            if (assembly.area == null) {
                x = nextUnassignedX;
                y = nextUnassignedY;
                nextUnassignedX++;
                if (nextUnassignedX >= factorioLayout.areaWidth) {
                    nextUnassignedX = 0;
                    nextUnassignedY++;
                }
            } else {
                x = assembly.area.x;
                y = assembly.area.y;
            }
            const assemblyElement = $(`<div class="img-thumbnail text-center" style="left:${x * 50}px; top:${y * 50}px; position:absolute; width:50px; height:50px; max-width:50px; max-height:50px; z-index: -1; background-color: ${color};"
                                            title="${assembly.recipe.name} ${assembly.id}"/>`)
                            .append($(`<img style="margin-top: 4px" src="${assembly.recipe.imageUrl}" alt="${assembly.recipe.name}"/>`));
            if (assembly.area == null) {
                unassignedAssembliesDiv.append(assemblyElement);
            } else {
                areaAssemblies.append(assemblyElement);
                for (const inputAssembly of assembly.inputAssemblyList) {
                    if (inputAssembly.area != null) {
                        let inputX = inputAssembly.area.x;
                        let inputY = inputAssembly.area.y;
                        canvas.append($(`<line x1="${inputX * 50 + (inputX < x ? 40 : 10)}" y1="${inputY * 50 + (inputY < y ? 40 : 10)}" x2="${x * 50 + (inputX < x ? 10 : 40)}" y2="${y * 50 + (inputY < y ? 10 : 40)}" stroke="black" marker-end="url(#arrowhead)"/>`));
                    }
                }
            }
        }
        // Workaround because JQuery cannot manipulate SVG DOM
        areaAssemblies.append(canvas.prop('outerHTML'));
    });
}

function solve() {
    $.post("/factorioLayout/solve", function () {
        refreshSolvingButtons(true);
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Start solving failed.", xhr);
    });
}

function refreshSolvingButtons(solving) {
    if (solving) {
        $("#solveButton").hide();
        $("#stopSolvingButton").show();
        if (autoRefreshIntervalId == null) {
            autoRefreshIntervalId = setInterval(refreshFactorioLayout, 2000);
        }
    } else {
        $("#solveButton").show();
        $("#stopSolvingButton").hide();
        if (autoRefreshIntervalId != null) {
            clearInterval(autoRefreshIntervalId);
            autoRefreshIntervalId = null;
        }
    }
}

function stopSolving() {
    $.post("/factorioLayout/stopSolving", function () {
        refreshSolvingButtons(false);
        refreshFactorioLayout();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Stop solving failed.", xhr);
    });
}

function showError(title, xhr) {
    const serverErrorMessage = !xhr.responseJSON ? `${xhr.status}: ${xhr.statusText}` : xhr.responseJSON.message;
    console.error(title + "\n" + serverErrorMessage);
    const notification = $(`<div class="toast" role="alert" aria-live="assertive" aria-atomic="true" style="min-width: 30rem"/>`)
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
    jQuery.each(["put", "delete"], function(i, method) {
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


    $("#refreshButton").click(function() {
        refreshFactorioLayout();
    });
    $("#solveButton").click(function() {
        solve();
    });
    $("#stopSolvingButton").click(function() {
        stopSolving();
    });

    refreshFactorioLayout();
});

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
