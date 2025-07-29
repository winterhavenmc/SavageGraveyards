package com.winterhavenmc.savagegraveyards.plugin.ports.datastore;

import org.bukkit.plugin.Plugin;
import java.sql.SQLException;


public interface ConnectionProvider
{
	GraveyardRepository graveyards();

	DiscoveryRepository discoveries();


	/**
	 * Initialize datastore
	 */
	void connect() throws SQLException, ClassNotFoundException;


	/**
	 * Close SQLite datastore connection
	 */
	void close();

}
