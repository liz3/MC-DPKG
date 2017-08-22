package de.liz3.mcdpkg.include

import org.bukkit.command.CommandSender
import java.util.*

enum class RequestType {
    UPDATE,
    SEARCH,
    INSTALL,
    REMOVE
}
class Request(val sender:CommandSender, val  method: RequestType, val packages: Vector<String> = Vector(), val backports:Boolean = false, val reload:Boolean = false)

