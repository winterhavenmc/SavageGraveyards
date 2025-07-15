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

package com.winterhavenmc.savagegraveyards.plugin.commands;

import com.winterhavenmc.savagegraveyards.plugin.PluginMain;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.SearchKey;
import com.winterhavenmc.savagegraveyards.plugin.util.SoundId;
import com.winterhavenmc.savagegraveyards.plugin.util.Macro;
import com.winterhavenmc.savagegraveyards.plugin.util.MessageId;

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
	private final PluginMain plugin;


	/**
	 * Class constructor
	 * @param plugin reference to plugin main class instance
	 */
	TeleportCommand(final PluginMain plugin)
	{
		this.plugin = Objects.requireNonNull(plugin);
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
				? plugin.dataStore.selectMatchingGraveyardNames(args[1])
				: Collections.emptyList();
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// sender must be in game player
		if (!(sender instanceof Player player))
		{
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// check for permission
		if (!sender.hasPermission(permissionNode))
		{
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_TELEPORT).send();
			return true;
		}

		// check minimum arguments
		if (args.size() < minArgs)
		{
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			displayUsage(sender);
			return true;
		}

		switch (SearchKey.of(args))
		{
			case SearchKey.Invalid invalidKey -> sendKeyInvalidMessage(sender, invalidKey);
			case SearchKey.Valid validKey ->
			{
				switch (plugin.dataStore.selectGraveyard(validKey))
				{
					case Graveyard.Valid valid -> teleportPlayer(player, valid);
					case Graveyard.Invalid invalid -> teleportFail(sender, invalid);
				}
			}
		}

		return true;
	}

	private void sendKeyInvalidMessage(CommandSender sender, SearchKey.Invalid invalid)
	{
		//TODO: add body to this method
	}


	private void teleportPlayer(Player player, Graveyard.Valid graveyard)
	{
		// if destination graveyard location is null, send fail message and return
		if (plugin.getServer().getWorld(graveyard.location().world().uid()) == null)
		{
			plugin.soundConfig.playSound(player, SoundId.COMMAND_FAIL);
			plugin.messageBuilder.compose(player, MessageId.COMMAND_FAIL_TELEPORT_WORLD_INVALID)
					.setMacro(Macro.GRAVEYARD, graveyard.displayName().colorString())
					.setMacro(Macro.INVALID_WORLD, graveyard.worldName())
					.send();
		}

		// play teleport departure sound
		plugin.soundConfig.playSound(player, SoundId.TELEPORT_SUCCESS_DEPARTURE);

		// try to teleport player to graveyard location
		if (player.teleport(graveyard.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN))
		{
			plugin.soundConfig.playSound(player, SoundId.TELEPORT_SUCCESS_ARRIVAL);
			plugin.messageBuilder.compose(player, MessageId.COMMAND_SUCCESS_TELEPORT)
					.setMacro(Macro.GRAVEYARD, graveyard.displayName().colorString())
					.setMacro(Macro.LOCATION, graveyard.getLocation())
					.send();
		}
		else
		{
			plugin.soundConfig.playSound(player, SoundId.COMMAND_FAIL);
			plugin.messageBuilder.compose(player, MessageId.COMMAND_FAIL_TELEPORT)
					.setMacro(Macro.GRAVEYARD, graveyard.displayName().colorString())
					.setMacro(Macro.LOCATION, graveyard.getLocation())
					.send();
		}
	}


	private void teleportFail(CommandSender sender, Graveyard optionalGraveyard)
	{
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_NO_RECORD)
				.setMacro(Macro.GRAVEYARD, optionalGraveyard.displayName().colorString())
				.send();
	}

}
