/*
 * Copyright (c) 2022 Tim Savage.
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

package com.winterhavenmc.savagegraveyards.commands;

import com.winterhavenmc.savagegraveyards.PluginMain;
import com.winterhavenmc.savagegraveyards.util.SoundId;
import com.winterhavenmc.savagegraveyards.storage.Graveyard;
import com.winterhavenmc.savagegraveyards.util.Macro;
import com.winterhavenmc.savagegraveyards.util.MessageId;

import org.bukkit.Location;
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
		if (args.length == 2)
		{
			// return list of valid matching graveyard names
			return plugin.dataStore.selectMatchingGraveyardNames(args[1]);
		}

		return Collections.emptyList();
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
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_TELEPORT).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check minimum arguments
		if (args.size() < minArgs)
		{
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			displayUsage(sender);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get display name from remaining arguments
		String displayName = String.join(" ", args);

		// get graveyard from datastore
		Optional<Graveyard> optionalGraveyard = plugin.dataStore.selectGraveyard(displayName);

		// if graveyard does not exist in datastore, send message and return
		if (optionalGraveyard.isEmpty())
		{
			// create dummy graveyard to send to message manager
			Graveyard dummyGraveyard = new Graveyard.Builder(plugin).displayName(displayName).build();

			// send message
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_NO_RECORD)
					.setMacro(Macro.GRAVEYARD, dummyGraveyard)
					.send();

			// play sound
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// unwrap optional graveyard
		Graveyard graveyard = optionalGraveyard.get();

		// get optional graveyard location
		Optional<Location> optionalDestination = graveyard.getOptLocation();

		// if destination is empty, send fail message and return
		if (optionalDestination.isEmpty())
		{
			// send message
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_TELEPORT_WORLD_INVALID)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.setMacro(Macro.INVALID_WORLD, graveyard.getWorldName())
					.send();

			// play sound
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// unwrap optional destination
		Location destination = optionalDestination.get();

		// play teleport departure sound
		plugin.soundConfig.playSound(player, SoundId.TELEPORT_SUCCESS_DEPARTURE);

		// try to teleport player to graveyard location
		if (player.teleport(destination, PlayerTeleportEvent.TeleportCause.PLUGIN))
		{
			// send successful teleport message
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_TELEPORT)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.setMacro(Macro.LOCATION, graveyard.getOptLocation())
					.send();

			// play success sound
			plugin.soundConfig.playSound(player, SoundId.TELEPORT_SUCCESS_ARRIVAL);
		}
		else
		{
			// send message
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_TELEPORT).setMacro(Macro.GRAVEYARD, graveyard).send();

			// play sound
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		}

		return true;
	}

}
