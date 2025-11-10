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

import com.winterhavenmc.savagegraveyards.core.ports.commands.CommandDispatcher;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.DiscoveryRepository;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.GraveyardRepository;
import com.winterhavenmc.savagegraveyards.core.tasks.discovery.ValidDiscoveryObserver;
import com.winterhavenmc.savagegraveyards.core.util.Macro;
import com.winterhavenmc.savagegraveyards.core.util.MessageId;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;


/**
 * Implements command executor for SavageGraveyards commands.
 */
public final class BukkitCommandDispatcher implements TabExecutor, CommandDispatcher
{
	private final MessageBuilder messageBuilder;
	private final SubcommandRegistry subcommandRegistry = new SubcommandRegistry();


	/**
	 * constructor method for {@code CommandDispatcher} class
	 */
	public BukkitCommandDispatcher(final JavaPlugin plugin,
	                               final MessageBuilder messageBuilder,
	                               final GraveyardRepository graveyards,
	                               final DiscoveryRepository discoveries,
	                               final ValidDiscoveryObserver validDiscoveryObserver)
	{
		this.messageBuilder = messageBuilder;
		Objects.requireNonNull(plugin.getCommand("graveyard")).setExecutor(this);
		CommandCtx ctx = new CommandCtx(plugin, messageBuilder, graveyards, discoveries, validDiscoveryObserver);
		Arrays.stream(SubcommandType.values()).forEach(type -> subcommandRegistry.register(type.create(ctx)));
		subcommandRegistry.register(new HelpSubcommand(ctx, subcommandRegistry));
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
	                         final @NotNull String[] args)
	{
		List<String> argsList = new ArrayList<>(Arrays.asList(args));
		String subcommandName = getSubcommandNameOrDefault(argsList);
		getSubcommandOrFallback(sender, subcommandName).ifPresent(subcommand -> subcommand.onCommand(sender, argsList));

		return true;
	}


	private String getSubcommandNameOrDefault(final List<String> argsList)
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
		messageBuilder.compose(sender, MessageId.COMMAND_FAIL_INVALID_COMMAND)
				.setMacro(Macro.INVALID_NAME, subcommandName)
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


	/**
	 * Returns predicate for determining if command sender has permission for command
	 *
	 * @param sender the command sender
	 * @return the predicate
	 */
	private Predicate<String> hasPermission(final CommandSender sender)
	{
		return subcommandName -> subcommandRegistry.getSubcommand(subcommandName)
				.map(subcommand -> sender.hasPermission(subcommand.getPermissionNode()))
				.orElse(false);
	}


	/**
	 * Returns a predicate for determining if a string prefix matches a subcommand name
	 *
 	 * @param prefix the string prefix to match
	 * @return the predicate
	 */
	private Predicate<String> matchesPrefix(final String prefix)
	{
		return subcommandName -> subcommandName.startsWith(prefix.toLowerCase());
	}

}
