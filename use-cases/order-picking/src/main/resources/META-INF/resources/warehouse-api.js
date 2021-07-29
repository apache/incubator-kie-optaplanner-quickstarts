/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const LEFT = 'LEFT';
const RIGHT = 'RIGHT';

const WAREHOUSE_COLUMNS = ['A', 'B', 'C', 'D', 'E'];
const WAREHOUSE_ROWS = ['1', '2', '3'];

const WAREHOUSE_PADDING_TOP = 10;
const WAREHOUSE_PADDING_LEFT = 10;

const SHELVING_PADDING = 80;
const SHELVING_WIDTH = 150;
const SHELVING_HEIGHT = 300;
const SHELVING_ROWS = 10;

const SHELVING_LINE_WIDTH = 15;
const SHELVING_STROKE_STYLE = 'green';
const SHELVING_LINE_JOIN = 'bevel';

const TROLLEY_PATH_LINE_WIDTH = 5;
const TROLLEY_PATH_LINE_JOIN = 'round';

const SHELVINGS_MAP = new Map();

function shelvingId(i, j) {
    return '(' + i + ',' + j + ')';
}

class Point {
    x = 0;
    y = 0;

    constructor(x, y) {
        this.x = x;
        this.y = y;
    }
}

class ShelvingShape {
    id;
    x = 0;
    y = 0;
    width = 0;
    height = 0;

    constructor(id, x, y, width, height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}

class WarehouseLocation {
    shelvingId;
    side;
    row;

    constructor(shelvingId, side, row) {
        this.shelvingId = shelvingId;
        this.side = side;
        this.row = row;
    }
}

/**
 * Returns the WarehouseCanvas.
 */
function getWarehouseCanvasContext() {
    return document.getElementById('warehouseCanvas').getContext('2d');
}

/**
 * Clears the WarehouseCanvas.
 */
function clearWarehouseCanvas() {
    const canvas = document.getElementById('warehouseCanvas');
    const ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
}

/**
 * Draws the WarehouseStructure on the WarehouseCanvas.
 */
function drawWarehouse() {
    const ctx = getWarehouseCanvasContext();
    let x = WAREHOUSE_PADDING_LEFT;
    for (let column = 0; column < WAREHOUSE_COLUMNS.length; column++) {
        let y = WAREHOUSE_PADDING_TOP;
        for (let row = 0; row < WAREHOUSE_ROWS.length; row++) {
            const shelving = new ShelvingShape(shelvingId(WAREHOUSE_COLUMNS[column], WAREHOUSE_ROWS[row]), x, y, SHELVING_WIDTH, SHELVING_HEIGHT);
            drawShelving(ctx, shelving);
            SHELVINGS_MAP.set(shelving.id, shelving);
            y = y + SHELVING_HEIGHT + SHELVING_PADDING;
        }
        x = x + SHELVING_WIDTH + SHELVING_PADDING;
    }
}

/**
 * Draws a ShelvingShape on the Warehouse canvas.
 */
function drawShelving(ctx, shelving) {
    ctx.strokeStyle = SHELVING_STROKE_STYLE;
    ctx.lineJoin = SHELVING_LINE_JOIN;
    ctx.lineWidth = SHELVING_LINE_WIDTH;
    ctx.strokeRect(shelving.x, shelving.y, shelving.width, shelving.height);

    ctx.font = '30px serif';
    ctx.textAlign = 'center';
    ctx.fillStyle = SHELVING_STROKE_STYLE
    ctx.fillText(shelving.id, shelving.x + shelving.width / 2, shelving.y + shelving.height / 2);
}

/**
 * Draws the path travelled by a Trolley.
 */
function drawTrolleyPath(strokeStyle, warehouseLocations) {
    const ctx = getWarehouseCanvasContext();
    ctx.lineJoin = TROLLEY_PATH_LINE_JOIN;
    ctx.lineWidth = TROLLEY_PATH_LINE_WIDTH;
    ctx.strokeStyle = strokeStyle;
    drawWarehousePath(warehouseLocations);
}

/**
 * Draws a path composed of WarehouseLocations.
 */
function drawWarehousePath(warehouseLocations) {
    const ctx = getWarehouseCanvasContext();
    const startLocation = warehouseLocations[0];
    const startShelving = SHELVINGS_MAP.get(startLocation.shelvingId);
    const startPoint = location2Point(startShelving, startLocation.side, startLocation.row);

    ctx.beginPath();
    ctx.moveTo(startPoint.x, startPoint.y);
    ctx.arc(startPoint.x, startPoint.y, 5, 0, 2 * Math.PI);
    for (let i = 1; i < warehouseLocations.length; i++) {
        const location = warehouseLocations[i];
        const shelving = SHELVINGS_MAP.get(location.shelvingId);
        const point = location2Point(shelving, location.side, location.row);
        ctx.lineTo(point.x, point.y);
        ctx.arc(point.x, point.y, 5, 0, 2 * Math.PI);

    }
    ctx.stroke();
    ctx.closePath();
}

/**
 * Transforms a WarehouseLocation into an absolute Point in the canvas.
 */
function location2Point(shelving, side, row) {
    let x;
    let y;
    if (side === LEFT) {
        x = shelving.x;
    } else {
        x = shelving.x + shelving.width;
    }
    const rowWidth = shelving.height / SHELVING_ROWS;
    y = shelving.y + row * rowWidth;
    return new Point(x, y);
}


