package net.namekdev.memcop.domain.pojo

data class LevelInfo(
    val name: String,
    val goalDescription: String,
    val memories: List<MemorySourceInfo>,
    val memoriesLayout: List<List<Int>> = listOf(listOf(0), listOf(1)),
    val validators: List<CopyValidatorInfo>,
    val shouldCopyCodeSolutionFromPreviousLevel: Boolean = true
)

data class MemorySourceInfo(
    val title: String,
    val sectorsPerRow: Int,
    val size: Int,
    val sectors: List<SectorInfo>?,
    val brokenSectorIndices: List<Int>?,
    val canReadFrom: Boolean = false,
    val canWriteTo: Boolean = false,
    val canWriteToSpecificIndex: Boolean = false,
    val canReadFromSpecificIndex: Boolean = false
)

data class SectorInfo(
    val isBroken: Boolean
)

data class CopyValidatorInfo(
    val inputMemIndex: Int,
    val outputMemIndex: Int,
    val indexStartInput: Int,
    val indexStartOutput: Int,
    val copyLength: Int,
    val copyTimes: Int
)
