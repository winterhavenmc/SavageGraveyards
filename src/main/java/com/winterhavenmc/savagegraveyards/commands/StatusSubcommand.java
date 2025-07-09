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
import com.winterhavenmc.savagegraveyards.util.SoundId;
import com.winterhavenmc.savagegraveyards.util.MessageId;

import com.winterhavenmc.savagegraveyards.util.Config;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;


/**
 * Status command implementation<br>
 * Display plugin settings
 */
final class StatusSubcommand extends AbstractSubcommand implements Subcommand
{
	private final PluginMain plugin;


	/**
	 * Class constructor
	 * @param plugin reference to plugin main class instance
	 */
	StatusSubcommand(final PluginMain plugin)
	{
		this.plugin = Objects.requireNonNull(plugin);
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
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_STATUS).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// output config settings
		showStatusBanner(sender);
		showPluginVersion(sender);
		showDebugSetting(sender);
		showLanguageSetting(sender);
		showLocaleSetting(sender);
		showDiscoveryRangeSetting(sender);
		showDiscoveryIntervalSetting(sender);
		showSafetyTimeSetting(sender);
		showListItemPageSizeSetting(sender);
		showEnabledWorlds(sender);

		// always return true to suppress bukkit usage message
		return true;
	}

	private void showStatusBanner(final CommandSender sender)
	{
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_BANNER)
				.setMacro(Macro.PLUGIN, plugin.getDescription().getName())
				.send();
	}


	private void showPluginVersion(final CommandSender sender)
	{
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_PLUGIN_VERSION)
				.setMacro(Macro.VERSION, plugin.getDescription().getVersion())
				.send();
	}


	private void showDebugSetting(final CommandSender sender)
	{
		if (Config.DEBUG.getBoolean(plugin.getConfig())) {
			sender.sendMessage(ChatColor.DARK_RED + "DEBUG: true");
		}
	}


	private void showLanguageSetting(final CommandSender sender)
	{
		String languageSetting = plugin.getConfig().getString("language");
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_LANGUAGE)
				.setMacro(Macro.LANGUAGE, languageSetting)
				.send();
	}


	private void showLocaleSetting(final CommandSender sender)
	{
		String languageSetting = plugin.getConfig().getString("language");
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_LOCALE)
				.setMacro(Macro.LOCALE, languageSetting)
				.send();
	}


	private void showDiscoveryRangeSetting(final CommandSender sender)
	{
		int blocks = Config.DISCOVERY_RANGE.getInt(plugin.getConfig());
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_DISCOVERY_RANGE)
				.setMacro(Macro.NUMBER, blocks)
				.send();
	}


	private void showSafetyTimeSetting(final CommandSender sender)
	{
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_SAFETY_TIME)
				.setMacro(Macro.DURATION,  Config.SAFETY_TIME.getLong(plugin.getConfig()))
				.send();
	}


	private void showDiscoveryIntervalSetting(final CommandSender sender)
	{
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_DISCOVERY_INTERVAL)
				.setMacro(Macro.DURATION, Config.DISCOVERY_INTERVAL.getInt(plugin.getConfig()))
				.send();
	}


	private void showListItemPageSizeSetting(final CommandSender sender)
	{
		int items = Config.LIST_PAGE_SIZE.getInt(plugin.getConfig());
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_LIST_SIZE)
				.setMacro(Macro.NUMBER, items)
				.send();
	}


	private void showEnabledWorlds(final CommandSender sender)
	{
		String worldList = plugin.worldManager.getEnabledWorldNames().toString();
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_ENABLED_WORLDS)
				.setMacro(Macro.ENABLED_WORLDS, worldList)
				.send();
	}

}
