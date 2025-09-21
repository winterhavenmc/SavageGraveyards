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

package com.winterhavenmc.savagegraveyards.models.displayname;

import com.winterhavenmc.savagegraveyards.models.FailReason;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public enum DisplayNameFailReason implements FailReason
{
	PLAYER_NULL("The parameter 'player' cannot be null."),
	STRING_NULL("The parameter 'string' was null."),
	STRING_BLANK("The parameter 'string' was blank."),
	;

	private final String defaultMessage;


	DisplayNameFailReason(String defaultMessage)
	{
		this.defaultMessage = defaultMessage;
	}


	public String getLocalizedMessage(final Locale locale)
	{
		try
		{
			ResourceBundle bundle = ResourceBundle.getBundle(getClass().getSimpleName(), locale);
			return bundle.getString(name());
		}
		catch (MissingResourceException exception)
		{
			return this.defaultMessage;
		}
	}


	@Override
	public String toString()
	{
		return this.defaultMessage;
	}

}
