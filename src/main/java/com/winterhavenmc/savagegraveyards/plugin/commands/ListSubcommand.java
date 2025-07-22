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
import com.winterhavenmc.savagegraveyards.plugin.models.world.ImmutableWorld;
import com.winterhavenmc.savagegraveyards.plugin.util.SoundId;
import com.winterhavenmc.savagegraveyards.plugin.util.Macro;
import com.winterhavenmc.savagegraveyards.plugin.util.MessageId;

import com.winterhavenmc.savagegraveyards.plugin.util.Config;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.function.Predicate;


/**
 * List command implementation<br>
 * Displays listing of graveyards
 */
final class ListSubcommand extends AbstractSubcommand implements Subcommand
{
	private final PluginMain plugin;


	/**
	 * Class constructor
	 * @param plugin reference to plugin main class instance
	 */
	ListSubcommand(final PluginMain plugin)
	{
		this.plugin = Objects.requireNonNull(plugin);
		this.name = "list";
		this.usageString = "/graveyard list [page]";
		this.description = MessageId.COMMAND_HELP_LIST;
		this.permissionNode = "graveyard.list";
		this.maxArgs = 1;
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// if command sender does not have permission to list graveyards, output error message and return true
		if (!sender.hasPermission(permissionNode))
		{
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_LIST).send();
			return true;
		}

		// check maximum arguments
		if (args.size() > maxArgs)
		{
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER).send();
			displayUsage(sender);
			return true;
		}

		// get undiscovered searchKeys for player
		Set<String> undiscoveredKeys = plugin.dataStore.graveyards().getUndiscoveredKeys(sender);

		// filter graveyards to be displayed in list
		List<Graveyard> displayRecords = plugin.dataStore.graveyards().getAll()
				.filter(allowInvalidIfPermitted(sender))
				.filter(isEnabledOrPermitted(sender))
				.filter(isDiscoveredOrPermitted(sender, undiscoveredKeys))
				.filter(hasGroupPermission(sender))
				.toList();

		// if display list is empty, output list empty message and return
		if (displayRecords.isEmpty())
		{
			plugin.messageBuilder.compose(sender, MessageId.LIST_EMPTY).send();
		}
		else
		{
			int itemsPerPage = Config.LIST_PAGE_SIZE.getInt(plugin.getConfig());

			// get page count
			int pageCount = (displayRecords.size() + itemsPerPage - 1) / itemsPerPage;

			int page = (args.size() == 1)
					? parsePage(args.getFirst(), pageCount)
					: 1;

			int startIndex = ((page - 1) * itemsPerPage);
			int endIndex = Math.min((page * itemsPerPage), displayRecords.size());

			List<Graveyard> displayRange = displayRecords.subList(startIndex, endIndex);

			displayListHeader(sender, page, pageCount);
			displayListItems(sender, displayRange, startIndex, undiscoveredKeys);
			displayListFooter(sender, page, pageCount);
		}

		return true;
	}


	void displayListItems(final CommandSender sender,
	                              final List<Graveyard> displayRange,
	                              final int itemNumber,
	                              final Collection<String> undiscoveredKeys)
	{
		int displayItemNumber = itemNumber;

		for (Graveyard graveyard : displayRange)
		{
			// copy item number and increment
			displayItemNumber += 1;

			switch (graveyard)
			{
				case Graveyard.Invalid invalid ->
						plugin.messageBuilder.compose(sender, MessageId.LIST_ITEM_INVALID_WORLD)
								.setMacro(Macro.ITEM_NUMBER, displayItemNumber)
								.setMacro(Macro.GRAVEYARD, invalid)
								.setMacro(Macro.INVALID_WORLD, invalid.worldName())
								.send();

				case Graveyard.Valid valid ->
				{
					// display unavailable list item
					if (valid.location().world() instanceof ImmutableWorld.Unavailable)
					{
						plugin.messageBuilder.compose(sender, MessageId.LIST_ITEM_UNAVAILABLE)
								.setMacro(Macro.ITEM_NUMBER, displayItemNumber)
								.setMacro(Macro.GRAVEYARD, valid)
								.send();
					}

					// display disabled list item
					if (!valid.attributes().enabled().value())
					{
						plugin.messageBuilder.compose(sender, MessageId.LIST_ITEM_DISABLED)
								.setMacro(Macro.ITEM_NUMBER, displayItemNumber)
								.setMacro(Macro.GRAVEYARD, valid)
								.send();
					}

					// display undiscovered list item
					else if (valid.attributes().hidden().value() && undiscoveredKeys.contains(valid.displayName().noColorString()))
					{
						plugin.messageBuilder.compose(sender, MessageId.LIST_ITEM_UNDISCOVERED)
								.setMacro(Macro.ITEM_NUMBER, displayItemNumber)
								.setMacro(Macro.GRAVEYARD, valid)
								.send();
					}

					// display normal list item
					else
					{
						plugin.messageBuilder.compose(sender, MessageId.LIST_ITEM)
								.setMacro(Macro.ITEM_NUMBER, displayItemNumber)
								.setMacro(Macro.GRAVEYARD, valid)
								.send();
					}
				}
			}
		}
	}


	void displayListHeader(final CommandSender sender, final int page, final int pageCount)
	{
		plugin.messageBuilder.compose(sender, MessageId.LIST_HEADER)
				.setMacro(Macro.PAGE_NUMBER, page)
				.setMacro(Macro.PAGE_TOTAL, pageCount)
				.send();
	}


	void displayListFooter(final CommandSender sender, final int page, final int pageCount)
	{
		plugin.messageBuilder.compose(sender, MessageId.LIST_FOOTER)
				.setMacro(Macro.PAGE_NUMBER, page)
				.setMacro(Macro.PAGE_TOTAL, pageCount)
				.send();
	}


	static int parsePage(String string, int max)
	{
		if (string == null) return 1;
		try
		{
			int value = Integer.parseInt(string);
			return Math.max(1, Math.min(max, value));
		}
		catch (NumberFormatException exception)
		{
			return 1;
		}
	}


	static Predicate<Graveyard> allowInvalidIfPermitted(CommandSender sender)
	{
		return graveyard -> !(graveyard instanceof Graveyard.Invalid)
				|| sender.hasPermission("graveyard.list.disabled");
	}


	static Predicate<Graveyard> isEnabledOrPermitted(CommandSender sender)
	{
		return graveyard -> !(graveyard instanceof Graveyard.Valid valid)
				|| valid.attributes().enabled().value()
				|| sender.hasPermission("graveyard.list.disabled");
	}


	static Predicate<Graveyard> isDiscoveredOrPermitted(CommandSender sender, Set<String> undiscovered)
	{
		return graveyard -> !(graveyard instanceof Graveyard.Valid valid)
				|| !valid.attributes().hidden().value()
				|| !undiscovered.contains(valid.displayName().noColorString())
				|| sender.hasPermission("graveyard.list.hidden");
	}


	static Predicate<Graveyard> hasGroupPermission(CommandSender sender)
	{
		return graveyard -> {
			if (!(graveyard instanceof Graveyard.Valid valid)) return true;
			String group = valid.attributes().group().value();
			return group == null || group.isEmpty() || sender.hasPermission("group." + group);
		};
	}

}
