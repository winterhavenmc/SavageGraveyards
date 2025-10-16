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
import com.winterhavenmc.savagegraveyards.core.util.Config;
import com.winterhavenmc.savagegraveyards.core.util.Macro;
import com.winterhavenmc.savagegraveyards.core.util.MessageId;
import com.winterhavenmc.savagegraveyards.core.util.SoundId;

import com.winterhavenmc.savagegraveyards.models.graveyard.ValidGraveyard;
import com.winterhavenmc.savagegraveyards.models.graveyard.InvalidGraveyard;
import com.winterhavenmc.savagegraveyards.models.searchkey.SearchKey;
import com.winterhavenmc.savagegraveyards.models.searchkey.ValidSearchKey;
import com.winterhavenmc.savagegraveyards.models.searchkey.InvalidSearchKey;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;


/**
 * Show command implementation<br>
 * displays graveyard settings
 */
public final class ShowSubcommand extends AbstractSubcommand
{
	private final CommandCtx ctx;


	/**
	 * Class constructor
	 */
	public ShowSubcommand(final CommandCtx ctx)
	{
		this.ctx = ctx;
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
				? ctx.graveyards().getMatchingNames(args[1])
				: Collections.emptyList();
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// if command sender does not have permission to show graveyards, output error message and return true
		if (!sender.hasPermission(permissionNode))
		{
			ctx.messageBuilder().sounds().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_SHOW).send();
			return true;
		}

		// check minimum arguments
		if (args.size() < minArgs)
		{
			ctx.messageBuilder().sounds().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			displayUsage(sender);
			return true;
		}

		switch (SearchKey.of(args))
		{
			case InvalidSearchKey invalidKey -> sendNotFoundMessage(sender, invalidKey);
			case ValidSearchKey validKey ->
			{
				switch (ctx.graveyards().get(validKey))
				{
					case InvalidGraveyard ignored -> sendNotFoundMessage(sender, validKey);
					case ValidGraveyard valid ->
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
							sender.sendMessage(ChatColor.DARK_AQUA + "Discovery Range: "
									+ ChatColor.RESET + valid.attributes().discoveryRange().value() + " blocks");
						}
						else
						{
							sender.sendMessage(ChatColor.DARK_AQUA + "Discovery Range: "
									+ ChatColor.RESET + Config.DISCOVERY_RANGE.getInt(ctx.plugin().getConfig()) + " blocks (default)");
						}

						// get custom discovery message and display if not null or empty
						if (valid.attributes().discoveryMessage() != null && !valid.attributes().discoveryMessage().value().isEmpty())
						{
							sender.sendMessage(ChatColor.DARK_AQUA + "Custom Discovery Message: "
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
									+ ChatColor.RESET + Config.SAFETY_TIME.getLong(ctx.plugin().getConfig()) + " seconds (default)");
						}

						// get graveyard group; if null or empty, set to ALL
						String group = (valid.attributes().group() != null && !valid.attributes().group().value().isBlank())
								? valid.attributes().group().value()
								: "ALL";

						sender.sendMessage(ChatColor.DARK_AQUA + "Group: " + ChatColor.RESET + group);

						// if world is invalid, set color to gray
						final String locationString = getLocationString(valid);
						sender.sendMessage(locationString);
					}
				}
			}
		}

		return true;
	}


	private static @NotNull String getLocationString(ValidGraveyard valid)
	{
		ChatColor worldColor = ChatColor.AQUA;
		if (valid.getLocation().getWorld() == null)
		{
			worldColor = ChatColor.GRAY;
		}

		// display graveyard location
		return ChatColor.DARK_AQUA + "Location: "
				+ ChatColor.RESET + "["
				+ worldColor + valid.worldName()
				+ ChatColor.RESET + "] "
				+ ChatColor.RESET + "X: " + ChatColor.AQUA + Math.round(valid.location().x()) + " "
				+ ChatColor.RESET + "Y: " + ChatColor.AQUA + Math.round(valid.location().y()) + " "
				+ ChatColor.RESET + "Z: " + ChatColor.AQUA + Math.round(valid.location().z()) + " "
				+ ChatColor.RESET + "P: " + ChatColor.GOLD + String.format("%.2f", valid.location().pitch()) + " "
				+ ChatColor.RESET + "Y: " + ChatColor.GOLD + String.format("%.2f", valid.location().yaw());
	}


	private void sendNotFoundMessage(CommandSender sender, SearchKey searchKey)
	{
		ctx.messageBuilder().sounds().playSound(sender, SoundId.COMMAND_FAIL);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_NO_RECORD)
				.setMacro(Macro.GRAVEYARD, searchKey.string())
				.send();
	}

}
