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

package com.winterhavenmc.savagegraveyards.core.tasks;

import com.winterhavenmc.savagegraveyards.core.util.MessageId;
import com.winterhavenmc.library.messagebuilder.MessageBuilder;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class SafetyTask extends BukkitRunnable
{
	private final Player player;
	private final SafetyManager safetyManager;
	private final MessageBuilder messageBuilder;


	public SafetyTask(final SafetyManager safetyManager, final MessageBuilder messageBuilder, final Player player)
	{
		this.safetyManager = safetyManager;
		this.messageBuilder = messageBuilder;
		this.player = player;
	}


	public void run()
	{
		safetyManager.removePlayer(player);
		messageBuilder.compose(player, MessageId.SAFETY_COOLDOWN_END).send();
	}

}
