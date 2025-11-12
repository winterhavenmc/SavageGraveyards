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

package com.winterhavenmc.savagegraveyards.adapters.tasks.safety;

import com.winterhavenmc.savagegraveyards.ports.tasks.safety.SafetyManager;
import com.winterhavenmc.savagegraveyards.ports.tasks.safety.SafetyTask;
import com.winterhavenmc.savagegraveyards.models.MessageId;
import com.winterhavenmc.library.messagebuilder.MessageBuilder;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public final class BukkitSafetyTask extends BukkitRunnable implements SafetyTask
{
	private final Player player;
	private final SafetyManager safetyManager;
	private final MessageBuilder messageBuilder;


	public BukkitSafetyTask(final SafetyManager safetyManager, final MessageBuilder messageBuilder, final Player player)
	{
		this.safetyManager = safetyManager;
		this.messageBuilder = messageBuilder;
		this.player = player;
	}


	@Override
	public void run()
	{
		if (safetyManager instanceof BukkitSafetyManager bukkitSafetyManager)
		{
			bukkitSafetyManager.remove(player);
		}
		messageBuilder.compose(player, MessageId.SAFETY_COOLDOWN_END).send();
	}

}
