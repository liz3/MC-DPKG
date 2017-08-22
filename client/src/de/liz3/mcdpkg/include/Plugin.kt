package de.liz3.mcdpkg.include

import java.util.*

class Plugin(val id: Int, val name: String, val userId: Long, val versions: List<PluginVersion>, val dependencies: Vector<Int>) {


}


class PluginVersion(val version: Double, val link:String, val hash:String, val serverVersion:List<String>)