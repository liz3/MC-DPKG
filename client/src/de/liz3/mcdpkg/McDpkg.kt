package de.liz3.mcdpkg

import de.liz3.mcdpkg.core.Manager
import de.liz3.mcdpkg.include.Request
import de.liz3.mcdpkg.include.RequestType
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*

class McDpkg : JavaPlugin() {

    val config = Config(this)
    val manager = Manager(this)

    override fun onEnable() {

        getCommand("pkg").executor = DpkgCommand(this)


    }

    override fun onDisable() {
        super.onDisable()
    }
}

class DpkgCommand(val main: McDpkg) : CommandExecutor {


    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {



        val action = args?.first()



        if(action == "update") {
            main.manager.executeRequest(Request(sender!!, RequestType.UPDATE))
            return true
        }

        val packages = Vector<String>()
        var reload = false
        var backports = false

        for (arg in args!!) {
            if(arg == action) continue

            if(arg == "-reload") {
                reload = true
                continue
            }
            if(arg == "-backports") {
                backports = true
                continue
            }
            packages.addElement(arg)
        }

        if(action.equals("search", true)) {
            main.manager.executeRequest(Request(sender!!, RequestType.SEARCH, packages, backports, reload))
            return true
        }
        if(action.equals("install", true)) {
            main.manager.executeRequest(Request(sender!!, RequestType.INSTALL, packages, backports, reload))
            return true
        }
        return false
    }


}