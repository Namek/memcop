package net.namekdev.memcop.view

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.github.czyzby.lml.annotation.*
import com.github.czyzby.lml.parser.LmlParser
import com.github.czyzby.lml.parser.impl.AbstractLmlView
import com.kotcrab.vis.ui.widget.HighlightTextArea
import net.namekdev.memcop.domain.Assembly
import net.namekdev.memcop.domain.Assembly.AssemblyCompilationError
import net.namekdev.memcop.domain.Level
import net.namekdev.memcop.domain.LevelFactory
import net.namekdev.memcop.domain.Transputer
import net.namekdev.memcop.view.widgets.LmlSourceHighlighter

import java.util.ArrayList
import java.util.TreeMap

class GameView(_stage: Stage) : AbstractLmlView(_stage) {
    private var parser: LmlParser? = null

    @LmlActor("btnRun") lateinit var btnRun: TextButton
    @LmlActor("btnDebug") lateinit var btnDebug: TextButton
    @LmlActor("btnStop") lateinit var btnStop: TextButton
    @LmlActor("btnShowGoal") lateinit var btnShowGoal: TextButton

    @get:LmlAction("scale") val scale = Render.scale
    @get:LmlAction("codeInput") val codeInput = HighlightTextArea("", "codeTextArea")

    lateinit var skin: Skin


    internal var level = LevelFactory.create(4)
    internal var transputer: Transputer
    internal var registerValueLabels: MutableMap<String, Label> = TreeMap()

    val levelName: String
        @LmlAction("levelName")
        get() = level.name

    val goalDescription: String
        @LmlAction("goalDescription")
        get() = level.goalDescription

    val registerList: List<String>
        @LmlAction("registers")
        get() {
            val names = ArrayList<String>()

            for (reg in transputer.registerStates) {
                names.add(reg.info.name.replace("$", ""))
            }
            return names
        }


    init {
        transputer = Transputer()
        transputer.sourceMemory = level.inputMem
        transputer.destMemory = level.outputMem
        transputer.reset()

        codeInput.highlighter = LmlSourceHighlighter()
        codeInput.messageText = "If you need help... hit F1"
        codeInput.isFocusBorderEnabled = false
        codeInput.color.a = 0.6f
    }

    @LmlBefore
    fun beforeParse(parser: LmlParser) {
        this.parser = parser
        this.skin = parser.data.defaultSkin
    }

    @LmlAfter
    fun afterParse() {
        btnShowGoal.addListener(object : InputListener() {
            override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                MemorySourceRenderer.drawGoal = true
                super.enter(event, x, y, pointer, fromActor)
            }

            override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                MemorySourceRenderer.drawGoal = false
                super.exit(event, x, y, pointer, toActor)
            }

            override fun mouseMoved(event: InputEvent?, x: Float, y: Float): Boolean {
                MemorySourceRenderer.drawGoal = true
                return super.mouseMoved(event, x, y)
            }
        })

        codeInput.addListener(object : InputListener() {
            override fun keyTyped(event: InputEvent?, character: Char): Boolean {
                onCodeEdited()
                return super.keyTyped(event, character)
            }
        })
    }


    override fun show() {
        val code = "" +
            "mov 5 \$a\n" +
            "mov 0 \$b\n" +
            "_start:\n" +
            "ldi \$c\n" +
            "sti \$c \$b\n" +
            "inc \$b\n" +
            "cmp \$b \$a\n" +
            "jge _start\n" +
            "sub \$b 2 \$b\n" + //expecting b = 4

            "\n" +
            "_end:\n" +
            "mov \$b \$c"

        codeInput.text = code

        compileCode()
        updateAllUi()
    }

    override fun getTemplateFile(): FileHandle {
        return Gdx.files.internal("views/first.lml")
    }

    override fun getViewId(): String {
        return "first"
    }


    @LmlAction("codeCursor")
    fun createCodeCursor(): Actor {
        return CodeCursor(codeInput, transputer)
    }

    @LmlAction("inputMemActor")
    fun createInputMemActor(): Actor {
        return MemorySourceRenderer(level.inputMem)
    }

    @LmlAction("outputMemActor")
    fun createOutputMemActor(): Actor {
        return MemorySourceRenderer(level.outputMem)
    }

    @LmlAction("createRegisterValueLabel")
    fun createRegisterValueLabel(name: String): Actor {
        val label = Label("0", skin, "code-like")
        registerValueLabels["$$name"] = label

        return label
    }


    /**
     * Simulates whole program.
     * If program is in the middle and no code was changed then continue without reset.
     */
    @LmlAction("run")
    fun run() {
        if (transputer.lastInstruction == null) {
            reset()
            compileCode()
        }

        while (transputer.step()) {
            updateRegisters()
        }
        updateAllUi()
    }

    /**
     * Start debugging by going step by step.
     */
    @LmlAction("debug")
    fun debug() {
        if (!transputer.isDebuggingActive) {
            transputer.startDebugging()
        }
        else {
            transputer.step()
        }
        updateAllUi()
    }

    @LmlAction("stop")
    fun reset() {
        transputer.reset()
        level.reset()
        updateAllUi()
    }

    private fun onCodeEdited() {
        assert(!transputer.isDebuggingActive)

        // TODO
    }

    private fun compileCode() {
        try {
            val code = codeInput.text
            transputer.instructions = Assembly.compile(code, level)
        } catch (assemblyCompilationError: AssemblyCompilationError) {
            assemblyCompilationError.printStackTrace()
        }

    }

    private fun updateAllUi() {
        updateButtonTexts()
        updateRegisters()
        codeInput.isDisabled = transputer.isDebuggingActive
    }

    private fun updateButtonTexts() {
        val isDebuggingActive = transputer.isDebuggingActive
        val noMoreInstructions = isDebuggingActive && transputer.nextInstruction == null

        btnRun.setText(if (isDebuggingActive) "\ue801" else "\ue800")
        btnRun.isDisabled = noMoreInstructions
        btnDebug.setText(if (isDebuggingActive) "\ue806" else "\uf188")
        btnDebug.isDisabled = noMoreInstructions
        btnStop.setText("\ue802")
        btnStop.isDisabled = !isDebuggingActive
    }

    private fun updateRegisters() {
        for (reg in transputer.registerStates) {
            val valueLabel = registerValueLabels[reg.info.name]!!
            valueLabel.setText(reg.value.toString())
        }
    }
}