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
import com.winterhavenmc.savagegraveyards.util.Macro;
import com.winterhavenmc.savagegraveyards.util.MessageId;
import com.winterhavenmc.savagegraveyards.util.SoundId;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;


/**
 * Forget command implementation<br>
 * Removes graveyard discovery record for player
 */
final class ForgetSubcommand extends AbstractSubcommand implements Subcommand
{
	private final PluginMain plugin;


	/**
	 * Class constructor
	 * @param plugin reference to plugin main class instance
	 */
	ForgetSubcommand(final PluginMain plugin)
	{
		this.plugin = Objects.requireNonNull(plugin);
		this.name = "forget";
		this.usageString = "/graveyard forget <player> <graveyard name>";
		this.description = MessageId.COMMAND_HELP_FORGET;
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
			case 2 -> plugin.getServer().matchPlayer(args[1]).stream().map(Player::getName).sorted().limit(20).toList();
			case 3 -> plugin.dataStore.selectMatchingGraveyardNames(args[2]);
			default -> Collections.emptyList();
		};
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// check for permission
		if (!sender.hasPermission(permissionNode))
		{
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_FORGET).send();
			return true;
		}

		// check for minimum arguments
		if (args.size() < minArgs)
		{
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			displayUsage(sender);
			return true;
		}

		String playerName = args.removeFirst();
		String displayName = String.join(" ", args).trim();

		// match playerName to offline player
		Arrays.stream(plugin.getServer().getOfflinePlayers())
				.filter(player -> playerName.equals(player.getName()))
				.findFirst()
				.ifPresentOrElse(player -> deleteDiscovery(sender, player, displayName),
						() -> deleteFailed(sender));

		return true;
	}

	private void deleteFailed(CommandSender sender)
	{
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_FORGET_PLAYER_NOT_FOUND).send();
	}


	private void deleteDiscovery(CommandSender sender, OfflinePlayer player, String displayName)
	{
		if (plugin.dataStore.deleteDiscovery(displayName, player.getUniqueId()))
		{
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_FORGET);
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_FORGET)
					.setMacro(Macro.GRAVEYARD, displayName)
					.setMacro(Macro.TARGET_PLAYER, player.getName())
					.send();
		}
		else
		{
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_FORGET)
					.setMacro(Macro.GRAVEYARD, displayName)
					.setMacro(Macro.TARGET_PLAYER, player.getName())
					.send();
		}
	}

}
