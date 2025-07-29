package com.winterhavenmc.savagegraveyards.bootstrap;

import com.winterhavenmc.savagegraveyards.adapters.datastore.sqlite.SQLiteConnectionProvider;
import com.winterhavenmc.savagegraveyards.plugin.ports.datastore.ConnectionProvider;
import org.bukkit.plugin.Plugin;


public final class Bootstrap
{
	private Bootstrap() { /* private constructor to prevent instantiation */ }


	public static ConnectionProvider getConnectionProvider(Plugin plugin)
	{
		return new SQLiteConnectionProvider(plugin);
	}

}
