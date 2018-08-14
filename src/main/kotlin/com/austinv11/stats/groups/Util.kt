package com.austinv11.stats.groups

import com.austinv11.stats.StatsBotCommandGroup
import commandable.annotations.WireService
import commandable.api.CommandContext
import commandable.aspects.AutoHelp
import commandable.aspects.Command
import reactor.core.publisher.Mono
import java.time.Instant

@WireService(StatsBotCommandGroup::class)
class UtilGroup() : StatsBotCommandGroup(name = "Utility") {

    @Command
    @AutoHelp("Pong!")
    fun ping(context: CommandContext): Mono<Void> {
        val msg = context.message
        return context.channel.flatMap {
            val currTime = Instant.now().toEpochMilli()
            it.createMessage("Pong! Took approximately ~${currTime - msg.timestamp.toEpochMilli()}ms")
        }.then()
    }
}