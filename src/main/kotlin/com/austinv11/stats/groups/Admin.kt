package com.austinv11.stats.groups

import com.austinv11.stats.StatsBotCommandGroup
import com.austinv11.stats.client
import commandable.annotations.WireService
import commandable.aspects.AutoHelp
import commandable.aspects.Command
import commandable.aspects.OwnerOnly

@WireService(StatsBotCommandGroup::class)
class AdminGroup() : StatsBotCommandGroup(name = "Administration") {

    @Command
    @AutoHelp("Shuts down this bot (owner only!)")
    @OwnerOnly
    fun logout() {
        client.logout()
    }
}

