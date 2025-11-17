/*
 * Copyright (c) 2025 Tim Savage.
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

package com.winterhavenmc.savagegraveyards.models.discovery;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;


public final class ValidDiscovery implements Discovery
{
	private final UUID graveyardUid;
	private final UUID playerUid;
	private final Instant timestamp;


	/**
	 * Class constructor
	 */
	ValidDiscovery(final UUID graveyardUid, final UUID playerUid, final Instant timestamp)
	{
		this.graveyardUid = graveyardUid;
		this.playerUid = playerUid;
		this.timestamp = timestamp;
	}


	public UUID graveyardUid()
	{
		return this.graveyardUid;
	}


	public UUID playerUid()
	{
		return playerUid;
	}


	public Instant getTimestamp()
	{
		return this.timestamp;
	}


	@Override
	public boolean equals(final Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass())
		{
			return false;
		}
		var that = (ValidDiscovery) obj;
		return Objects.equals(this.graveyardUid, that.graveyardUid) &&
				Objects.equals(this.playerUid, that.playerUid);
	}


	@Override
	public int hashCode()
	{
		return Objects.hash(graveyardUid, playerUid);
	}


	@Override
	public String toString()
	{
		return "ValidDiscovery[" +
				"graveyardUid=" + graveyardUid + ", " +
				"playerUid=" + playerUid + ']';
	}

}
