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
import com.winterhavenmc.savagegraveyards.plugin.util.Macro;
import com.winterhavenmc.savagegraveyards.plugin.util.MessageId;
import com.winterhavenmc.savagegraveyards.plugin.util.SoundId;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;


/**
 * Creates new graveyard at player location with given name
 */
final class CreateSubcommand extends AbstractSubcommand implements Subcommand
{
	private final PluginMain plugin;


	/**
	 * Class constructor
	 * @param plugin reference to plugin main class instance
	 */
	CreateSubcommand(final PluginMain plugin)
	{
		this.plugin = Objects.requireNonNull(plugin);
		this.name = "create";
		this.usageString = "/graveyard create <graveyard name>";
		this.description = MessageId.COMMAND_HELP_CREATE;
		this.permissionNode = "graveyard.create";
		this.minArgs = 1;
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		if (!(sender instanceof Player player))
		{
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		if (!sender.hasPermission(permissionNode))
		{
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_CREATE).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		if (args.size() < minArgs)
		{
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		SearchKey searchKey = SearchKey.of(args);

		switch (searchKey)
		{
			case SearchKey.Invalid invalidKey -> sendInvalidKey(sender, invalidKey);
			case SearchKey.Valid validKey ->
			{
				// check for existing graveyard
				switch (plugin.dataStore.selectGraveyard(validKey))
				{
					case Graveyard.Valid valid -> overwrite(sender, valid);
					case Graveyard.Invalid invalid -> create(player, invalid.displayName().toString());
				}
			}
		}

		return true;
	}


	private void sendInvalidKey(CommandSender sender, SearchKey.Invalid invalidKey)
	{
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_CREATE_INVALID_KEY)
				.setMacro(Macro.SEARCH_KEY, invalidKey.string())
				.send();
	}


	/**
	 * Create new graveyard
	 *
	 * @param player      the player who issued the command
	 * @param displayName the display name of the new graveyard record
	 */
	@SuppressWarnings("UnusedReturnValue")
	private Graveyard create(final Player player,
	                         final String displayName)
	{
		Graveyard graveyard = Graveyard.of(plugin, displayName, player);

		// switch on object type
		switch (graveyard)
		{
			case Graveyard.Invalid invalid -> sendFailedInvalid(player, invalid);
			case Graveyard.Valid valid ->
			{
				// switch on insert record
				switch (plugin.dataStore.insertGraveyard(valid))
				{
					case Graveyard.Invalid invalid -> sendFailedInsert(player, invalid);
					case Graveyard.Valid inserted -> sendSuccess(player, inserted);
				}
			}
		}

		return graveyard;
	}


	@SuppressWarnings("UnusedReturnValue")
	private Graveyard overwrite(final CommandSender sender,
	                            final Graveyard.Valid graveyard)
	{
		return (sender.hasPermission("graveyard.overwrite"))
				? plugin.dataStore.updateGraveyard(graveyard)
				: sendOverwriteDenied(sender, graveyard);
	}


	/**
	 * Send successful create graveyard message
	 *
	 * @param sender    the player who issued the command
	 * @param graveyard the newly created graveyard record
	 */
	private void sendSuccess(final CommandSender sender, final Graveyard graveyard)
	{
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_CREATE);
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_CREATE)
				.setMacro(Macro.GRAVEYARD, graveyard.displayName())
				.send();
	}


	private void sendFailedInvalid(final CommandSender sender, final Graveyard graveyard)
	{
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_CREATE_INVALID)
				.setMacro(Macro.GRAVEYARD, graveyard.displayName())
				.send();
	}


	private void sendFailedInsert(final CommandSender sender, final Graveyard graveyard)
	{
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_CREATE_INSERT)
				.setMacro(Macro.GRAVEYARD, graveyard.displayName())
				.send();
	}


	/**
	 * Send unsuccessful overwrite graveyard message
	 *
	 * @param sender the player who issued the command
	 * @param graveyard the existing graveyard object that could not be overwritten
	 */
	private Graveyard sendOverwriteDenied(final CommandSender sender, final Graveyard graveyard)
	{
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_CREATE_EXISTS)
				.setMacro(Macro.GRAVEYARD, graveyard.displayName())
				.send();

		plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		return graveyard;
	}

}
