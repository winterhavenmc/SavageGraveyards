package com.winterhavenmc.savagegraveyards.core.ports.datastore;

import java.sql.SQLException;


public interface ConnectionProvider extends AutoCloseable
{
	/**
	 * Initialize datastore
	 */
	void connect() throws SQLException, ClassNotFoundException;


	/**
	 * Close SQLite datastore connection
	 */
	void close();


	/**
	 * Get instance of GraveyardRepository
	 *
	 * @return {@link GraveyardRepository}
	 */
	GraveyardRepository graveyards();


	/**
	 * Get instance of DiscoveryRepository
	 *
	 * @return {@link DiscoveryRepository}
	 */
	DiscoveryRepository discoveries();

}
