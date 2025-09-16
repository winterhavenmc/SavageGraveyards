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

package com.winterhavenmc.savagegraveyards.core.util;

/**
 * A nested Enum that provides routines to convert between key naming conventions. The members of
 * this Enum use upper snake case, because they are constants, while the yaml file uses
 * lower kebab case for the key naming convention.
 * <p>
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
public enum Case
{
	UPPER_SNAKE()
			{
				public String convert(final String string)
				{
					return string.toUpperCase().replace('-', '_');
				}
			},
	LOWER_KEBAB()
			{
				public String convert(final String string)
				{
					return string.toLowerCase().replace('_', '-');
				}
			};

	public abstract String convert(final String string);

	String convert(final Config config)
	{
		return convert(config.name());
	}
}
