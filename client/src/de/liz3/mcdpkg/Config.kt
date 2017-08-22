package de.liz3.mcdpkg

import org.binaryone.jutils.io.FileUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class Config(main: McDpkg)  {

     val installed = HashMap<Int, Double>()
    private val configFolder = main.dataFolder
    private val config = File(configFolder, "config.json")
    val url = "http://127.0.0.1/dpkg/index.php"

    init {

        if(!configFolder.exists()) configFolder.mkdir()


        if(!config.exists()) {
            config.createNewFile()
            writeConfig()

        } else {
            parseConfig()
        }
    }
    fun add(id:Int, version:Double)  {
        installed.put(id, version)

        writeConfig()
    }

    fun update(id:Int, version:Double) {
        remove(id)
        add(id, version)
    }
    fun remove(id:Int) {
        if(installed.containsKey(id)) {
            installed.remove(id)
            writeConfig()
        }
    }
    fun get(id:Int) = installed[id]



    private fun writeConfig() {
        val obj = JSONObject()
        val installedArray = JSONArray()
        for((key, value) in installed) {
            val instObj = JSONObject()
            instObj.put("id", key)
            instObj.put("version", value)
            installedArray.put(instObj)
        }
        obj.put("installed", installedArray)

        FileUtils.writeFile(obj.toString(), config)
    }
    private fun parseConfig() {

        installed.clear()
        val obj = JSONObject(FileUtils.readFile(config))

        for(plugin in obj.getJSONArray("installed")) {
            if(plugin is JSONObject) {
                installed.put(plugin.getInt("id"), plugin.getDouble("version"))
            }
        }
    }

}