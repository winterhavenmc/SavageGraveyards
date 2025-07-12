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
		return (args.length == 2)
				? plugin.dataStore.selectMatchingGraveyardNames(args[1])
				: Collections.emptyList();
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		if (!sender.hasPermission(permissionNode))
		{
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_DELETE).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		if (args.size() < minArgs)
		{
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			displayUsage(sender);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		switch (SearchKey.of(args))
		{
			case SearchKey.Invalid invalidKey -> invalidKeyMessage(sender, invalidKey);
			case SearchKey.Valid validKey ->
			{
				switch (plugin.dataStore.deleteGraveyard(validKey))
				{
					case Graveyard.Valid valid -> successMessage(sender, valid);
					case Graveyard.Invalid invalid -> notFoundMessage(sender, invalid);
				}
			}
		}
		return true;
	}


	private void invalidKeyMessage(final CommandSender sender, final SearchKey.Invalid invalid)
	{
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_DELETE_INVALID_KEY)
				.setMacro(Macro.REASON, invalid.reason())
				.send();
	}


	private void successMessage(final CommandSender sender, final Graveyard.Valid graveyard)
	{
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_DELETE);
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_DELETE)
				.setMacro(Macro.GRAVEYARD, graveyard.displayName())
				.send();
	}


	private void notFoundMessage(final CommandSender sender,
	                             final Graveyard.Invalid invalid)
	{
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_NO_RECORD)
				.setMacro(Macro.GRAVEYARD, invalid.displayName())
				.setMacro(Macro.REASON, invalid.reason())
				.send();
	}

}
