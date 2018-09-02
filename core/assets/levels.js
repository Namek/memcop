JSON.stringify([

    // there are no broken sectors, copy the memory once
    function() {
        var w = 12, h = 15, outputSize = w * h, inputSize = w * 4;

        return {
            name: "My first Transputer",
            goalDescription: "Copy " + inputSize + " source numbers ",

            memories: [{
                title: "from Cartridge #529",
                sectorsPerRow: w,
                size: inputSize,
                canReadFrom: true
            }, {
                title: "to disk",
                sectorsPerRow: w,
                size: outputSize,
                canWriteTo: true,
                canWriteToSpecificIndex: true
            }],
            memoriesLayout: [
                [0],
                [1]
            ],
            validators: [{
                inputMemIndex: 0,
                outputMemIndex: 1,
                indexStartInput: 0,
                indexStartOutput: 0,
                copyLength: inputSize,
                copyTimes: 1
            }]
        };
    }(),

    // there are no broken sectors, copy same memory 3 times
    function() {
        var w = 12, h = 15, outputSize = w * h, inputSize = w * 4;

        return {
            name: "3 times Basics",
            goalDescription: "Copy " + inputSize + " source numbers 3 times",

            memories: [{
                title: "from Cartridge #529",
                sectorsPerRow: w,
                size: inputSize,
                canReadFrom: true
            }, {
                title: "to disk",
                sectorsPerRow: w,
                size: outputSize,
                canWriteTo: true,
                canWriteToSpecificIndex: true
            }],
            memoriesLayout: [
                [0],
                [1]
            ],
            validators: [{
                inputMemIndex: 0,
                outputMemIndex: 1,
                indexStartInput: 0,
                indexStartOutput: 0,
                copyLength: inputSize,
                copyTimes: 3
            }]
        };
    }(),

    // there is exactly one broken sector
    function() {
        var w = 12, h = 15, outputSize = w * h, inputSize = w * 4;

        return {
            name: "Broken",
            goalDescription: "Copy " + inputSize + " source numbers without errors",

            memories: [{
                title: "from Cartridge #530",
                sectorsPerRow: w,
                size: inputSize,
                canReadFrom: true
            }, {
                title: "to disk",
                sectorsPerRow: w,
                size: outputSize,
                canWriteTo: true,
                canWriteToSpecificIndex: true,
                brokenSectorIndices: [inputSize]
            }],
            memoriesLayout: [
                [0],
                [1]
            ],
            validators: [{
                inputMemIndex: 0,
                outputMemIndex: 1,
                indexStartInput: 0,
                indexStartOutput: 0,
                copyLength: inputSize,
                copyTimes: 3
            }]
        };
    }(),

    // TODO there is easy (one-if) pattern in broken sectors
    function() {
        var w = 12, h = 15, outputSize = w * h, inputSize = w * 4;

        var brokenSectorIndices = []
        var i = inputSize
        while (i < outputSize) {
            brokenSectorIndices.push(i)
            i += inputSize
        }

        return {
            name: "The bad ones",
            goalDescription: "Copy " + inputSize + " source numbers without errors",

            memories: [{
                title: "from Cartridge #531",
                sectorsPerRow: w,
                size: inputSize,
                canReadFrom: true
            }, {
                title: "to disk",
                sectorsPerRow: w,
                size: outputSize,
                canWriteTo: true,
                canWriteToSpecificIndex: true,
                brokenSectorIndices: brokenSectorIndices
            }],
            memoriesLayout: [
                [0],
                [1]
            ],
            validators: [{
                inputMemIndex: 0,
                outputMemIndex: 1,
                indexStartInput: 0,
                indexStartOutput: 0,
                copyLength: inputSize,
                copyTimes: 3
            }]
        };
    }(),

    // there is a pattern in broken sectors
    function() {
        var w = 12, h = 15, outputSize = w * h, inputSize = w * 4;

        var brokenSectorIndices = []

        var y = 0
        var padLeft = false
        var brokenSectors = 0

        do {
            var startX = 1
            y += 1

            if (padLeft)
                startX += 1

            var i = y * w + startX
            if (i >= outputSize) break
            brokenSectorIndices.push(i)
            brokenSectors += 1

            i += 3
            if (i >= outputSize) break
            brokenSectorIndices.push(i)
            brokenSectors += 1

            i += 5
            if (i >= outputSize) break
            brokenSectorIndices.push(i)
            brokenSectors += 1

            y += 1
            padLeft = !padLeft
        } while (y < h)

        return {
            name: "The Pattern",
            goalDescription: "Copy " + inputSize + " source numbers 3 times",

            memories: [{
                title: "from Cartridge #532",
                sectorsPerRow: w,
                size: inputSize,
                canReadFrom: true
            }, {
                title: "to disk",
                sectorsPerRow: w,
                size: outputSize,
                canWriteTo: true,
                canWriteToSpecificIndex: true,
                brokenSectorIndices: brokenSectorIndices
            }],
            memoriesLayout: [
                [0],
                [1]
            ],
            validators: [{
                inputMemIndex: 0,
                outputMemIndex: 1,
                indexStartInput: 0,
                indexStartOutput: 0,
                copyLength: inputSize,
                copyTimes: 3
            }]
        };
    }(),
])