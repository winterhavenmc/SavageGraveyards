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

package com.winterhavenmc.savagegraveyards.plugin.models.location;

import com.winterhavenmc.savagegraveyards.plugin.models.world.ImmutableWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ImmutableLocationTest
{
	@Mock Player playerMock;
	@Mock World worldMock;
	@Mock Location locationMock;


	@Test
	void of_returns_ValidLocation_given_valid_player()
	{
		// Arrange
		when(playerMock.getWorld()).thenReturn(worldMock);
		when(playerMock.getLocation()).thenReturn(locationMock);

		// Act
		ImmutableLocation result = ImmutableLocation.of(playerMock);

		// Assert
		assertInstanceOf(ImmutableLocation.Valid.class, result);

		// Verify
		verify(playerMock, atLeastOnce()).getWorld();
		verify(playerMock, atLeastOnce()).getLocation();
	}


	@Test
	void of_returns_InvalidLocation_given_null_Bukkit_Location()
	{
		// Act
		ImmutableLocation result = ImmutableLocation.of((Location) null);

		// Assert
		assertInstanceOf(ImmutableLocation.Invalid.class, result);
		assertEquals("The location was null.", ((ImmutableLocation.Invalid) result).reason());
	}


	@Test
	void of_returns_InvalidLocation_given_Location_with_null_world()
	{
		// Act
		ImmutableLocation result = ImmutableLocation.of(locationMock);

		// Assert
		assertInstanceOf(ImmutableLocation.Invalid.class, result);
		assertEquals("The world was invalid: The world was null.", ((ImmutableLocation.Invalid) result).reason());
	}


	@Test
	void of_returns_ValidLocation_given_Location_with_valid_world()
	{
		// Arrange
		UUID uid = new UUID(42, 42);
		when(locationMock.getWorld()).thenReturn(worldMock);
		when(worldMock.getName()).thenReturn("mock world");
		when(worldMock.getUID()).thenReturn(uid);

		try (MockedStatic<Bukkit> mocked = mockStatic(Bukkit.class))
		{
			mocked.when(() -> Bukkit.getWorld(uid)).thenReturn(worldMock);

			// Act
			ImmutableLocation result = ImmutableLocation.of(locationMock);

		// Assert
		assertInstanceOf(ImmutableLocation.Valid.class, result);
		assertEquals("mock world", ((ImmutableLocation.Valid) result).world().name());
		assertEquals(new UUID(42, 42), ((ImmutableLocation.Valid) result).world().uid());
	}


	@Test
	void of_returns_InvalidLocation_given_null_world_name()
	{
		// Arrange
		UUID uid = new UUID(42, 42);

		// Act
		ImmutableLocation result = ImmutableLocation.of(null, uid, 1, 2, 3, 4, 5);

		// Assert
		assertInstanceOf(ImmutableLocation.Invalid.class, result);
		assertEquals("The world name was null.", ((ImmutableLocation.Invalid) result).reason());
	}


	@Test
	void of_returns_InvalidLocation_given_blank_world_name()
	{
		// Arrange
		UUID uid = new UUID(42, 42);

		// Act
		ImmutableLocation result = ImmutableLocation.of("", uid, 1, 2, 3, 4, 5);

		// Assert
		assertInstanceOf(ImmutableLocation.Invalid.class, result);
		assertEquals("The world name was blank.", ((ImmutableLocation.Invalid) result).reason());
	}


	@Test
	void of_returns_InvalidLocation_given_null_uuid()
	{
		// Arrange
		// Act
		ImmutableLocation result = ImmutableLocation.of("world name", null, 1, 2, 3, 4, 5);

		// Assert
		assertInstanceOf(ImmutableLocation.Invalid.class, result);
		assertEquals("The world UUID was null.", ((ImmutableLocation.Invalid) result).reason());
	}


	@Test
	void of_returns_ValidLocation_given_valid_parameters()
	{
		// Arrange
		String name = "world name";
		UUID uid = new UUID(42, 42);
		try (MockedStatic<Bukkit> mocked = mockStatic(Bukkit.class))
		{
			mocked.when(() -> Bukkit.getWorld(uid)).thenReturn(worldMock);

			// Act
			ImmutableLocation result = ImmutableLocation.of(name, uid, 1, 2, 3, 4, 5);

			// Assert
			assertInstanceOf(ImmutableLocation.Valid.class, result);
			assertEquals("world name", ((ImmutableLocation.Valid) result).world().name());
			assertEquals(new UUID(42, 42), ((ImmutableLocation.Valid) result).world().uid());
		}
	}


	@Test
	void of_returns_ValidLocation_when_Bukkit_returns_null_world()
	{
		// Arrange
		String name = "world name";
		UUID uid = new UUID(42, 42);
		try (MockedStatic<Bukkit> mocked = mockStatic(Bukkit.class))
		{
			mocked.when(() -> Bukkit.getWorld(uid)).thenReturn(null);

			// Act
			ImmutableLocation result = ImmutableLocation.of(name, uid, 1, 2, 3, 4, 5);

			// Assert
			assertInstanceOf(ImmutableLocation.Valid.class, result);
			assertEquals("world name", ((ImmutableLocation.Valid) result).world().name());
			assertEquals(new UUID(42, 42), ((ImmutableLocation.Valid) result).world().uid());
			assertInstanceOf(ImmutableWorld.Unavailable.class, ((ImmutableLocation.Valid) result).world());
		}
	}

}
