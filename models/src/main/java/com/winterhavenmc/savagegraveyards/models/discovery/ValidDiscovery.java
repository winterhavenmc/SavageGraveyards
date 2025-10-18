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

import com.winterhavenmc.savagegraveyards.models.searchkey.ValidSearchKey;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;


public final class ValidDiscovery implements Discovery
{
	private final ValidSearchKey searchKey;
	private final UUID playerUid;
	private final Instant timestamp;


	ValidDiscovery(final ValidSearchKey searchKey, final UUID playerUid)
	{
		this.searchKey = searchKey;
		this.playerUid = playerUid;
		this.timestamp = Instant.now();
	}


	ValidDiscovery(final ValidSearchKey searchKey, final UUID playerUid, final Instant timestamp)
	{
		this.searchKey = searchKey;
		this.playerUid = playerUid;
		this.timestamp = timestamp;
	}


	public ValidSearchKey searchKey()
	{
		return searchKey;
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
		return Objects.equals(this.searchKey, that.searchKey) &&
				Objects.equals(this.playerUid, that.playerUid);
	}


	@Override
	public int hashCode()
	{
		return Objects.hash(searchKey, playerUid);
	}


	@Override
	public String toString()
	{
		return "ValidDiscovery[" +
				"searchKey=" + searchKey + ", " +
				"playerUid=" + playerUid + ']';
	}

}
