package de.liz3.mcdpkg.core

import de.liz3.mcdpkg.Http
import de.liz3.mcdpkg.McDpkg
import de.liz3.mcdpkg.include.Plugin
import de.liz3.mcdpkg.include.PluginVersion
import de.liz3.mcdpkg.include.Request
import de.liz3.mcdpkg.include.RequestType
import org.binaryone.jutils.io.IOUtils
import org.bukkit.command.CommandSender
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java
        .util.*
import kotlin.collections.HashMap

class Manager(val main: McDpkg) {

    val httpService = Http()
    val installManager = InstallationManager(main, httpService)

    fun executeRequest(req: Request) {

        if(req.method == RequestType.UPDATE) {

            val obj = JSONObject()
            val packages = JSONArray()
            for (x in main.config.installed.keys) {
                packages.put(x)
            }
            obj.put("action", "search")
            obj.put("ids", packages)
            obj.put("backports", true)
            req.sender.sendMessage("Checking for updates available")
            val hostVersion: String = {
                val str = main.server.version.split("MC: ")[1].split(")")[0]

                str
            }.invoke()
            httpService.postRequest(main.config.url, obj.toString(), HashMap(), { result ->

                var updated = 0
                if (result.responseCode != 200) {
                    return@postRequest

                }
                val response = JSONObject(IOUtils.convertStreamToString(result.inputStream))

                val parsed = parse(response)

                if(parsed.first) {

                    for ((name, plugin) in parsed.second) {


                        val installed = main.config.get(plugin!!.id)

                        for (version in plugin.versions) {
                            if(version.version > installed!! && version.serverVersion.contains(hostVersion)) {
                                req.sender.sendMessage("Update found for $name")
                                updated++
                                installManager.install(version.version, plugin, req.sender)
                            }
                        }
                    }
                    req.sender.sendMessage("Updated a total of $updated plugins")
                }
            })


        }
        if (req.method == RequestType.SEARCH) {
            val obj = JSONObject()
            val packages = JSONArray()
            for (x in req.packages) {
                packages.put(x)
            }
            obj.put("action", "search")
            obj.put("packages", packages)
            obj.put("backports", req.backports)


            httpService.postRequest(main.config.url, obj.toString(), HashMap(), { result ->

                if (result.responseCode == 200) {
                    val response = JSONObject(IOUtils.convertStreamToString(result.inputStream))
                    if (response.getBoolean("success")) {
                        val data = response.getJSONObject("data")

                        for (key in data.keySet()) {
                            if (data.get(key) is Boolean) {
                                req.sender.sendMessage("$key was not found!")
                            } else {
                                req.sender.sendMessage("$key:")
                                val pl = data.getJSONObject(key)
                                pl.getJSONArray("versions")
                                        .filterIsInstance<JSONObject>()
                                        .forEach { req.sender.sendMessage("version: ${it.getString("version")}") }
                            }
                        }
                    }

                }

            })

        }
        if (req.method == RequestType.INSTALL) {
            val obj = JSONObject()
            val packages = JSONArray()
            for (x in req.packages) {

                if (x.contains("@")) {
                    packages.put(x.split("@")[0])
                } else {
                    packages.put(x)
                }
            }
            obj.put("action", "search")
            obj.put("packages", packages)
            obj.put("backports", req.backports)
            val version: Double = {
                val str = main.server.version.split("MC: ")[1].split(")")[0]

                str.toDouble()
            }.invoke()
            req.sender.sendMessage("Searching for Packages:\n")
            for (x in req.packages) {

                req.sender.sendMessage(x)
            }
            httpService.postRequest(main.config.url, obj.toString(), HashMap(), { result ->

                if (result.responseCode != 200) {
                    return@postRequest

                }
                val response = JSONObject(IOUtils.convertStreamToString(result.inputStream))

                val parsed = parse(response)

                if (!parsed.first) {
                    req.sender.sendMessage("Fetch failed!")
                    return@postRequest
                }
                for ((name, plugin) in parsed.second) {
                    if (plugin == null) {
                        req.sender.sendMessage("$name was not found")
                        continue
                    }
                    var found = false
                    for (x in plugin.versions) {
                        for (sX in x.serverVersion) {
                            if (sX.toDouble() == version) {
                                found = true
                                break
                            }
                        }
                        if (found) break

                    }
                    if (!found) {
                        req.sender.sendMessage("No versions of $name are with the server $version compatible!")
                        continue
                    }
                    val versionWanted = {
                        var ver = -1.0
                        req.packages
                                .filter { it.contains(name, true) && it.contains("@") }
                                .forEach { ver = it.split("@")[1].toDouble() }
                        ver
                    }.invoke()
                    if (main.config.installed.containsKey(plugin.id)) {
                        if (versionWanted == -1.0 || versionWanted == main.config.installed[plugin.id]) {
                            req.sender.sendMessage("$name is already installed")
                            continue
                        }
                    }
                    manageDependencies(plugin, req.sender, req.packages)
                    installManager.install(versionWanted, plugin, req.sender)


                }

                if (req.reload) {
                    req.sender.sendMessage("Reloading server...")
                    main.server.reload()
                }
            })
        }
    }

