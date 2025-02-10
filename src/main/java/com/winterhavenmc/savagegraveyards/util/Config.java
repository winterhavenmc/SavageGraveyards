/*
 * Copyright (c) 2024 Tim Savage.
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

package com.winterhavenmc.savagegraveyards.util;

import com.winterhavenmc.savagegraveyards.storage.DataStoreType;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventPriority;

import java.util.List;
import java.util.Locale;


/**
 * An Enum who's members represent default config.yml keys, and the values passed to the constructor are
 * literals of the appropriate type, or string values when no appropriate type exists. The object passed
 * to the constructor is stored so it may be tested for type and value.
 * <p>
 * The Enum members may also be used to retrieve the current config setting for a key, providing type checking
 * of keys rather than string literals. A bukkit Configuration instance is passed, to allow the use of alternative
 * or testing configurations.
 * <p>
 * <i>example:</i>
 * <pre>
 * {@code int range = Config.DISCOVERY_RANGE.getInt(plugin.getConfig())}
 * </pre>
 */
public enum Config {

	DEBUG(Boolean.FALSE),
	LANGUAGE(Locale.US.toLanguageTag()),
	ENABLED_WORLDS(List.of()),
	DISABLED_WORLDS(List.of()),
	DEFAULT_ENABLED(Boolean.TRUE),
	DEFAULT_HIDDEN(Boolean.TRUE),
	SAFETY_TIME(15),
	DISCOVERY_RANGE(50),
	DISCOVERY_INTERVAL(40),
	LIST_PAGE_SIZE(5),
	RESPAWN_PRIORITY(EventPriority.NORMAL),
	TITLES_ENABLED(Boolean.TRUE),
	SOUND_EFFECTS(Boolean.TRUE),
	CONSIDER_BEDSPAWN(Boolean.FALSE),
	STORAGE_TYPE(DataStoreType.SQLITE);

	private final Object defaultObject;


	/**
	 * A nested Enum that provides routines to convert between key naming conventions. The members of
	 * this Enum use upper snake case, because they are constants, while the yaml file uses
	 * lower kebab case for the key naming convention.
	 * <P>
	 * There are overloaded methods providing for passing a String or an Enum member. All methods return {@code String}.
	 * <p>
	 * <i>examples:</i>
	 * <p>
	 * <pre>
	 * {@code
	 * String fileKey = Case.LOWER_KEBAB.convert(Config.SAFETY_TIME); // safety-time
	 * String enumKey = Case.UPPER_SNAKE.convert(fileKey); // SAFETY_TIME }
	 * </pre>
	 */
	public enum Case {
		UPPER_SNAKE() {
			public String convert(final String string) {
				return string.toUpperCase().replace('-','_');
			}
		},
		LOWER_KEBAB() {
			public String convert(final String string) {
				return string.toLowerCase().replace('_','-');
			}
		};

		public abstract String convert(final String string);
		String convert(final Config config) {
			return convert(config.name());
		}
	}


	/**
	 * Constructor for Enum members
	 *
	 * @param defaultObject the object passed
	 */
	Config(final Object defaultObject) {
		this.defaultObject = defaultObject;
	}

	/**
	 * Get corresponding key for Enum member, formatted for style used in config.yml file
	 *
	 * @return {@code String} the key as formatted in config.yml file
	 */
	public String asFileKey() {
		return this.toLowerKebabCase();
	}

	/**
	 * Convert Enum member name to lower kebab case
	 *
	 * @return {@code String} the Enum member name as lower kebab case
	 */
	private String toLowerKebabCase() {
		return Case.LOWER_KEBAB.convert(this);
	}

	/**
	 * Convert Enum member name to upper snake case (used for testing)
	 *
	 * @return {@code String} the Enum member name converted to upper snake case
	 */
	public String toUpperSnakeCase() {
		return Case.UPPER_SNAKE.convert(this);
	}

	/**
	 * Get default value for key, matching exactly the corresponding string in the default config.yml file
	 *
	 * @return {@code Object} the default object for the corresponding key
	 */
	public Object getDefaultObject() {
		return this.defaultObject;
	}

	/**
	 * Get value as boolean for corresponding key in current configuration
	 *
	 * @param configuration {@code Configuration} reference to the plugin current configuration instance
	 * @return {@code boolean} the referenced value in the current configuration
	 */
	public boolean getBoolean(final Configuration configuration) {
		return configuration.getBoolean(asFileKey());
	}

	/**
	 * Get value as int for corresponding key in current configuration
	 *
	 * @param configuration {@code Configuration} reference to the plugin current configuration instance
	 * @return {@code int} the referenced value in the current configuration
	 */
	public int getInt(final Configuration configuration) {
		return configuration.getInt(asFileKey());
	}

	/**
	 * Get value as int for corresponding key in current configuration
	 *
	 * @param configuration {@code Configuration} reference to the plugin current configuration instance
	 * @return {@code long} the referenced value in the current configuration
	 */
	public long getLong(final Configuration configuration) {
		return configuration.getLong(asFileKey());
	}

	/**
	 * Get value as String for corresponding key in current configuration
	 *
	 * @param configuration {@code Configuration} reference to the plugin current configuration instance
	 * @return {@code String} the referenced value in the current configuration
	 */
	public String getString(final Configuration configuration) {
		return configuration.getString(asFileKey());
	}

	/**
	 * Get value as List of String for corresponding key in current configuration
	 *
	 * @param configuration {@code Configuration} reference to the plugin current configuration instance
	 * @return {@code List<String>} the referenced value in the current configuration
	 */
	@SuppressWarnings("unused")
	public List<String> getStringList(final Configuration configuration) {
		return configuration.getStringList(asFileKey());
	}

}
