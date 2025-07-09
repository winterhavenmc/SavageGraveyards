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
import com.winterhavenmc.savagegraveyards.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.models.location.world.UnavailableWorld;
import com.winterhavenmc.savagegraveyards.util.SoundId;
import com.winterhavenmc.savagegraveyards.util.Macro;
import com.winterhavenmc.savagegraveyards.util.MessageId;

import com.winterhavenmc.savagegraveyards.util.Config;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;


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

		// set default page
		int page = 1;

		// if argument exists, try to parse as integer page number
		if (args.size() == 1)
		{
			try
			{
				page = Integer.parseInt(args.getFirst());
			}
			catch (NumberFormatException e)
			{
				// second argument not a page number, let default of 1 stand
			}
		}
		page = Math.max(1, page);

		int itemsPerPage = Config.LIST_PAGE_SIZE.getInt(plugin.getConfig());

		// get all records from datastore
		final Collection<Graveyard> allRecords = plugin.dataStore.selectAllGraveyards();

		if (Config.DEBUG.getBoolean(plugin.getConfig()))
		{
			plugin.getLogger().info("Records fetched from datastore: " + allRecords.size());
		}

		// get undiscovered searchKeys for player
		Collection<String> undiscoveredKeys = new HashSet<>();
		if (sender instanceof Player)
		{
			undiscoveredKeys.addAll(plugin.dataStore.selectUndiscoveredKeys((Player) sender));
		}

		// create empty list of records
		List<Graveyard> displayRecords = new ArrayList<>();

		for (Graveyard graveyard : allRecords)
		{
			switch (graveyard)
			{
				case Graveyard.Invalid invalid ->
				{
					if (sender.hasPermission("graveyard.list.disabled"))
					{
						displayRecords.add(invalid);
					}
				}

				case Graveyard.Valid valid ->
				{
					String group = valid.attributes().group();

					// if graveyard is not enabled and sender does not have override permission, do not add to display list
					if (!valid.attributes().enabled() && !sender.hasPermission("graveyard.list.disabled"))
					{
						if (Config.DEBUG.getBoolean(plugin.getConfig()))
						{
							plugin.getLogger().info(graveyard.displayName()
									+ " is disabled and player does not have graveyard.list.disabled permission.");
						}
					}

					// if graveyard is undiscovered and sender does not have override permission, do not add to display list
					else if (valid.attributes().hidden()
							&& undiscoveredKeys.contains(graveyard.searchKey())
							&& !sender.hasPermission("graveyard.list.hidden"))
					{
						if (Config.DEBUG.getBoolean(plugin.getConfig()))
						{
							plugin.getLogger().info(graveyard.displayName()
									+ " is undiscovered and player does not have graveyard.list.hidden permission.");
						}
					}

					// if graveyard has group set and sender does not have group permission, do not add to display list
					else if (group != null && !group.isEmpty() && !sender.hasPermission("group." + valid.attributes().group()))
					{
						if (Config.DEBUG.getBoolean(plugin.getConfig()))
						{
							plugin.getLogger().info(graveyard.displayName()
									+ " is in group that player does not have permission.");
						}
					}
					else
					{
						// add graveyard to display list
						displayRecords.add(graveyard);
					}
				}
			}

			// if display list is empty, output list empty message and return
			if (displayRecords.isEmpty())
			{
				plugin.messageBuilder.compose(sender, MessageId.LIST_EMPTY).send();
				return true;
			}
		}

		// get page count
		int pageCount = ((displayRecords.size() - 1) / itemsPerPage) + 1;
		if (page > pageCount)
		{
			page = pageCount;
		}
		int startIndex = ((page - 1) * itemsPerPage);
		int endIndex = Math.min((page * itemsPerPage), displayRecords.size());

		List<Graveyard> displayRange = displayRecords.subList(startIndex, endIndex);

		displayListHeader(sender, page, pageCount);
		displayListItems(sender, displayRange, startIndex, undiscoveredKeys);
		displayListFooter(sender, page, pageCount);

		return true;
	}

	private void displayListItems(CommandSender sender, List<Graveyard> displayRange, int itemNumber, Collection<String> undiscoveredKeys)
	{
		for (Graveyard graveyard1 : displayRange)
		{
			// increment item number
			itemNumber++;

			switch (graveyard1)
			{
				case Graveyard.Invalid invalid ->
						plugin.messageBuilder.compose(sender, MessageId.LIST_ITEM_INVALID_WORLD)
								.setMacro(Macro.ITEM_NUMBER, itemNumber)
								.setMacro(Macro.GRAVEYARD, invalid.displayName())
								.setMacro(Macro.INVALID_WORLD, invalid.worldName())
								.send();

				case Graveyard.Valid valid ->
				{
					// display unavailable list item
					if (valid.location().world() instanceof UnavailableWorld)
					{
						plugin.messageBuilder.compose(sender, MessageId.LIST_ITEM_UNAVAILABLE)
								.setMacro(Macro.GRAVEYARD, valid.displayName())
								.setMacro(Macro.LOCATION, valid.getLocation())
								.setMacro(Macro.ITEM_NUMBER, itemNumber)
								.send();
					}

					// display disabled list item
					if (!valid.attributes().enabled())
					{
						plugin.messageBuilder.compose(sender, MessageId.LIST_ITEM_DISABLED)
								.setMacro(Macro.GRAVEYARD, valid.displayName())
								.setMacro(Macro.LOCATION, valid.getLocation())
								.setMacro(Macro.ITEM_NUMBER, itemNumber)
								.send();
					}

					// display undiscovered list item
					else if (valid.attributes().hidden() && undiscoveredKeys.contains(valid.searchKey()))
					{
						plugin.messageBuilder.compose(sender, MessageId.LIST_ITEM_UNDISCOVERED)
								.setMacro(Macro.GRAVEYARD, valid.displayName())
								.setMacro(Macro.LOCATION, valid.getLocation())
								.setMacro(Macro.ITEM_NUMBER, itemNumber)
								.send();
					}

					// display normal list item
					else
					{
						plugin.messageBuilder.compose(sender, MessageId.LIST_ITEM)
								.setMacro(Macro.GRAVEYARD, valid.displayName())
								.setMacro(Macro.LOCATION, valid.getLocation())
								.setMacro(Macro.ITEM_NUMBER, itemNumber)
								.send();
					}
				}
			}
		}
	}

	private void displayListHeader(final CommandSender sender, final int page, final int pageCount)
	{
		plugin.messageBuilder.compose(sender, MessageId.LIST_HEADER)
				.setMacro(Macro.PAGE_NUMBER, page)
				.setMacro(Macro.PAGE_TOTAL, pageCount)
				.send();
	}


	private void displayListFooter(final CommandSender sender, final int page, final int pageCount)
	{
		plugin.messageBuilder.compose(sender, MessageId.LIST_FOOTER)
				.setMacro(Macro.PAGE_NUMBER, page)
				.setMacro(Macro.PAGE_TOTAL, pageCount)
				.send();
	}

}
