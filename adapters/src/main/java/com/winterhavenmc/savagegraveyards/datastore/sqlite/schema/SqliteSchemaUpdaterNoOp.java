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

package com.winterhavenmc.savagegraveyards.datastore.sqlite.schema;

import com.winterhavenmc.library.messagebuilder.models.configuration.ConfigRepository;

import com.winterhavenmc.savagegraveyards.datastore.DatastoreMessage;

import org.bukkit.plugin.Plugin;


public final class SqliteSchemaUpdaterNoOp implements SchemaUpdater
{
	private final Plugin plugin;
	private final ConfigRepository configRepository;


	public SqliteSchemaUpdaterNoOp(final Plugin plugin, final ConfigRepository configRepository)
	{
		this.plugin = plugin;
		this.configRepository = configRepository;
	}


	@Override
	public void update()
	{
		plugin.getLogger().info(DatastoreMessage.SCHEMA_UP_TO_DATE_NOTICE.getLocalizedMessage(configRepository.locale(), DatastoreMessage.DATASTORE_NAME));
	}

}
