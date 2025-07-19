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
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.DisplayName;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
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

		// check that args produce a valid display name
		switch (DisplayName.of(args))
		{
			case DisplayName.Invalid invalidName -> sendInvalidNameMessage(sender, invalidName);
			case DisplayName.Valid validName ->
			{
				// check for existing graveyard
				switch (plugin.dataStore.graveyards().get(validName.toSearchKey()))
				{
					case Graveyard.Valid existing -> overwriteExistingGraveyard(player, existing);
					case Graveyard.Invalid ignored -> insertNewGraveyard(player, validName);
				}
			}
		}

		return true;
	}


	/**
	 * Insert new graveyard in the datastore if all parameters are valid, outputting appropriate message.
	 *
	 * @param player      the player who issued the command
	 * @param displayName the display name of the new graveyard record
	 * @return a Graveyard.Valid if insertion was successful, or a Graveyard.Invalid if it failed
	 */
	@SuppressWarnings("UnusedReturnValue")
	private Graveyard insertNewGraveyard(final Player player,
	                                     final DisplayName.Valid displayName)
	{
		return switch (Graveyard.of(plugin, displayName, player))
		{
			case Graveyard.Invalid invalid -> sendFailedInvalidMessage(player, invalid);
			case Graveyard.Valid validGraveyard -> switch (plugin.dataStore.graveyards().save(validGraveyard))
			{
				case Graveyard.Invalid invalid -> sendFailedInsertMessage(player, invalid);
				case Graveyard.Valid inserted -> sendSuccessMessage(player, inserted);
			};
		};
	}


	/**
	 * Overwrite existing graveyard in the datastore if the player has requisite permission
	 *
	 * @param sender the player who issued the command
	 * @param graveyard the new graveyard that will replace the existing graveyard in the datastore
	 * @return an instance of the new graveyard
	 */
	@SuppressWarnings("UnusedReturnValue")
	private Graveyard overwriteExistingGraveyard(final CommandSender sender,
	                                             final Graveyard.Valid graveyard)
	{
		if (sender.hasPermission("graveyard.overwrite"))
		{
			plugin.dataStore.graveyards().update(graveyard);
			return sendOverwriteSuccessMessage(sender, graveyard);
		}
		else
		{
			return sendOverwriteDeniedMessage(sender, graveyard);
		}
	}


	/**
	 * Send graveyard overwrite success message
	 *
	 * @param sender the player who issued the command
	 * @param graveyard the existing graveyard object that could not be overwritten
	 */
	private Graveyard sendOverwriteSuccessMessage(final CommandSender sender,
	                                              final Graveyard.Valid graveyard)
	{
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_CREATE);
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_CREATE_OVERWRITE)
				.setMacro(Macro.GRAVEYARD, graveyard.displayName())
				.send();

		return graveyard;
	}


	/**
	 * Send graveyard overwrite failed message
	 *
	 * @param sender the player who issued the command
	 * @param graveyard the existing graveyard object that could not be overwritten
	 */
	private Graveyard sendOverwriteDeniedMessage(final CommandSender sender, final Graveyard graveyard)
	{
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_CREATE_EXISTS)
				.setMacro(Macro.GRAVEYARD, graveyard.displayName())
				.send();

		return graveyard;
	}


	/**
	 * Send display name invalid message
	 * @param sender the player who issued the command
	 * @param invalidName the invalid display name
	 */
	private void sendInvalidNameMessage(CommandSender sender, DisplayName.Invalid invalidName)
	{
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_CREATE_INVALID_NAME)
				.setMacro(Macro.GRAVEYARD, invalidName.colorString())
				.setMacro(Macro.REASON, invalidName.reason().toString())
				.send();
	}


	/**
	 * Send successful create graveyard message
	 *
	 * @param sender    the player who issued the command
	 * @param graveyard the newly created graveyard record
	 */
	private Graveyard sendSuccessMessage(final CommandSender sender, final Graveyard graveyard)
	{
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_CREATE);
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_CREATE)
				.setMacro(Macro.GRAVEYARD, graveyard.displayName())
				.send();

		return graveyard;
	}


	/**
	 * Send create graveyard failed message
	 *
	 * @param sender    the player who issued the command
	 * @param graveyard the newly created graveyard record
	 */
	private Graveyard sendFailedInvalidMessage(final CommandSender sender, final Graveyard graveyard)
	{
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_CREATE_INVALID)
				.setMacro(Macro.GRAVEYARD, graveyard.displayName())
				.send();

		return graveyard;
	}


	/**
	 * Send graveyard insert failed message
	 *
	 * @param sender    the player who issued the command
	 * @param graveyard the newly created graveyard record
	 */
	private Graveyard sendFailedInsertMessage(final CommandSender sender, final Graveyard graveyard)
	{
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_CREATE_INSERT)
				.setMacro(Macro.GRAVEYARD, graveyard.displayName())
				.send();

		return graveyard;
	}

}
