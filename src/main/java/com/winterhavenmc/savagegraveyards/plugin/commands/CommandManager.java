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
import com.winterhavenmc.savagegraveyards.plugin.util.MessageId;
import com.winterhavenmc.savagegraveyards.plugin.util.SoundId;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import javax.annotation.Nonnull;
import java.util.*;


/**
 * Implements command executor for SavageGraveyards commands.
 */
public final class CommandManager implements TabExecutor
{
	// reference to main class
	private final PluginMain plugin;

	// map of subcommands
	private final SubcommandRegistry subcommandRegistry = new SubcommandRegistry();


	/**
	 * constructor method for {@code CommandManager} class
	 *
	 * @param plugin reference to main class
	 */
	public CommandManager(final PluginMain plugin)
	{
		// set reference to main class
		this.plugin = plugin;

		// register this class as command executor
		Objects.requireNonNull(plugin.getCommand("graveyard")).setExecutor(this);

		// register subcommands
		for (SubcommandType subcommandType : SubcommandType.values())
		{
			subcommandRegistry.register(subcommandType.create(plugin));
		}

		// register help command
		subcommandRegistry.register(new HelpSubcommand(plugin, subcommandRegistry));
	}


	/**
	 * Tab completer for SavageGraveyards commands
	 */
	@Override
	public List<String> onTabComplete(final @Nonnull CommandSender sender,
	                                  final @Nonnull Command command,
	                                  final @Nonnull String alias,
	                                  final String[] args)
	{
		// if more than one argument, use tab completer of subcommand
		if (args.length > 1)
		{
			// get subcommand from map
			Optional<Subcommand> optionalSubcommand = subcommandRegistry.getSubcommand(args[0]);

			// if no subcommand returned from map, return empty list
			if (optionalSubcommand.isEmpty())
			{
				return Collections.emptyList();
			}

			// unwrap optional subcommand
			Subcommand subcommand = optionalSubcommand.get();

			// return subcommand tab completer output
			return subcommand.onTabComplete(sender, command, alias, args);
		}

		// return list of subcommands for which sender has permission
		return matchingCommands(sender, args[0]);
	}


	/**
	 * Command Executor for SavageGraveyards
	 */
	@Override
	public boolean onCommand(final @Nonnull CommandSender sender,
	                         final @Nonnull Command command,
							 final @Nonnull String label,
							 final String[] args)
	{
		// convert args array to list
		List<String> argsList = new ArrayList<>(Arrays.asList(args));

		String subcommandName;

		// get subcommandName, remove from front of list
		if (!argsList.isEmpty())
		{
			subcommandName = argsList.removeFirst();
		}
		// if no arguments, set subcommandName to help
		else
		{
			subcommandName = "help";
		}

		subcommandRegistry.getSubcommand(subcommandName)
				.or(() -> {
						plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_INVALID_COMMAND).send();
						plugin.soundConfig.playSound(sender, SoundId.COMMAND_INVALID);
						return subcommandRegistry.getSubcommand("help");
					})
				.ifPresent(subcommand -> subcommand.onCommand(sender, argsList));

		return true;
	}


	/**
	 * Get matching list of subcommands for which sender has permission
	 *
	 * @param sender the command sender
	 * @param matchString the string prefix to match against command names
	 * @return List of String - command names that match prefix and sender has permission
	 */
	private List<String> matchingCommands(final CommandSender sender, final String matchString)
	{
		List<String> returnList = new ArrayList<>();

		for (String subcommandName : subcommandRegistry.getKeys())
		{
			Optional<Subcommand> subcommand = subcommandRegistry.getSubcommand(subcommandName);
			if (subcommand.isPresent())
			{
				if (sender.hasPermission(subcommand.get().getPermissionNode())
						&& subcommandName.startsWith(matchString.toLowerCase()))
				{
					returnList.add(subcommandName);
				}
			}
		}
		return returnList;
	}

}
