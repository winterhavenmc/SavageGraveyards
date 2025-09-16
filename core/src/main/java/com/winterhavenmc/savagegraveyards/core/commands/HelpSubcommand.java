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

import com.winterhavenmc.savagegraveyards.core.PluginController;
import com.winterhavenmc.savagegraveyards.core.util.MessageId;
import com.winterhavenmc.savagegraveyards.core.util.SoundId;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Help command implementation<br>
 * displays help and usage messages for plugin commands
 */
final class HelpSubcommand extends AbstractSubcommand implements Subcommand
{
	private final PluginController.ContextContainer ctx;
	private final SubcommandRegistry subcommandRegistry;


	/**
	 * Class constructor
	 */
	HelpSubcommand(final PluginController.ContextContainer ctx, final SubcommandRegistry subcommandRegistry)
	{
		this.ctx = ctx;
		this.subcommandRegistry = Objects.requireNonNull(subcommandRegistry);
		this.name = "help";
		this.usageString = "/graveyard help [command]";
		this.description = MessageId.COMMAND_HELP_HELP;
		this.permissionNode = "graveyard.help";
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender,
	                                  final Command command,
									  final String alias,
									  final String[] args)
	{
		return (args.length == 2 && args[0].equalsIgnoreCase(this.name))
				? subcommandRegistry.getNames().stream()
						.map(subcommandRegistry::getSubcommand)
						.filter(Optional::isPresent)
						.filter(subcommand -> sender.hasPermission(subcommand.get().getPermissionNode()))
						.map(subcommand -> subcommand.get().getName())
						.filter(subCommandName -> subCommandName.toLowerCase().startsWith(args[1].toLowerCase()))
						.filter(subCommandName -> !subCommandName.equalsIgnoreCase(this.name))
						.collect(Collectors.toList())
				: Collections.emptyList();
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// if command sender does not have permission to display help, output error message and return true
		if (!sender.hasPermission(permissionNode))
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_HELP).send();
			return true;
		}

		// if no arguments, display usage for all commands
		if (args.isEmpty())
		{
			displayUsageAll(sender);
			return true;
		}

		// display subcommand help message or invalid command message
		subcommandRegistry.getSubcommand(args.getFirst()).ifPresentOrElse(
				subcommand -> sendCommandHelpMessage(sender, subcommand),
				() -> sendCommandInvalidMessage(sender)
		);

		return true;
	}


	/**
	 * Send help description for subcommand to command sender with subcommand permission node,
	 * otherwise send invalid command message
	 *
	 * @param sender the command sender
	 * @param subcommand the subcommand to display help description
	 */
	private void sendCommandHelpMessage(final CommandSender sender, final Subcommand subcommand)
	{
		if (sender.hasPermission(subcommand.getPermissionNode()))
		{
			ctx.messageBuilder().compose(sender, subcommand.getDescription()).send();
			subcommand.displayUsage(sender);
		}
		else
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_INVALID);
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_HELP_INVALID).send();
		}
	}


	/**
	 * Send invalid subcommand message to command sender and display usage for all subcommands
	 *
	 * @param sender the command sender
	 */
	private void sendCommandInvalidMessage(CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_HELP_INVALID).send();
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_INVALID);
		displayUsageAll(sender);
	}


	/**
	 * Display usage message for all commands
	 *
	 * @param sender the command sender
	 */
	void displayUsageAll(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_HELP_USAGE_HEADER).send();

		subcommandRegistry.getNames().stream()
				.map(subcommandRegistry::getSubcommand)
				.filter(Optional::isPresent)
				.filter(subcommand -> sender.hasPermission(subcommand.get().getPermissionNode()))
				.forEach(subcommand -> subcommand.get().displayUsage(sender));
	}

}
