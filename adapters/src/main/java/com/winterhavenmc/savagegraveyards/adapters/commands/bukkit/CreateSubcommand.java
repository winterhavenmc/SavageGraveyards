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
import com.winterhavenmc.savagegraveyards.models.displayname.DisplayName;
import com.winterhavenmc.savagegraveyards.models.displayname.InvalidDisplayName;
import com.winterhavenmc.savagegraveyards.models.displayname.ValidDisplayName;
import com.winterhavenmc.savagegraveyards.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.core.util.Macro;
import com.winterhavenmc.savagegraveyards.core.util.MessageId;
import com.winterhavenmc.savagegraveyards.core.util.SoundId;

import com.winterhavenmc.savagegraveyards.models.graveyard.InvalidGraveyard;
import com.winterhavenmc.savagegraveyards.models.graveyard.ValidGraveyard;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;


/**
 * Creates new graveyard at player location with given name
 */
final class CreateSubcommand extends AbstractSubcommand implements Subcommand
{
	private final CommandCtx ctx;

	/**
	 * Class constructor
	 */
	CreateSubcommand(final CommandCtx ctx)
	{
		this.ctx = ctx;
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
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		if (!sender.hasPermission(permissionNode))
		{
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_CREATE).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		if (args.size() < minArgs)
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// check that args produce a valid display name
		switch (DisplayName.of(args))
		{
			case InvalidDisplayName invalidName -> sendInvalidNameMessage(sender, invalidName);
			case ValidDisplayName validName ->
			{
				// check for existing graveyard
				switch (ctx.graveyards().get(validName.searchKey()))
				{
					case ValidGraveyard existing -> overwriteExistingGraveyard(player, existing);
					case InvalidGraveyard ignored -> insertNewGraveyard(player, validName);
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
	                                     final ValidDisplayName displayName)
	{
		return switch (Graveyard.of(ctx.plugin(), displayName, player))
		{
			case InvalidGraveyard invalid -> sendFailedInvalidMessage(player, invalid);
			case ValidGraveyard validGraveyard -> switch (ctx.graveyards().save(validGraveyard))
			{
				case InvalidGraveyard invalid -> sendFailedInsertMessage(player, invalid);
				case ValidGraveyard inserted -> sendSuccessMessage(player, inserted);
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
	                                             final ValidGraveyard graveyard)
	{
		if (sender.hasPermission("graveyard.overwrite"))
		{
			ctx.graveyards().update(graveyard);
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
	                                              final ValidGraveyard graveyard)
	{
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_SUCCESS_CREATE);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_CREATE_OVERWRITE)
				.setMacro(Macro.GRAVEYARD, graveyard)
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
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_CREATE_EXISTS)
				.setMacro(Macro.GRAVEYARD, graveyard)
				.send();

		return graveyard;
	}


	/**
	 * Send display name invalid message
	 * @param sender the player who issued the command
	 * @param invalidName the invalid display name
	 */
	private void sendInvalidNameMessage(CommandSender sender, InvalidDisplayName invalidName)
	{
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_CREATE_INVALID_NAME)
				.setMacro(Macro.GRAVEYARD, invalidName.colorString()) //TODO: create INVALID_NAME macro
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
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_SUCCESS_CREATE);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_CREATE)
				.setMacro(Macro.GRAVEYARD, graveyard)
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
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_CREATE_INVALID)
				.setMacro(Macro.GRAVEYARD, graveyard)
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
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_CREATE_INSERT)
				.setMacro(Macro.GRAVEYARD, graveyard)
				.send();

		return graveyard;
	}

}
