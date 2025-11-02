/*
 * Copyright (c) 2022-2025 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.savagegraveyards.core.commands;

import com.winterhavenmc.savagegraveyards.core.context.CommandCtx;
import com.winterhavenmc.savagegraveyards.core.util.Macro;
import com.winterhavenmc.savagegraveyards.core.util.MessageId;

import com.winterhavenmc.savagegraveyards.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.models.graveyard.ValidGraveyard;
import com.winterhavenmc.savagegraveyards.models.graveyard.InvalidGraveyard;
import com.winterhavenmc.savagegraveyards.models.searchkey.SearchKey;
import com.winterhavenmc.savagegraveyards.models.searchkey.ValidSearchKey;
import com.winterhavenmc.savagegraveyards.models.searchkey.InvalidSearchKey;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;


/**
 * Teleport command implementation<br>
 * teleports player to graveyard location
 */
public final class TeleportSubcommand extends AbstractSubcommand
{
	private final CommandCtx ctx;


	/**
	 * Class constructor
	 */
	public TeleportSubcommand(final CommandCtx ctx)
	{
		this.ctx = ctx;
		this.name = "teleport";
		this.usageString = "/graveyard teleport <name>";
		this.description = MessageId.COMMAND_DESCRIPTION_TELEPORT;
		this.permissionNode = "graveyard.teleport";
		this.aliases = Set.of("tp");
		this.minArgs = 1;
	}


	@Override
	public List<String> onTabComplete(final @NotNull CommandSender sender,
	                                  final @NotNull Command command,
	                                  final @NotNull String alias,
	                                  final String[] args)
	{
		return (args.length == 2)
				? ctx.graveyards().getMatchingNames(args[1])
				: Collections.emptyList();
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// sender must be in game player
		if (!(sender instanceof Player player))
		{
			return ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
		}

		// check sender permission
		if (!sender.hasPermission(permissionNode))
		{
			return ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_PERMISSION_TELEPORT).send();
		}

		// validate arguments
		if (args.size() < minArgs)
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			displayUsage(sender);
			return true;
		}

		// perform command logic
		switch (SearchKey.of(args))
		{
			case InvalidSearchKey invalidKey -> sendKeyInvalidMessage(sender, invalidKey);
			case ValidSearchKey validKey ->
			{
				switch (ctx.graveyards().get(validKey))
				{
					case ValidGraveyard valid -> teleportPlayer(player, valid);
					case InvalidGraveyard invalid -> teleportFail(sender, invalid);
				}
			}
		}

		// return true to suppress display of bukkit command usage
		return true;
	}


	private void sendKeyInvalidMessage(CommandSender sender, InvalidSearchKey invalid)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_TELEPORT_DESTINATION_KEY_INVALID)
				.setMacro(Macro.SEARCH_KEY, invalid.string())
				.setMacro(Macro.REASON, invalid.reason().toString());
	}


	private void teleportPlayer(Player player, ValidGraveyard graveyard)
	{
		// if destination graveyard location is null, send fail message and return
		if (ctx.plugin().getServer().getWorld(graveyard.location().world().uid()) == null)
		{
			ctx.messageBuilder().compose(player, MessageId.COMMAND_FAIL_TELEPORT_WORLD_INVALID)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.setMacro(Macro.INVALID_WORLD, graveyard.worldName())
					.send();
		}

		// play teleport departure sound
		ctx.messageBuilder().compose(player, MessageId.EVENT_TELEPORT_SUCCESS_DEPARTURE)
				.setMacro(Macro.GRAVEYARD, graveyard)
				.send();

		// try to teleport player to graveyard location
		if (player.teleport(graveyard.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN))
		{
			ctx.messageBuilder().compose(player, MessageId.EVENT_TELEPORT_SUCCESS_ARRIVAL)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.send();
		}
		else
		{
			ctx.messageBuilder().compose(player, MessageId.COMMAND_FAIL_TELEPORT)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.send();
		}
	}


	private void teleportFail(CommandSender sender, Graveyard graveyard)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_NO_RECORD)
				.setMacro(Macro.GRAVEYARD, graveyard)
				.send();
	}

}
