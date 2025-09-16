package com.winterhavenmc.savagegraveyards.core.ports.datastore;


public interface ConnectionProvider
{
	/**
	 * Initialize datastore
	 */
	ConnectionProvider connect();


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
