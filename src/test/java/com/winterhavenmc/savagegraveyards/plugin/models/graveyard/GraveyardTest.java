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
import com.winterhavenmc.savagegraveyards.plugin.models.location.ImmutableLocation;

import com.winterhavenmc.savagegraveyards.plugin.models.world.ImmutableWorld;
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
	@Mock
	ImmutableLocation.Valid validLocationMock;
	@Mock
	ImmutableWorld.Available availableWorldMock;


	@Nested
	class StaticFactory1Tests
	{
		@Test
		void of_throws_exception_given_null_plugin()
		{
			// Arrange
			DisplayName.Valid displayName = new DisplayName.Valid("Display Name");

			// Act
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> Graveyard.of(null, displayName, playerMock));

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
			assertEquals(GraveyardReason.DISPLAY_NAME_NULL, ((Graveyard.Invalid) result).graveyardReason());

			// Verify
			verify(pluginMock, atLeastOnce()).getConfig();
			verify(playerMock, atLeastOnce()).getWorld();
			verify(playerMock, atLeastOnce()).getLocation();
		}


		@Test
		void of_returns_Invalid_given_null_player()
		{
			// Arrange
			DisplayName.Valid displayName = new DisplayName.Valid("Display Name");

			// Act
			Graveyard graveyard = Graveyard.of(pluginMock, displayName, null);

			// Assert
			assertInstanceOf(Graveyard.Invalid.class, graveyard);
			assertEquals(GraveyardReason.PLAYER_NULL, ((Graveyard.Invalid) graveyard).graveyardReason());
		}


		@Test
		void of_returns_Valid_given_valid_parameters()
		{
			// Arrange
			DisplayName.Valid displayName = new DisplayName.Valid("Display Name");
			FileConfiguration configuration = new YamlConfiguration();
			when(pluginMock.getConfig()).thenReturn(configuration);
			when(playerMock.getWorld()).thenReturn(worldMock);
			when(playerMock.getLocation()).thenReturn(locationMock);

			// Act
			Graveyard result = Graveyard.of(pluginMock, displayName, playerMock);

			// Assert
			assertInstanceOf(Graveyard.Valid.class, result);
			assertEquals("Display Name", result.displayName().colorString());

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
			assertEquals(GraveyardReason.DISPLAY_NAME_NULL, ((Graveyard.Invalid) result).graveyardReason());

			// Verify
			verify(validLocationMock, atLeastOnce()).world();
			verify(availableWorldMock, atLeastOnce()).name();
		}


		@Test
		void of_returns_Valid_given_valid_parameters()
		{
			// Arrange
			DisplayName.Valid displayName = new DisplayName.Valid("Display Name");
			when(validLocationMock.world()).thenReturn(availableWorldMock);
			when(availableWorldMock.name()).thenReturn("mock world");

			// Act
			Graveyard result = Graveyard.of(displayName, attributesMock, validLocationMock);

			// Assert
			assertInstanceOf(Graveyard.Valid.class, result);
			assertEquals("Display Name", result.displayName().colorString());
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
		DisplayName.Valid displayName = new DisplayName.Valid("Display Name");
		UUID uid = new UUID(42, 42);
		when(validLocationMock.world()).thenReturn(availableWorldMock);

		try (MockedStatic<Bukkit> mocked = mockStatic(Bukkit.class))
		{
			mocked.when(() -> Bukkit.getWorld(uid)).thenReturn(worldMock);

			// Act
			Graveyard result = Graveyard.of(displayName, attributesMock, validLocationMock);

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
		// Arrange
		DisplayName.Valid displayName = new DisplayName.Valid("Display &aName&r");

		// Act
		Graveyard result = Graveyard.of(displayName, attributesMock, validLocationMock);

		// Assert
		assertInstanceOf(Graveyard.Valid.class, result);
		assertEquals("Display &aName&r", result.displayName().colorString());
	}


	@Test
	void displayName_returns_valid_DisplayName_without_color()
	{
		// Arrange
		DisplayName.Valid displayName = new DisplayName.Valid("Display &aName&r");

		// Act
		Graveyard result = Graveyard.of(displayName, attributesMock, validLocationMock);

		// Assert
		assertInstanceOf(Graveyard.Valid.class, result);
		assertEquals("Display Name", result.displayName().noColorString());
	}


	@Test
	void searchKey_returns_valid_searchKey()
	{
		// Arrange
		DisplayName.Valid displayName = new DisplayName.Valid("Display &aName&r");

		// Act
		Graveyard result = Graveyard.of(displayName, attributesMock, validLocationMock);

		// Assert
		assertInstanceOf(Graveyard.Valid.class, result);
		assertEquals("Display_Name", ((Graveyard.Valid) result).searchKey().string());
	}

}
