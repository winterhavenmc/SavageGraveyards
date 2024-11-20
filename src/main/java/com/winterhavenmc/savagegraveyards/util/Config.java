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

import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;


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
    CONSIDER_BEDSPAWN(Boolean.FALSE);

    private final Object defaultObject;


    /**
     * Constructor for Enum members
     * @param defaultObject the object passed
     * @param <T> type for object
     */
    <T> Config(final T defaultObject) {
        this.defaultObject = defaultObject;
    }

    /**
     * Get corresponding key for Enum member, formatted for style used in config.yml file
     * @return {@code String} the key as formatted in config.yml file
     */
    public String asKey() {
        return this.toLowerKebabCase();
    }

    /**
     * Get default value for key, matching exactly the corresponding string in the default config.yml file
     * @return {@code Object} the default object for the corresponding key
     */
    public Object getDefaultObject() {
        return this.defaultObject;
    }

    /**
     * Get value as boolean for corresponding key in current configuration
     * @param plugin {@code JavaPlugin} reference to the plugin instance
     * @return {@code boolean} the referenced value in the current configuration
     */
    public boolean getBoolean(final JavaPlugin plugin) {
        return plugin.getConfig().getBoolean(asKey());
    }

    /**
     * Get value as int for corresponding key in current configuration
     * @param plugin {@code JavaPlugin} reference to the plugin instance
     * @return {@code int} the referenced value in the current configuration
     */
    public int getInt(final JavaPlugin plugin) {
        return plugin.getConfig().getInt(asKey());
    }

    /**
     * Get value as int for corresponding key in current configuration
     * @param plugin {@code JavaPlugin} reference to the plugin instance
     * @return {@code long} the referenced value in the current configuration
     */
    public long getLong(final JavaPlugin plugin) {
        return plugin.getConfig().getLong(asKey());
    }

    /**
     * Get value as String for corresponding key in current configuration
     * @param plugin {@code JavaPlugin} reference to the plugin instance
     * @return {@code String} the referenced value in the current configuration
     */
    public String getString(final JavaPlugin plugin) {
        return plugin.getConfig().getString(asKey());
    }

    /**
     * Get value as List of String for corresponding key in current configuration
     * @param plugin {@code JavaPlugin} reference to the plugin instance
     * @return {@code List<String>} the referenced value in the current configuration
     */
    @SuppressWarnings("unused")
    public List<String> getStringList(final JavaPlugin plugin) {
        return plugin.getConfig().getStringList(asKey());
    }

    /**
     * Convert Enum member name to lower kebab case
     * @return {@code String} the Enum member name as lower kebab case
     */
    private String toLowerKebabCase() {
        return this.name().toLowerCase().replace('_', '-');
    }

}