    fun manageDependencies(pkg: Plugin, sender: CommandSender, original: List<String>) {

        if(pkg.dependencies.size == 0) return

        val hostVersion: Double = {
            val str = main.server.version.split("MC: ")[1].split(")")[0]
            str.toDouble()
        }.invoke()
        val obj = JSONObject()
        val packages = JSONArray()
        for (x in pkg.dependencies) {
            packages.put(x)
        }
        obj.put("action", "search")
        obj.put("ids", packages)
        obj.put("backports", false)

        httpService.postRequest(main.config.url, obj.toString(), HashMap(), { result ->

            if (result.responseCode != 200) {
                sender.sendMessage("Failed to get dependencies of ${pkg.name}!")
            }
            val response = JSONObject(IOUtils.convertStreamToString(result.inputStream))
            val parsed = parse(response)

            if (!parsed.first) {
                sender.sendMessage("Failed to get dependencies of ${pkg.name}!")
            }

            for ((key, value) in parsed.second) {

                var found = original.any { key.equals(it, true) }

                if(found) {
                    sender.sendMessage("Skipped $key dependency from ${pkg.name}, because it will be installed")
                    continue
                }
                if(main.config.installed.containsKey(value!!.id)) {
                    sender.sendMessage("Skipped $key dependency from ${pkg.name}, because it already installed")

                    continue
                }
                for (version in value.versions) {
                    for (serverVersion in version.serverVersion) {

                        if (serverVersion.toDouble() == hostVersion) {
                            println("found")
                            found = true
                            break
                        }
                    }
                    if (found) break
                }

                if(found) {
                    installManager.install(-1.0, value, sender)
                    return@postRequest
                } else {
                    sender.sendMessage("Failed to get dependency $key of ${pkg.name}, no compatible version found!!")

                }
            }

        });

    }

    fun parse(input: JSONObject): Pair<Boolean, HashMap<String, Plugin?>> {

        val success = input.getBoolean("success")
        val plugins = HashMap<String, Plugin?>()
        val data = input.getJSONObject("data")
        for (key in data.keys()) {
            val plugin = data[key]
            if (plugin is Boolean) {
                plugins.put(key, null)
                continue
            } else if (plugin is JSONObject) {
                val versions = Vector<PluginVersion>()
                val plVersions = plugin.getJSONArray("versions")
                val dependencies = Vector<Int>()
                val dependenciesObj = plugin.getJSONArray("dependencies")
                for (dep in dependenciesObj) {
                    if(dep == null) continue
                    if(dep is String) {
                        if(dep.isEmpty() || dep == "") continue
                        dependencies.addElement(dep.toInt())

                    }
                }
                for (version in plVersions) {

                    if (version is JSONObject) {
                        val serverVersions = Vector<String>()
                        val serverVers = version.getJSONArray("server")
                        for (server in serverVers) {
                            serverVersions.addElement(server as String)
                        }
                        versions.addElement(PluginVersion(version.getString("version").toDouble(), version.getString("link"), version.getString("hash"), serverVersions))

                    }
                }
                plugins.put(key, Plugin(plugin.getInt("id"), key, plugin.getLong("author"), versions, dependencies))


            }
        }
        return Pair(success, plugins)
    }

}