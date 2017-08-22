package de.liz3.mcdpkg.core

import de.liz3.mcdpkg.Http
import de.liz3.mcdpkg.McDpkg
import de.liz3.mcdpkg.include.Plugin
import org.bukkit.command.CommandSender
import java.io.File
import java.util.*

class InstallationManager(val main: McDpkg, val http: Http) {

    val plFolder = File(File(".").canonicalPath, "plugins")
    val pluginManager = main.server.pluginManager
    val toRemove = Vector<org.bukkit.plugin.Plugin>()


    fun install(wanted: Double, pkg: Plugin, sender: CommandSender) {

        for (pl in pluginManager.plugins) {
            if(pl.name.contains(pkg.name, true)) {
                //TODO
            }
        }
        if(wanted == -1.0) {
            var currentVersion = 0.0
            var currentPlugin = pkg.versions.first()
            for (version in pkg.versions) {
                if(version.version > currentVersion) {
                    currentVersion = version.version
                    currentPlugin = version
                }
            }
            val file = File(plFolder, "${pkg.name}.jar")
            http.downloadFile(currentPlugin.link, file)
            main.config.update(pkg.id, currentVersion)
            sender.sendMessage("${pkg.name} has been installed!")
        } else {

            val version = pkg.versions.find { it.version == wanted }

            val file = File(plFolder, "${pkg.name}.jar")
            http.downloadFile(version!!.link, file)
            main.config.update(pkg.id, wanted)
            sender.sendMessage("${pkg.name} has been updated!")

        }
    }
}