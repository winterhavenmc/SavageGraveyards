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

import com.winterhavenmc.savagegraveyards.core.util.Config;
import com.winterhavenmc.savagegraveyards.core.util.Macro;
import com.winterhavenmc.savagegraveyards.core.util.MessageId;

import com.winterhavenmc.savagegraveyards.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.models.graveyard.ValidGraveyard;
import com.winterhavenmc.savagegraveyards.models.graveyard.InvalidGraveyard;
import com.winterhavenmc.savagegraveyards.models.world.UnavailableWorld;

import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.function.Predicate;


/**
 * List command implementation<br>
 * Displays listing of graveyards
 */
public final class ListSubcommand extends AbstractSubcommand
{
	private final CommandCtx ctx;


	/**
	 * Class constructor
	 */
	public ListSubcommand(final CommandCtx ctx)
	{
		this.ctx = ctx;
		this.name = "list";
		this.usageString = "/graveyard list [page]";
		this.description = MessageId.COMMAND_DESCRIPTION_LIST;
		this.permissionNode = "graveyard.list";
		this.maxArgs = 1;
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// check sender permission
		if (!sender.hasPermission(permissionNode))
		{
			return ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_PERMISSION_LIST).send();
		}

		// validate arguments
		if (args.size() > maxArgs)
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER).send();
			displayUsage(sender);
			return true;
		}

		// get undiscovered searchKeys for player
		Set<String> undiscoveredKeys = ctx.graveyards().getUndiscoveredKeys(sender);

		// filter graveyards to be displayed in list
		List<Graveyard> displayRecords = ctx.graveyards().getAll()
				.filter(allowInvalidIfPermitted(sender))
				.filter(isEnabledOrPermitted(sender))
				.filter(isDiscoveredOrPermitted(sender, undiscoveredKeys))
				.filter(hasGroupPermission(sender))
				.toList();

		// if display list is empty, output list empty message and return
		if (displayRecords.isEmpty())
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_LIST_EMPTY).send();
		}
		else
		{
			int itemsPerPage = Config.LIST_PAGE_SIZE.getInt(ctx.plugin().getConfig());

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

		// return true to suppress display of bukkit command usage
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
				case InvalidGraveyard invalid ->
						ctx.messageBuilder().compose(sender, MessageId.COMMAND_LIST_ITEM_INVALID_WORLD)
								.setMacro(Macro.ITEM_NUMBER, displayItemNumber)
								.setMacro(Macro.GRAVEYARD, invalid)
								.setMacro(Macro.INVALID_WORLD, invalid.worldName())
								.send();

				case ValidGraveyard valid ->
				{
					// display unavailable list item
					if (valid.location().world() instanceof UnavailableWorld)
					{
						ctx.messageBuilder().compose(sender, MessageId.COMMAND_LIST_ITEM_UNAVAILABLE)
								.setMacro(Macro.ITEM_NUMBER, displayItemNumber)
								.setMacro(Macro.GRAVEYARD, valid)
								.setMacro(Macro.INVALID_WORLD, valid.location().world().name())
								.send();
					}

					// display disabled list item
					if (!valid.attributes().enabled().value())
					{
						ctx.messageBuilder().compose(sender, MessageId.COMMAND_LIST_ITEM_DISABLED)
								.setMacro(Macro.ITEM_NUMBER, displayItemNumber)
								.setMacro(Macro.GRAVEYARD, valid)
								.send();
					}

					// display undiscovered list item
					else if (valid.attributes().hidden().value() && undiscoveredKeys.contains(valid.displayName().noColorString()))
					{
						ctx.messageBuilder().compose(sender, MessageId.COMMAND_LIST_ITEM_UNDISCOVERED)
								.setMacro(Macro.ITEM_NUMBER, displayItemNumber)
								.setMacro(Macro.GRAVEYARD, valid)
								.send();
					}

					// display normal list item
					else
					{
						ctx.messageBuilder().compose(sender, MessageId.COMMAND_LIST_ITEM)
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
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_LIST_HEADER)
				.setMacro(Macro.PAGE_NUMBER, page)
				.setMacro(Macro.PAGE_TOTAL, pageCount)
				.send();
	}


	void displayListFooter(final CommandSender sender, final int page, final int pageCount)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_LIST_FOOTER)
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
		return graveyard -> !(graveyard instanceof InvalidGraveyard)
				|| sender.hasPermission("graveyard.list.disabled");
	}


	static Predicate<Graveyard> isEnabledOrPermitted(CommandSender sender)
	{
		return graveyard -> !(graveyard instanceof ValidGraveyard valid)
				|| valid.attributes().enabled().value()
				|| sender.hasPermission("graveyard.list.disabled");
	}


	static Predicate<Graveyard> isDiscoveredOrPermitted(CommandSender sender, Set<String> undiscovered)
	{
		return graveyard -> !(graveyard instanceof ValidGraveyard valid)
				|| !valid.attributes().hidden().value()
				|| !undiscovered.contains(valid.displayName().noColorString())
				|| sender.hasPermission("graveyard.list.hidden");
	}


	static Predicate<Graveyard> hasGroupPermission(CommandSender sender)
	{
		return graveyard -> {
			if (!(graveyard instanceof ValidGraveyard valid)) return true;
			String group = valid.attributes().group().value();
			return group == null || group.isEmpty() || sender.hasPermission("group." + group);
		};
	}

}
