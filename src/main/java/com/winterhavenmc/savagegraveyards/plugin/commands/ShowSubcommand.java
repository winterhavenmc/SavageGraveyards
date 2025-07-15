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
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.SearchKey;
import com.winterhavenmc.savagegraveyards.plugin.util.SoundId;
import com.winterhavenmc.savagegraveyards.plugin.util.Macro;
import com.winterhavenmc.savagegraveyards.plugin.util.MessageId;

import com.winterhavenmc.savagegraveyards.plugin.util.Config;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;


/**
 * Show command implementation<br>
 * displays graveyard settings
 */
final class ShowSubcommand extends AbstractSubcommand implements Subcommand
{
	private final PluginMain plugin;


	/**
	 * Class constructor
	 * @param plugin reference to plugin main class instance
	 */
	ShowSubcommand(final PluginMain plugin)
	{
		this.plugin = plugin;
		this.name = "show";
		this.usageString = "/graveyard show <graveyard>";
		this.description = MessageId.COMMAND_HELP_SHOW;
		this.permissionNode = "graveyard.show";
		this.minArgs = 1;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender,
	                                  final Command command,
									  final String alias,
									  final String[] args)
	{
		return (args.length == 2)
				? plugin.dataStore.selectMatchingGraveyardNames(args[1])
				: Collections.emptyList();
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// if command sender does not have permission to show graveyards, output error message and return true
		if (!sender.hasPermission(permissionNode))
		{
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_SHOW).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check minimum arguments
		if (args.size() < minArgs)
		{
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			displayUsage(sender);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		switch (SearchKey.of(args))
		{
			case SearchKey.Invalid invalidKey -> sendNotFoundMessage(sender, invalidKey);
			case SearchKey.Valid validKey ->
			{
				switch (plugin.dataStore.selectGraveyard(validKey))
				{
					case Graveyard.Invalid ignored -> sendNotFoundMessage(sender, validKey);
					case Graveyard.Valid valid ->
					{
						// display graveyard display name
						sender.sendMessage(ChatColor.DARK_AQUA + "Name: "
								+ ChatColor.RESET + valid.displayName().colorString());

						// display graveyard 'enabled' setting
						sender.sendMessage(ChatColor.DARK_AQUA + "Enabled: "
								+ ChatColor.RESET + valid.attributes().enabled().value());

						// display graveyard 'hidden' setting
						sender.sendMessage(ChatColor.DARK_AQUA + "Hidden: "
								+ ChatColor.RESET + valid.attributes().hidden().value());

						// if graveyard discovery range is set to non-negative value, display it; else display configured default
						if (valid.attributes().discoveryRange().value() >= 0)
						{
							sender.sendMessage(ChatColor.DARK_AQUA + "ValidDiscovery Range: "
									+ ChatColor.RESET + valid.attributes().discoveryRange().value() + " blocks");
						}
						else
						{
							sender.sendMessage(ChatColor.DARK_AQUA + "ValidDiscovery Range: "
									+ ChatColor.RESET + Config.DISCOVERY_RANGE.getInt(plugin.getConfig()) + " blocks (default)");
						}

						// get custom discovery message and display if not null or empty
						if (valid.attributes().discoveryMessage() != null && !valid.attributes().discoveryMessage().value().isEmpty())
						{
							sender.sendMessage(ChatColor.DARK_AQUA + "Custom ValidDiscovery Message: "
									+ ChatColor.RESET + valid.attributes().discoveryMessage());
						}

						// get custom respawn message and display if not null or empty
						if (valid.attributes().respawnMessage() != null && !valid.attributes().respawnMessage().value().isEmpty())
						{
							sender.sendMessage(ChatColor.DARK_AQUA + "Custom Respawn Message: "
									+ ChatColor.RESET + valid.attributes().respawnMessage());
						}

						// if graveyard safety time is set to non-negative value, display it; else display configured default
						if (valid.attributes().safetyTime().value().isPositive() || valid.attributes().safetyTime().value().isZero())
						{
							sender.sendMessage(ChatColor.DARK_AQUA + "Safety time: "
									+ ChatColor.RESET + valid.attributes().safetyTime().value().toSeconds() + " seconds");
						}
						else
						{
							sender.sendMessage(ChatColor.DARK_AQUA + "Safety time: "
									+ ChatColor.RESET + Config.SAFETY_TIME.getLong(plugin.getConfig()) + " seconds (default)");
						}

						// get graveyard group; if null or empty, set to ALL
						String group = (valid.attributes().group() != null && !valid.attributes().group().value().isBlank())
								? valid.attributes().group().value()
								: "ALL";

						sender.sendMessage(ChatColor.DARK_AQUA + "Group: " + ChatColor.RESET + group);

						// if world is invalid, set color to gray
						ChatColor worldColor = ChatColor.AQUA;
						if (valid.getLocation().getWorld() == null)
						{
							worldColor = ChatColor.GRAY;
						}

						// display graveyard location
						String locationString = ChatColor.DARK_AQUA + "Location: "
								+ ChatColor.RESET + "["
								+ worldColor + valid.worldName()
								+ ChatColor.RESET + "] "
								+ ChatColor.RESET + "X: " + ChatColor.AQUA + Math.round(valid.location().x()) + " "
								+ ChatColor.RESET + "Y: " + ChatColor.AQUA + Math.round(valid.location().y()) + " "
								+ ChatColor.RESET + "Z: " + ChatColor.AQUA + Math.round(valid.location().z()) + " "
								+ ChatColor.RESET + "P: " + ChatColor.GOLD + String.format("%.2f", valid.location().pitch()) + " "
								+ ChatColor.RESET + "Y: " + ChatColor.GOLD + String.format("%.2f", valid.location().yaw());
						sender.sendMessage(locationString);
					}
				}
			}
		}

		return true;
	}


	private void sendNotFoundMessage(CommandSender sender, SearchKey searchKey)
	{
		// send message
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_NO_RECORD)
				.setMacro(Macro.GRAVEYARD, searchKey.string())
				.send();

		// play sound
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
	}

}
