function createLevel0(name) {
    var w = 12, h = 15, outputSize = w * h, inputSize = w * 4

    return {
        _w: w,
        _h: h,

        name: name,
        goalDescription: "Copy " + inputSize + " source numbers ",

        memories: [{
            title: "from Cartridge #528",
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
    }
}

return JSON.stringify([
    // there are no broken sectors, copy the memory once
    function() {
        return createLevel0("My first Transputer")
    }(),

    // there are no broken sectors, copy same memory 3 times
    function() {
        var l = createLevel0("3 times Basics")
        var inputSize = l.memories[0].size
        l.validators[0].copyTimes = 3
        l.goalDescription = "Copy " + inputSize + " source numbers 3 times"
        l.memories[0].title = "from Cartridge #529"

        return l
    }(),

    // there is exactly one broken sector
    function() {
        var l = createLevel0("Broken");
        var inputSize = l.memories[0].size
        l.validators[0].copyTimes = 3
        l.goalDescription = "Copy " + inputSize + " source numbers without errors"
        l.memories[0].title = "from Cartridge #530"
        l.memories[1].brokenSectorIndices = [inputSize]

        return l
    }(),

    // TODO there is easy (one-if) pattern in broken sectors
    function() {
        var l = createLevel0("The bad ones")
        var inputSize = l.memories[0].size
        var outputSize = l.memories[1].size
        l.validators[0].copyTimes = 3
        l.goalDescription = "Copy " + inputSize + " source numbers without errors"
        l.memories[0].title = "from Cartridge #531"
        l.memories[1].brokenSectorIndices = [inputSize]
        l.brokenSectorIndices = []

        var i = inputSize
        while (i < outputSize) {
            l.brokenSectorIndices.push(i)
            i += inputSize
        }

        return l
    }(),

    // there is a pattern in broken sectors
    function() {
        var l = createLevel0("The Pattern")
        var inputSize = l.memories[0].size
        var outputSize = l.memories[1].size
        l.validators[0].copyTimes = 3
        l.goalDescription = "Copy " + inputSize + " source numbers 3 times"
        l.memories[0].title = "from Cartridge #532"
        l.memories[1].brokenSectorIndices = [inputSize]
        l.brokenSectorIndices = []

        var y = 0
        var padLeft = false
        var brokenSectors = 0

        do {
            var startX = 1
            y += 1

            if (padLeft)
                startX += 1

            var i = y * l._w + startX
            if (i >= outputSize) break
            l.brokenSectorIndices.push(i)
            brokenSectors += 1

            i += 3
            if (i >= outputSize) break
            l.brokenSectorIndices.push(i)
            brokenSectors += 1

            i += 5
            if (i >= outputSize) break
            l.brokenSectorIndices.push(i)
            brokenSectors += 1

            y += 1
            padLeft = !padLeft
        } while (y < l._h)

        return l
    }(),
])