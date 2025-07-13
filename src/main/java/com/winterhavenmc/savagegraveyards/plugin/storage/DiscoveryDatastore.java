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

package com.winterhavenmc.savagegraveyards.plugin.storage;

import com.winterhavenmc.savagegraveyards.plugin.models.discovery.Discovery;

import java.util.Collection;
import java.util.UUID;

public interface DiscoveryDatastore
{
	/**
	 * Insert discovery record
	 */
	boolean insertDiscovery(final Discovery.Valid discovery);


	/**
	 * Insert discovery records
	 *
	 * @param discoveries collection of valid records to be inserted
	 * @return number of records successfully inserted
	 */
	int insertDiscoveries(Collection<Discovery.Valid> discoveries);


	/**
	 * Delete discovery record
	 *
	 * @param displayName display name or search key of record to be deleted
	 * @param playerUid the player unique id
	 * @return boolean - {@code true} if deletion was successful, {@code false} if not
	 */
	boolean deleteDiscovery(String displayName, UUID playerUid);


}
