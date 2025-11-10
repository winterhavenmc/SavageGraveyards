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

import com.winterhavenmc.savagegraveyards.core.util.*;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;


/**
 * Status command implementation<br>
 * Display plugin settings
 */
public final class StatusSubcommand extends AbstractSubcommand
{
	private final CommandCtx ctx;


	/**
	 * Class constructor
	 */
	public StatusSubcommand(final CommandCtx ctx)
	{
		this.ctx = ctx;
		this.name = "status";
		this.usageString = "/graveyard status";
		this.description = MessageId.COMMAND_DESCRIPTION_STATUS;
		this.permissionNode = "graveyard.status";
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// check sender permission
		if (!sender.hasPermission(permissionNode))
		{
			return ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_PERMISSION_STATUS).send();
		}

		// output config settings
		displayStatusHeader(sender);
		displayPluginVersion(sender);
		displayDebug(sender);
		displayLanguage(sender);
		displayLocale(sender);
		displayTimezone(sender);
		displayDiscoveryRange(sender);
		displayDiscoveryInterval(sender);
		displaySafetyTime(sender);
		displayListItemPageSize(sender);
		displaySoundEffects(sender);
		displayEnabledWorlds(sender);
		displayStatusFooter(sender);

		// return true to suppress display of bukkit command usage
		return true;
	}


	private void displayStatusHeader(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_HEADER)
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
				.setMacro(Macro.LANGUAGE, ctx.messageBuilder().config().language())
				.send();
	}


	private void displayLocale(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_LOCALE)
				.setMacro(Macro.LOCALE, ctx.messageBuilder().config().languageTag().toString())
				.send();
	}


	private void displayTimezone(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_TIMEZONE)
				.setMacro(Macro.TIMEZONE, ctx.messageBuilder().config().zoneId().getId())
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


	private void displaySoundEffects(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_SOUND_EFFECTS)
				.setMacro(Macro.BOOLEAN, Config.SOUND_EFFECTS.getBoolean(ctx.plugin().getConfig()))
				.send();
	}


	private void displayEnabledWorlds(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_ENABLED_WORLDS)
				.setMacro(Macro.ENABLED_WORLDS, ctx.messageBuilder().worlds().enabledNames().toString())
				.send();
	}


	private void displayStatusFooter(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_FOOTER).send();
	}

}
