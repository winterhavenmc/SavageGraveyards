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

package com.winterhavenmc.savagegraveyards.commands.bukkit;

import com.winterhavenmc.savagegraveyards.models.Config;
import com.winterhavenmc.savagegraveyards.models.Macro;
import com.winterhavenmc.savagegraveyards.models.MessageId;

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
		displayDebugSetting(sender);
		displayLanguageSetting(sender);
		displayLocaleSetting(sender);
		displayTimezoneSetting(sender);
		displayDiscoveryRangeSetting(sender);
		displayDiscoveryIntervalSetting(sender);
		displaySafetyTimeSetting(sender);
		displayListItemPageSizeSetting(sender);
		displaySoundEffectsSetting(sender);
		displayEnabledWorldsSetting(sender);
		displayStatusFooterSetting(sender);

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


	private void displayDebugSetting(final CommandSender sender)
	{
		if (Config.DEBUG.getBoolean(ctx.plugin().getConfig())) {
			sender.sendMessage(ChatColor.DARK_RED + "DEBUG: true");
		}
	}


	private void displayLanguageSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_LANGUAGE)
				.setMacro(Macro.LANGUAGE, ctx.messageBuilder().config().language())
				.send();
	}


	@SuppressWarnings("UnusedReturnValue")
	private boolean displayLocaleSetting(final CommandSender sender)
	{
		return allEqual()
				? displaySimpleLocaleSetting(sender)
				: displayDetailedLocaleSetting(sender);
	}


	private boolean displaySimpleLocaleSetting(final CommandSender sender)
	{
		return ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_LOCALE)
				.setMacro(Macro.LOCALE, ctx.messageBuilder().config().locale().toLanguageTag())
				.send();
	}


	private boolean displayDetailedLocaleSetting(final CommandSender sender)
	{
		return ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_LOCALE_DETAIL)
				.setMacro(Macro.NUMBER_LOCALE, ctx.messageBuilder().config().numberLocale().toLanguageTag())
				.setMacro(Macro.DATE_LOCALE, ctx.messageBuilder().config().dateLocale().toLanguageTag())
				.setMacro(Macro.TIME_LOCALE, ctx.messageBuilder().config().timeLocale().toLanguageTag())
				.setMacro(Macro.LOG_LOCALE, ctx.messageBuilder().config().logLocale().toLanguageTag())
				.send();
	}

	private boolean allEqual()
	{
		return (ctx.messageBuilder().config().numberLocale().equals(ctx.messageBuilder().config().dateLocale())
				&& ctx.messageBuilder().config().numberLocale().equals(ctx.messageBuilder().config().timeLocale())
				&& ctx.messageBuilder().config().numberLocale().equals(ctx.messageBuilder().config().logLocale()));
	}


	private void displayTimezoneSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_TIMEZONE)
				.setMacro(Macro.TIMEZONE, ctx.messageBuilder().config().zoneId().getId())
				.send();
	}

	private void displayDiscoveryRangeSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_DISCOVERY_RANGE)
				.setMacro(Macro.NUMBER, Config.DISCOVERY_RANGE.getInt(ctx.plugin().getConfig()))
				.send();
	}


	private void displaySafetyTimeSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_SAFETY_TIME)
				.setMacro(Macro.DURATION, Config.SAFETY_TIME.getSeconds(ctx.plugin().getConfig()))
				.send();
	}


	private void displayDiscoveryIntervalSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_DISCOVERY_INTERVAL)
				.setMacro(Macro.DURATION, Config.DISCOVERY_INTERVAL.getSeconds(ctx.plugin().getConfig()))
				.send();
	}


	private void displayListItemPageSizeSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_LIST_SIZE)
				.setMacro(Macro.NUMBER, Config.LIST_PAGE_SIZE.getInt(ctx.plugin().getConfig()))
				.send();
	}


	private void displaySoundEffectsSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_SOUND_EFFECTS)
				.setMacro(Macro.BOOLEAN, Config.SOUND_EFFECTS.getBoolean(ctx.plugin().getConfig()))
				.send();
	}


	private void displayEnabledWorldsSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_ENABLED_WORLDS)
				.setMacro(Macro.ENABLED_WORLDS, ctx.messageBuilder().worlds().enabledNames().toString())
				.send();
	}


	private void displayStatusFooterSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_FOOTER).send();
	}

}
