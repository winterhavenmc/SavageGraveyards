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

import com.winterhavenmc.savagegraveyards.core.context.CommandCtx;
import com.winterhavenmc.savagegraveyards.core.util.Macro;
import com.winterhavenmc.savagegraveyards.core.util.MessageId;
import com.winterhavenmc.savagegraveyards.models.graveyard.*;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;


/**
 * Closest command implementation<br>
 * Returns name of closest graveyard to player position
 */
public final class ClosestSubcommand extends AbstractSubcommand
{
	private final CommandCtx ctx;


	/**
	 * Class constructor
	 */
	public ClosestSubcommand(final CommandCtx ctx)
	{
		this.ctx = ctx;
		this.name = "closest";
		this.usageString = "/graveyard closest";
		this.description = MessageId.COMMAND_HELP_CLOSEST;
		this.permissionNode = "graveyard.closest";
		this.aliases = Set.of("nearest");
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// check sender permission
		if (!sender.hasPermission(permissionNode))
		{
			return ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_PERMISSION_CLOSEST).send();
		}

		// sender must be in game player
		else if (sender instanceof Player player)
		{
			// get list of nearest graveyards to player location, sorted by distance
			final List<ValidGraveyard> nearestGraveyards = ctx.graveyards().getNearestGraveyards(player);

			// if list is empty display no match message
			// else display command success message
			return (nearestGraveyards.isEmpty())
					? ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_CLOSEST_NO_MATCH).send()
					: ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_CLOSEST)
							.setMacro(Macro.GRAVEYARD, nearestGraveyards.getFirst())
							.send();
		}
		else
		{
			return ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
		}
	}

}
