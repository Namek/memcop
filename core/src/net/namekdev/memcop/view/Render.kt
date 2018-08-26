package net.namekdev.memcop.view

object Render {
    val logicalWidth = 900
    val logicalHeight = 600
    var scale = 1f

    val width: Float
        get() = logicalWidth * scale

    val height: Float
        get() = logicalHeight * scale
}