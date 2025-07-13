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

package com.winterhavenmc.savagegraveyards.plugin.models.graveyard;

import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.attributes.Attributes;
import com.winterhavenmc.savagegraveyards.plugin.models.location.ValidLocation;
import com.winterhavenmc.savagegraveyards.plugin.models.location.world.AvailableWorld;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class GraveyardTest
{
	@Mock Plugin pluginMock;
	@Mock Player playerMock;
	@Mock World worldMock;
	@Mock Location locationMock;
	@Mock Attributes attributesMock;
	@Mock ValidLocation validLocationMock;
	@Mock AvailableWorld availableWorldMock;


	@Nested
	class StaticFactory1Tests
	{
		@Test
		void of_throws_exception_given_null_plugin()
		{
			// Act
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> Graveyard.of(null, "Display Name", playerMock));

			// Assert
			assertEquals("The parameter 'plugin' cannot be null.", exception.getMessage());
		}


		@Test
		void of_returns_Invalid_given_null_displayName()
		{
			// Arrange
			FileConfiguration configuration = new YamlConfiguration();
			when(pluginMock.getConfig()).thenReturn(configuration);
			when(playerMock.getWorld()).thenReturn(worldMock);
			when(playerMock.getLocation()).thenReturn(locationMock);

			// Act
			Graveyard result = Graveyard.of(pluginMock, null, playerMock);

			// Assert
			assertInstanceOf(Graveyard.Invalid.class, result);
			assertEquals("The display name was null.", ((Graveyard.Invalid) result).reason());

			// Verify
			verify(pluginMock, atLeastOnce()).getConfig();
			verify(playerMock, atLeastOnce()).getWorld();
			verify(playerMock, atLeastOnce()).getLocation();
		}


		@Test
		void of_returns_Invalid_given_blank_displayName()
		{
			// Arrange
			FileConfiguration configuration = new YamlConfiguration();
			when(pluginMock.getConfig()).thenReturn(configuration);
			when(playerMock.getWorld()).thenReturn(worldMock);
			when(playerMock.getLocation()).thenReturn(locationMock);

			// Act
			Graveyard result = Graveyard.of(pluginMock, "", playerMock);

			// Assert
			assertInstanceOf(Graveyard.Invalid.class, result);
			assertEquals("The display name was blank.", ((Graveyard.Invalid) result).reason());

			// Verify
			verify(pluginMock, atLeastOnce()).getConfig();
			verify(playerMock, atLeastOnce()).getWorld();
			verify(playerMock, atLeastOnce()).getLocation();
		}


		@Test
		void of_throws_exception_given_null_player()
		{
			// Act
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> Graveyard.of(pluginMock, "Display Name", null));

			// Assert
			assertEquals("The parameter 'player' cannot be null.", exception.getMessage());
		}


		@Test
		void of_returns_Valid_given_valid_parameters()
		{
			// Arrange
			FileConfiguration configuration = new YamlConfiguration();
			when(pluginMock.getConfig()).thenReturn(configuration);
			when(playerMock.getWorld()).thenReturn(worldMock);
			when(playerMock.getLocation()).thenReturn(locationMock);

			// Act
			Graveyard result = Graveyard.of(pluginMock, "display name", playerMock);

			// Assert
			assertInstanceOf(Graveyard.Valid.class, result);
			assertEquals("display name", result.displayName().color());

			// Verify
			verify(pluginMock, atLeastOnce()).getConfig();
			verify(playerMock, atLeastOnce()).getWorld();
			verify(playerMock, atLeastOnce()).getLocation();
		}
	}


	@Nested
	class StaticFactory2Tests
	{
		@Test
		void of_returns_Invalid_given_null_displayName()
		{
			// Arrange
			when(validLocationMock.world()).thenReturn(availableWorldMock);
			when(availableWorldMock.name()).thenReturn("mock world");

			// Act
			Graveyard result = Graveyard.of(null, attributesMock, validLocationMock);

			// Assert
			assertInstanceOf(Graveyard.Invalid.class, result);
			assertEquals("The display name was null.", ((Graveyard.Invalid) result).reason());

			// Verify
			verify(validLocationMock, atLeastOnce()).world();
			verify(availableWorldMock, atLeastOnce()).name();
		}


		@Test
		void of_returns_Invalid_given_blank_displayName()
		{
			// Arrange
			when(validLocationMock.world()).thenReturn(availableWorldMock);
			when(availableWorldMock.name()).thenReturn("mock world");

			// Act
			Graveyard result = Graveyard.of("", attributesMock, validLocationMock);

			// Assert
			assertInstanceOf(Graveyard.Invalid.class, result);
			assertEquals("The display name was blank.", ((Graveyard.Invalid) result).reason());

			// Verify
			verify(validLocationMock, atLeastOnce()).world();
			verify(availableWorldMock, atLeastOnce()).name();
		}


		@Test
		void of_returns_Valid_given_valid_parameters()
		{
			// Arrange
			when(validLocationMock.world()).thenReturn(availableWorldMock);
			when(availableWorldMock.name()).thenReturn("mock world");

			// Act
			Graveyard result = Graveyard.of("display name", attributesMock, validLocationMock);

			// Assert
			assertInstanceOf(Graveyard.Valid.class, result);
			assertEquals("display name", result.displayName().color());
			assertEquals("mock world", result.worldName());

			// Verify
			verify(validLocationMock, atLeastOnce()).world();
			verify(availableWorldMock, atLeastOnce()).name();
		}
	}


	@Test
	void getLocation_returns_valid_Bukkit_Location()
	{
		// Arrange
		UUID uid = new UUID(42, 42);
		when(validLocationMock.world()).thenReturn(availableWorldMock);

		try (MockedStatic<Bukkit> mocked = mockStatic(Bukkit.class))
		{
			mocked.when(() -> Bukkit.getWorld(uid)).thenReturn(worldMock);

			// Act
			Graveyard result = Graveyard.of("display name", attributesMock, validLocationMock);

			// Assert
			assertInstanceOf(Graveyard.Valid.class, result);
			assertEquals(new Location(null, 0, 0, 0), ((Graveyard.Valid) result).getLocation());

			// Verify
			verify(validLocationMock, atLeastOnce()).world();
		}
	}


	@Test
	void displayName_returns_valid_DisplayName_with_color()
	{
		// Act
		Graveyard result = Graveyard.of("Display &aName&r", attributesMock, validLocationMock);

		// Assert
		assertInstanceOf(Graveyard.Valid.class, result);
		assertEquals("Display &aName&r", result.displayName().color());
	}


	@Test
	void displayName_returns_valid_DisplayName_without_color()
	{
		// Act
		Graveyard result = Graveyard.of("Display &aName&r", attributesMock, validLocationMock);

		// Assert
		assertInstanceOf(Graveyard.Valid.class, result);
		assertEquals("Display Name", result.displayName().noColor());
	}


	@Test
	void searchKey_returns_valid_searchKey()
	{
		// Act
		Graveyard result = Graveyard.of("Display &aName&r", attributesMock, validLocationMock);

		// Assert
		assertInstanceOf(Graveyard.Valid.class, result);
		assertEquals("Display_Name", ((Graveyard.Valid) result).searchKey().string());
	}

}
