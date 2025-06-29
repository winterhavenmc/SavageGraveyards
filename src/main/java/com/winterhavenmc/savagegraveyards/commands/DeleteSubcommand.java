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

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;


/**
 * Delete command implementation<br>
 * Removes graveyard record from datastore
 */
final class DeleteSubcommand extends AbstractSubcommand implements Subcommand
{
	private final PluginMain plugin;


	/**
	 * Class constructor
	 * @param plugin reference to plugin main class instance
	 */
	DeleteSubcommand(final PluginMain plugin)
	{
		this.plugin = Objects.requireNonNull(plugin);
		this.name = "delete";
		this.usageString = "/graveyard delete <graveyard name>";
		this.description = MessageId.COMMAND_HELP_DELETE;
		this.permissionNode = "graveyard.delete";
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
		// check for permission
		if (!sender.hasPermission(permissionNode))
		{
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_DELETE).send();
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

		// set displayName to passed arguments
		String displayName = String.join(" ", args);

		// delete graveyard record from storage
		plugin.dataStore.deleteGraveyard(displayName).ifPresentOrElse(
				graveyard -> sendGraveyardDeletedMessage(sender, graveyard),
				() -> sendNoGraveyardMessage(sender, displayName));

		return true;
	}


	private void sendGraveyardDeletedMessage(CommandSender sender, Graveyard graveyard)
	{
		// send success message to player
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_DELETE)
				.setMacro(Macro.GRAVEYARD, graveyard)
				.send();

		// play successful delete sound
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_DELETE);
	}


	private void sendNoGraveyardMessage(CommandSender sender, String displayName)
	{
		// create dummy graveyard to send to message builder
		Graveyard dummyGraveyard = new Graveyard.Builder(plugin).displayName(displayName).build();

		// send message
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_NO_RECORD)
				.setMacro(Macro.GRAVEYARD, dummyGraveyard)
				.send();

		// play command fail sound
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
	}

}
