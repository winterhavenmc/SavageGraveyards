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

package com.winterhavenmc.savagegraveyards.models;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public enum FailReason implements Reason
{
	PARAMETER_NULL("The parameter ‘{0}’ was null."),
	PARAMETER_BLANK("The parameter ‘{0}’ was blank."),
	PARAMETER_INVALID("The parameter ‘{0}’ was invalid."),
	PARAMETER_NO_MATCH("No match found for ‘{0}’."),
	VALUE_NOT_FOUND("‘{0}’ not found."),
	INSERT_FAILED("Could not insert ‘{0}’ in datastore."),
	;


	private final String defaultMessage;


	FailReason(String defaultMessage)
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


	public String getLocalizedMessage(final Locale locale, final Object... objects)
	{
		try
		{
			final ResourceBundle bundle = ResourceBundle.getBundle(getClass().getSimpleName(), locale);
			final String pattern = bundle.getString(name());
			return MessageFormat.format(pattern, objects);
		}
		catch (MissingResourceException exception)
		{
			return MessageFormat.format(this.defaultMessage, objects);
		}
	}


	@Override
	public String toString()
	{
		return this.defaultMessage;
	}

}
