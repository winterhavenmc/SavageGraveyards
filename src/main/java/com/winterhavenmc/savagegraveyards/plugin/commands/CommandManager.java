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
import com.winterhavenmc.savagegraveyards.plugin.util.Macro;
import com.winterhavenmc.savagegraveyards.plugin.util.MessageId;
import com.winterhavenmc.savagegraveyards.plugin.util.SoundId;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;


/**
 * Implements command executor for SavageGraveyards commands.
 */
public final class CommandManager implements TabExecutor
{
	private final PluginMain plugin;
	private final SubcommandRegistry subcommandRegistry = new SubcommandRegistry();


	/**
	 * constructor method for {@code CommandManager} class
	 *
	 * @param plugin reference to main class
	 */
	public CommandManager(final PluginMain plugin)
	{
		this.plugin = plugin;
		Objects.requireNonNull(plugin.getCommand("graveyard")).setExecutor(this);
		Arrays.stream(SubcommandType.values()).forEach(type -> subcommandRegistry.register(type.create(plugin)));
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
		// if more than one argument, use subcommand tab completer
		if (args.length > 1)
		{
			Optional<Subcommand> subcommand = subcommandRegistry.getSubcommand(args[0]);

			return (subcommand.isPresent())
					? subcommand.get().onTabComplete(sender, command, alias, args)
					: Collections.emptyList();
		}

		// return list of matching subcommand names for which sender has permission
		return matchingNames(sender, args[0]);
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
		List<String> argsList = new ArrayList<>(Arrays.asList(args));
		String subcommandName = getSubcommandNameOrDefault(argsList);
		getSubcommandOrFallback(sender, subcommandName).ifPresent(subcommand -> subcommand.onCommand(sender, argsList));

		return true;
	}


	private String getSubcommandNameOrDefault(List<String> argsList)
	{
		return (!argsList.isEmpty())
				? argsList.removeFirst()
				: "help";
	}


	private Optional<Subcommand> getSubcommandOrFallback(final CommandSender sender,
	                                           final String subcommandName)
	{
		Optional<Subcommand> subcommand = subcommandRegistry.getSubcommand(subcommandName);

		return (subcommand.isEmpty())
				? notifyInvalidCommand(sender, subcommandName)
				: subcommand;
	}


	private Optional<Subcommand> notifyInvalidCommand(final CommandSender sender, final String subcommandName)
	{
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_INVALID);
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_INVALID_COMMAND)
				.setMacro(Macro.COMMAND_NAME, subcommandName)
				.send();

		return subcommandRegistry.getSubcommand("help");
	}


	/**
	 * Get matching list of subcommand names for which sender has permission
	 *
	 * @param sender the command sender
	 * @param matchString the string prefix to match against command names
	 * @return List of String - command names that match prefix and sender permission
	 */
	private List<String> matchingNames(final CommandSender sender, final String matchString)
	{
		return subcommandRegistry.getNames().stream()
				.filter(hasPermission(sender))
				.filter(matchesPrefix(matchString))
				.toList();
	}


	private Predicate<String> hasPermission(final CommandSender sender)
	{
		return subcommandName -> subcommandRegistry.getSubcommand(subcommandName)
				.map(subcommand -> sender.hasPermission(subcommand.getPermissionNode()))
				.orElse(false);
	}


	private Predicate<String> matchesPrefix(final String prefix)
	{
		return subcommandName -> subcommandName.startsWith(prefix.toLowerCase());
	}

}
