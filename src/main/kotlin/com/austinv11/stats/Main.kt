package com.austinv11.stats

import commandable.annotations.Service
import commandable.annotations.WireService
import commandable.api.Command
import commandable.api.CommandGroup
import commandable.aspects.CommandComposer
import commandable.util.Boilerplate
import discord4j.core.DiscordClient
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.ReactionAddEvent
import reactor.core.publisher.Mono
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

lateinit var client: DiscordClient

typealias CommandAnnotation = commandable.aspects.Command

fun main(args: Array<String>) {
    client = Boilerplate.from(args).launch()
    for (hook in ServiceLoader.load(EarlyHook::class.java))
        hook.invoke(client)
}

@Service
abstract class StatsBotCommandGroup(val name: String) : CommandGroup() {

    init {
        this.javaClass.declaredMethods.filter { it.isAnnotationPresent(CommandAnnotation::class.java) }.forEach { addCommand(CommandComposer.composeCommand(this, it)) }
    }

    override fun name(): String = name
}

@Service
interface EarlyHook {

    fun invoke(client: DiscordClient)
}

@WireService(EarlyHook::class)
class StatsBotGuildManager : EarlyHook{

    val GUILD_ID = Snowflake.of("477919171645145089")
    val README_MESSAGE_ID = Snowflake.of("477924820768653313")
    val VERIFIED_ROLE_ID = Snowflake.of("477919946706255884")

    override fun invoke(client: DiscordClient) {
        //Handle terms of service agreement
        client.eventDispatcher.on(ReactionAddEvent::class.java)
                .filter { it.messageId == README_MESSAGE_ID }
                .filter { it.emoji is ReactionEmoji.Unicode }
                .filter { (it.emoji as ReactionEmoji.Unicode).raw == "\uD83C\uDDFE" }
                .flatMap { it.user }
                .filter { !it.isBot }
                .flatMap { it.asMember(GUILD_ID).flatMap { it.addRole(VERIFIED_ROLE_ID) } }
                .subscribe()
    }
}
