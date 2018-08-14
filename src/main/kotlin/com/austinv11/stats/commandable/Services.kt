package com.austinv11.stats.commandable

import com.austinv11.stats.StatsBotCommandGroup
import commandable.annotations.WireService
import commandable.api.*
import commandable.api.impl.PrefixCommandActivator
import commandable.util.HelpPage
import commandable.util.MissingPermissionsException
import discord4j.command.util.CommandException
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.MessageCreateSpec
import discord4j.core.spec.MessageEditSpec
import reactor.core.publisher.Mono
import java.awt.Color
import java.time.Instant
import java.util.*

typealias JString = java.lang.String

@WireService(CommandActivator::class)
class PrefixActivator : PrefixCommandActivator() {

    override fun getPrefix(context: MessageCreateEvent) = Mono.just(":")  //TODO: make customizable
}

@WireService(CommandPlugin::class)
class MainPlugin : CommandPlugin {

    val groups: List<CommandGroup> by lazy {
        val l = mutableListOf<CommandGroup>()
        for (group in ServiceLoader.load(StatsBotCommandGroup::class.java)) {
            l.add(group)
        }
        return@lazy l
    }

    override fun getCommandGroups() = groups

    override fun version() = "1.0"

    override fun name() = "StatsBot Main"
}

@WireService(ErrorBinding::class)
class GenericErrorBinding : ErrorBinding<CommandException> {

    override fun handle(context: MessageCreateEvent, error: CommandException): Mono<Void> {
        return context.message.channel.flatMap {
            it.createMessage(MessageCreateSpec().setContent(":warning: ${error.response().orElse("Unable to handle that command!")}"))
        }.then()
    }

    override fun bound() = CommandException::class.java
}

@WireService(ErrorBinding::class)
class MissingPermissionsBinding : ErrorBinding<MissingPermissionsException> {

    override fun handle(context: MessageCreateEvent, error: MissingPermissionsException): Mono<Void> {
        return context.message.channel.flatMap {
            it.createMessage(MessageCreateSpec().setContent(":warning: ${error.response().orElse("Unable to handle that command!")}"))
        }.then()
    }

    override fun bound() = MissingPermissionsException::class.java
}

@WireService(HelpPageRenderer::class)
class HelpRenderer : HelpPageRenderer {

    val iconUrl = "https://image.flaticon.com/icons/png/512/235/235218.png"
    val maxCommandsPerPage = 15
    val helpColor = Color(247, 5, 8)

    override fun handleEdit(pages: MutableList<HelpPage>) = mutableListOf<MessageEditSpec>().apply {
        val coalesced = mutableListOf<MutableList<HelpPage>>()
        pages.forEachIndexed { i, page ->
            val pageIndex = i / maxCommandsPerPage
            if (coalesced.size <= pageIndex) {
                coalesced.add(mutableListOf())
            }
            coalesced[pageIndex].add(page)
        }
        coalesced.forEachIndexed { index, helpPages ->
            this.add(MessageEditSpec().setEmbed(EmbedCreateSpec().apply {
                this.setTitle("Help Page (${index + 1}/${coalesced.size})")
                this.setColor(helpColor)
                this.setThumbnail(iconUrl)
                this.setTimestamp(Instant.now())
                this.setDescription(buildString {
                    this.appendln("**Available Commands**")
                    for (page in helpPages) {
                        this.appendln("• ${page.commandName}")
                    }
                })
            }))
        }
    }

    override fun handleEdit(page: HelpPage) = MessageEditSpec().setEmbed(EmbedCreateSpec().apply {
        this.setTitle("Help Page: ${page.commandName}")
        this.setColor(helpColor)
        this.setThumbnail(iconUrl)
        this.setDescription(buildString {
            this.appendln("**Description**")
            this.appendln(page.description )
            this.appendln().appendln("**Usage**")
            for (args in page.arguments) {
                this.appendln(page.commandName.toLowerCase() + " " + JString.join(" ", args.map { it.toString() }))
            }
        })
        this.setTimestamp(Instant.now())
    })

    override fun handleNew(pages: MutableList<HelpPage>) = mutableListOf<MessageCreateSpec>().apply {
        val coalesced = mutableListOf<MutableList<HelpPage>>()
        pages.forEachIndexed { i, page ->
            val pageIndex = i / maxCommandsPerPage
            if (coalesced.size <= pageIndex) {
                coalesced.add(mutableListOf())
            }
            coalesced[pageIndex].add(page)
        }
        coalesced.forEachIndexed { index, helpPages ->
            this.add(MessageCreateSpec().setEmbed {
                it.setTitle("Help Page (${index + 1}/${coalesced.size})")
                it.setColor(helpColor)
                it.setThumbnail(iconUrl)
                it.setTimestamp(Instant.now())
                it.setDescription(buildString {
                    this.appendln("**Available Commands**")
                    for (page in helpPages) {
                        this.appendln("• ${page.commandName}")
                    }
                })
            })
        }
    }

    override fun handleNew(page: HelpPage) = MessageCreateSpec().setEmbed {
        it.setTitle("Help Page: ${page.commandName}")
        it.setColor(helpColor)
        it.setThumbnail(iconUrl)
        it.setDescription(buildString {
            this.appendln("**Description**")
            this.appendln(page.description )
            this.appendln().appendln("**Usage**")
            for (args in page.arguments) {
                this.appendln(page.commandName.toLowerCase() + " " + JString.join(" ", args.map { it.toString() }))
            }
        })
        it.setTimestamp(Instant.now())
    }
}