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

import com.winterhavenmc.savagegraveyards.core.SavageGraveyardsPluginController;
import com.winterhavenmc.savagegraveyards.core.util.*;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.time.ZoneId;
import java.util.List;


/**
 * Status command implementation<br>
 * Display plugin settings
 */
final class StatusSubcommand extends AbstractSubcommand implements Subcommand
{
	private final SavageGraveyardsPluginController.CommandContextContainer ctx;


	/**
	 * Class constructor
	 */
	StatusSubcommand(final SavageGraveyardsPluginController.CommandContextContainer ctx)
	{
		this.ctx = ctx;
		this.name = "status";
		this.usageString = "/graveyard status";
		this.description = MessageId.COMMAND_HELP_STATUS;
		this.permissionNode = "graveyard.status";
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// if command sender does not have permission to view status, output error message and return true
		if (!sender.hasPermission(permissionNode)) {
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_STATUS).send();
			return true;
		}

		// output config settings
		displayStatusBanner(sender);
		displayPluginVersion(sender);
		displayDebug(sender);
		displayLanguage(sender);
		displayLocale(sender);
		displayTimezone(sender);
		displayDiscoveryRange(sender);
		displayDiscoveryInterval(sender);
		displaySafetyTime(sender);
		displayListItemPageSize(sender);
		displayEnabledWorlds(sender);
		displayStatusFooter(sender);

		// always return true to suppress bukkit usage message
		return true;
	}


	private void displayStatusBanner(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_BANNER)
				.setMacro(Macro.PLUGIN, ctx.plugin().getDescription().getName())
				.send();
	}


	private void displayPluginVersion(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_PLUGIN_VERSION)
				.setMacro(Macro.VERSION, ctx.plugin().getDescription().getVersion())
				.send();
	}


	private void displayDebug(final CommandSender sender)
	{
		if (Config.DEBUG.getBoolean(ctx.plugin().getConfig())) {
			sender.sendMessage(ChatColor.DARK_RED + "DEBUG: true");
		}
	}


	private void displayLanguage(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_LANGUAGE)
				.setMacro(Macro.LANGUAGE, Config.LANGUAGE.getString(ctx.plugin().getConfig()))
				.send();
	}


	private void displayLocale(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_LOCALE)
				.setMacro(Macro.LOCALE, Config.LOCALE.getString(ctx.plugin().getConfig()))
				.send();
	}


	private void displayTimezone(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_TIMEZONE)
				.setMacro(Macro.TIMEZONE, ctx.plugin().getConfig().getString("timezone", ZoneId.systemDefault().toString()))
				.send();
	}

	private void displayDiscoveryRange(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_DISCOVERY_RANGE)
				.setMacro(Macro.NUMBER, Config.DISCOVERY_RANGE.getInt(ctx.plugin().getConfig()))
				.send();
	}


	private void displaySafetyTime(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_SAFETY_TIME)
				.setMacro(Macro.DURATION, Config.SAFETY_TIME.getSeconds(ctx.plugin().getConfig()))
				.send();
	}


	private void displayDiscoveryInterval(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_DISCOVERY_INTERVAL)
				.setMacro(Macro.DURATION, Config.DISCOVERY_INTERVAL.getSeconds(ctx.plugin().getConfig()))
				.send();
	}


	private void displayListItemPageSize(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_LIST_SIZE)
				.setMacro(Macro.NUMBER, Config.LIST_PAGE_SIZE.getInt(ctx.plugin().getConfig()))
				.send();
	}


	private void displayEnabledWorlds(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_ENABLED_WORLDS)
				.setMacro(Macro.ENABLED_WORLDS, ctx.worldManager().getEnabledWorldNames().toString())
				.send();
	}


	private void displayStatusFooter(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_FOOTER)
				.setMacro(Macro.PLUGIN, ctx.plugin().getDescription().getName())
				.setMacro(Macro.URL, "https://github.com/winterhavenmc/SavageGraveyards")
				.send();
	}
}
