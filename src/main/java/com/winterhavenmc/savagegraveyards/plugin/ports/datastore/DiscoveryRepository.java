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

package com.winterhavenmc.savagegraveyards.plugin.ports.datastore;

import com.winterhavenmc.savagegraveyards.plugin.models.discovery.Discovery;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.SearchKey;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;


public interface DiscoveryRepository
{
	/**
	 * Insert discovery record
	 */
	boolean save(Discovery.Valid discovery);


	/**
	 * Insert discovery records
	 *
	 * @param discoveries collection of valid records to be inserted
	 * @return number of records successfully inserted
	 */
	int saveAll(Collection<Discovery.Valid> discoveries);


	boolean delete(SearchKey.Valid searchKey, UUID playerUid);

	Set<Discovery.Valid> getAll_v0();

	Set<Discovery.Valid> getAll_V1();
}
