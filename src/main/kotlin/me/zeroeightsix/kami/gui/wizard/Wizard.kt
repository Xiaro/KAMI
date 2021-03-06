package me.zeroeightsix.kami.gui.wizard

import imgui.ImGui
import imgui.ImGui.checkbox
import imgui.ImGui.dummy
import imgui.ImGui.openPopup
import imgui.ImGui.popStyleColor
import imgui.ImGui.popStyleVar
import imgui.ImGui.pushStyleColor
import imgui.ImGui.pushStyleVar
import imgui.ImGui.sameLine
import imgui.ImGui.separator
import imgui.ImGui.setNextWindowPos
import imgui.ImGui.text
import imgui.ImGui.textWrapped
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags
import imgui.internal.ImGui.popItemFlag
import imgui.internal.ImGui.pushItemFlag
import imgui.internal.flag.ImGuiItemFlags
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zeroeightsix.kami.conditionalWrap
import me.zeroeightsix.kami.feature.FindSettings
import me.zeroeightsix.kami.feature.module.Aura
import me.zeroeightsix.kami.gui.ImguiDSL
import me.zeroeightsix.kami.gui.ImguiDSL.button
import me.zeroeightsix.kami.gui.ImguiDSL.popupModal
import me.zeroeightsix.kami.gui.ImguiDSL.radioButton
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.widgets.EnabledWidgets
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.gui.windows.modules.Modules

@FindSettings
object Wizard {

    @Setting
    var firstTime = true

    val pages = listOf(
        {
            text("Welcome to KAMI!")
            text("This wizard is going to take you through setting up the GUI to your liking.")
            dummy(10f, 10f)
            text("Everything set by the wizard can be manually changed later through the Settings menu.")
        },
        {
            text("Please select your preferred theme and font.")
            Settings.showThemeSelector()
            Settings.showFontSelector()

            pushStyleColor(ImGuiCol.Text, .7f, .7f, .7f, 1f)
            text("GUI is visible in the background")
            popStyleColor()

            KamiGuiScreen.renderGui() // Show the full GUI
        },
        {
            text("How do you want your module windows to be set up?")

            radioButton("Per category", Modules.preferCategoryWindows) {
                Modules.preferCategoryWindows = true
                Modules.windows = Modules.getDefaultWindows()
            }
            radioButton("Everything in one window", !Modules.preferCategoryWindows) {
                Modules.preferCategoryWindows = false
                Modules.windows = Modules.getDefaultWindows()
            }

            pushStyleColor(ImGuiCol.Text, .7f, .7f, .7f, 1f)
            textWrapped(
                "The module windows in KAMI are fully customizable. If neither choice appeals to you, you can manually reorganise the module windows through the module window editor."
            )
            textWrapped("The module window editor may be accessed through the `View` menu in the top menu bar.")
            popStyleColor()
        },
        {
            text("Would you rather have settings appear in a popup, or embedded in the modules window?")

            separator()

            radioButton("In a popup", Settings.openSettingsInPopup) {
                Settings.openSettingsInPopup = true
            }
            radioButton("Embedded in the modules window", !Settings.openSettingsInPopup) {
                Settings.openSettingsInPopup = false
            }

            if (!Settings.openSettingsInPopup) {
                separator()

                text("Would you rather left-click or right-click a module to toggle it?")
                text("The other button will toggle its settings.")

                separator()

                radioButton("Left-click to toggle modules", Settings.swapModuleListButtons) {
                    Settings.swapModuleListButtons = true
                }
                radioButton("Right-click to toggle modules", !Settings.swapModuleListButtons) {
                    Settings.swapModuleListButtons = false
                }
            }

            separator()

            pushStyleColor(ImGuiCol.Text, .7f, .7f, .7f, 1f)
            val leftToggle = Settings.openSettingsInPopup || Settings.swapModuleListButtons
            text(
                "${
                if (leftToggle) {
                    "Left"
                } else {
                    "Right"
                }
                }-click to toggle, ${
                if (!leftToggle) {
                    "left"
                } else {
                    "right"
                }
                }-click to open settings."
            )
            text("Try it out:")
            popStyleColor()

            Modules.module(Aura, Modules.ModuleWindow("", Aura), "", Settings.moduleAlignment)

            separator()
        },
        {
            text("Should KAMI enable usage of modifier keys in binds?")
            text("Enabling this will make pressing e.g. 'Q' different from 'CTRL+Q'.")
            textWrapped("This has the sometimes unintended side effect of e.g. being unable to toggle a module while sneaking, if sneaking is bound to a modifier key.")
            ImguiDSL.checkbox("Enable modifier keys", Settings::modifiersEnabled)

            separator()

            pushStyleColor(ImGuiCol.Text, .7f, .7f, .7f, 1f)
            text("Assuming 'K' is bound to Aura,")
            text("And 'CTRL+Q' is bound to Brightness,")
            dummy(10f, 10f)
            val not = if (Settings.modifiersEnabled) " NOT " else " "
            text("Pressing K WILL toggle Aura.")
            text("Pressing SHIFT+K WILL${not}toggle Aura.")
            dummy(10f, 10f)
            text("Pressing CTRL+Q WILL toggle Brightness.")
            text("Pressing Q WILL${not}toggle Brightness.")
            popStyleColor()

            separator()
        },
        {
            text("How far from the edge should HUD elements be rendered?")
            Settings.showBorderOffsetSlider()
            separator()
            text("Which elements should be shown in the HUD?")
            EnabledWidgets.enabledButtons()
            separator()
            KamiGuiScreen.showWidgets(false)
        },
        {
            firstTime = false
        }
    )

    var currentPage = 0

    /**
     * Returns `true` if the wizard was opened
     */
    operator fun invoke(): Boolean {
        if (firstTime) {
            openPopup("Setup wizard")
            setNextWindowPos(
                ImGui.getIO().displaySizeX * 0.5f,
                ImGui.getIO().displaySizeY * 0.5f,
                ImGuiCond.Always,
                0.5f,
                0.5f
            )
            popupModal(
                "Setup wizard",
                extraFlags = ImGuiWindowFlags.AlwaysAutoResize or ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoMove
            ) {
                pages[currentPage]()
                (currentPage == 0).conditionalWrap(
                    {
                        pushItemFlag(ImGuiItemFlags.Disabled, true)
                        pushStyleVar(ImGuiStyleVar.Alpha, ImGui.getStyle().alpha * 0.5f)
                    },
                    {
                        button("Previous", 100f, 0f) {
                            currentPage--
                        }
                    },
                    {
                        popItemFlag()
                        popStyleVar()
                    }
                )
                sameLine()
                button("Next", 100f, 0f) {
                    currentPage++
                }
            }
        }

        return firstTime
    }
}