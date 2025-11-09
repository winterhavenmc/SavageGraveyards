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

import com.winterhavenmc.savagegraveyards.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.models.searchkey.SearchKey;
import com.winterhavenmc.savagegraveyards.models.searchkey.ValidSearchKey;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;


/**
 * Forget command implementation<br>
 * Removes graveyard discovery record for player
 */
public final class ForgetSubcommand extends AbstractSubcommand
{
	private final CommandCtx ctx;


	/**
	 * Class constructor
	 */
	public ForgetSubcommand(final CommandCtx ctx)
	{
		this.ctx = ctx;
		this.name = "forget";
		this.usageString = "/graveyard forget <player> <graveyard name>";
		this.description = MessageId.COMMAND_DESCRIPTION_FORGET;
		this.permissionNode = "graveyard.forget";
		this.minArgs = 2;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender,
	                                  final Command command,
	                                  final String alias,
	                                  final String[] args)
	{
		return switch (args.length)
		{
			case 2 -> ctx.plugin().getServer().matchPlayer(args[1]).stream().map(Player::getName).sorted().limit(20).toList();
			case 3 -> ctx.graveyards().getMatchingNames(args[2]);
			default -> Collections.emptyList();
		};
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// check sender permission
		if (!sender.hasPermission(permissionNode))
		{
			return ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_PERMISSION_FORGET).send();
		}

		// validate arguments
		if (args.size() < minArgs)
		{
			return sendMinArgsFailMessage(sender);
		}

		// perform logic
		String playerName = args.removeFirst();
		SearchKey searchKey = SearchKey.of(args);

		if (searchKey instanceof ValidSearchKey validSearchKey)
		{
			// match playerName to offline player
			Arrays.stream(ctx.plugin().getServer().getOfflinePlayers())
					.filter(player -> playerName.equals(player.getName()))
					.findFirst()
					.ifPresentOrElse(player -> deleteDiscovery(sender, player, validSearchKey),
							() -> sendDeleteFailMessage(sender));
		}

		// return true to suppress display of bukkit command usage
		return true;
	}


	private boolean sendMinArgsFailMessage(final CommandSender sender)
	{
		boolean result = ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
		displayUsage(sender);
		return result;
	}


	private void sendDeleteFailMessage(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_FORGET_PLAYER_NOT_FOUND).send();
	}


	@SuppressWarnings("UnusedReturnValue")
	private boolean deleteDiscovery(final CommandSender sender, final OfflinePlayer player, final ValidSearchKey searchKey)
	{
		Graveyard graveyard = ctx.graveyards().get(searchKey);

		return (ctx.discoveries().delete(searchKey, player.getUniqueId()))
				? ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_FORGET)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.setMacro(Macro.PLAYER, player)
					.send()
				: ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_FORGET)
					.setMacro(Macro.GRAVEYARD, searchKey.toDisplayName())
					.setMacro(Macro.PLAYER, player)
					.send();
	}

}
