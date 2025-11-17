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

package com.winterhavenmc.savagegraveyards.datastore;

import com.winterhavenmc.savagegraveyards.models.discovery.ValidDiscovery;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;


public interface DiscoveryRepository
{
	/**
	 * Get all discovery records from a datastore using schema v0
	 *
	 * @return a {@code Set} of all valid discoveries contained in the datastore
	 */
	Set<ValidDiscovery> getAll();


	/**
	 * Save discovery record
	 *
	 * @param discovery a discovery record to save
	 * @return boolean {@code true} if the record was successfully saved, or {@code false} if not
	 */
	boolean save(ValidDiscovery discovery);


	/**
	 * Save all discovery records
	 *
	 * @param discoveries a Collection of valid discoveries
	 * @return the number of records saved
	 */
	int saveAll(Collection<ValidDiscovery> discoveries);


	/**
	 * Delete discovery record
	 *
	 * @param graveyardUid the graveyardUid of the discovery record to be deleted
	 * @param playerUid the playerUid of the discovery record to be deleted
	 * @return boolean {@code true} if deletion was successful, or {@code false} if not
	 */
	boolean delete(UUID graveyardUid, UUID playerUid);

}
