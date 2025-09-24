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
import com.winterhavenmc.savagegraveyards.core.util.SoundId;
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
final class ClosestSubcommand extends AbstractSubcommand implements Subcommand
{
	private final CommandCtx ctx;


	/**
	 * Class constructor
	 */
	ClosestSubcommand(final CommandCtx ctx)
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
		if (!sender.hasPermission(permissionNode))
		{
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_CLOSEST).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		if (!(sender instanceof Player player))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		if (args.size() > maxArgs)
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			this.displayUsage(sender);
			return true;
		}


		List<ValidGraveyard> nearestGraveyards = ctx.graveyards().getNearestGraveyards(player);

		if (nearestGraveyards.isEmpty())
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_CLOSEST_NO_MATCH).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
		}
		else
		{
			ValidGraveyard graveyard = nearestGraveyards.getFirst();
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_CLOSEST)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.send();
		}

		return true;
	}

}
