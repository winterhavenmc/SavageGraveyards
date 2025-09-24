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

package com.winterhavenmc.savagegraveyards.adapters.commands.bukkit;

import com.winterhavenmc.savagegraveyards.core.context.CommandCtx;
import com.winterhavenmc.savagegraveyards.models.graveyard.*;
import com.winterhavenmc.savagegraveyards.core.util.SoundId;
import com.winterhavenmc.savagegraveyards.core.util.Macro;
import com.winterhavenmc.savagegraveyards.core.util.MessageId;

import com.winterhavenmc.savagegraveyards.models.searchkey.InvalidSearchKey;
import com.winterhavenmc.savagegraveyards.models.searchkey.SearchKey;
import com.winterhavenmc.savagegraveyards.models.searchkey.ValidSearchKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;


/**
 * Teleport command implementation<br>
 * teleports player to graveyard location
 */
final class TeleportCommand extends AbstractSubcommand implements Subcommand
{
	private final CommandCtx ctx;


	/**
	 * Class constructor
	 */
	TeleportCommand(final CommandCtx ctx)
	{
		this.ctx = ctx;
		this.name = "teleport";
		this.usageString = "/graveyard teleport <name>";
		this.description = MessageId.COMMAND_HELP_TELEPORT;
		this.permissionNode = "graveyard.teleport";
		this.aliases = Set.of("tp");
		this.minArgs = 1;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender,
	                                  final Command command,
	                                  final String alias,
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
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// check for permission
		if (!sender.hasPermission(permissionNode))
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_TELEPORT).send();
			return true;
		}

		// check minimum arguments
		if (args.size() < minArgs)
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			displayUsage(sender);
			return true;
		}

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

		return true;
	}

	private void sendKeyInvalidMessage(CommandSender sender, InvalidSearchKey invalid)
	{
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_TELEPORT_DESTINATION_KEY_INVALID)
				.setMacro(Macro.SEARCH_KEY, invalid.string())
				.setMacro(Macro.REASON, invalid.reason().toString());
	}


	private void teleportPlayer(Player player, ValidGraveyard graveyard)
	{
		// if destination graveyard location is null, send fail message and return
		if (ctx.plugin().getServer().getWorld(graveyard.location().world().uid()) == null)
		{
			ctx.soundConfig().playSound(player, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(player, MessageId.COMMAND_FAIL_TELEPORT_WORLD_INVALID)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.setMacro(Macro.INVALID_WORLD, graveyard.worldName())
					.send();
		}

		// play teleport departure sound
		ctx.soundConfig().playSound(player, SoundId.TELEPORT_SUCCESS_DEPARTURE);

		// try to teleport player to graveyard location
		if (player.teleport(graveyard.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN))
		{
			ctx.soundConfig().playSound(player, SoundId.TELEPORT_SUCCESS_ARRIVAL);
			ctx.messageBuilder().compose(player, MessageId.COMMAND_SUCCESS_TELEPORT)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.send();
		}
		else
		{
			ctx.soundConfig().playSound(player, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(player, MessageId.COMMAND_FAIL_TELEPORT)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.send();
		}
	}


	private void teleportFail(CommandSender sender, Graveyard graveyard)
	{
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_NO_RECORD)
				.setMacro(Macro.GRAVEYARD, graveyard)
				.send();
	}

}
