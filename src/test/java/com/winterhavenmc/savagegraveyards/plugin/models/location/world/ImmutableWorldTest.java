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

package com.winterhavenmc.savagegraveyards.plugin.models.location.world;

import com.winterhavenmc.savagegraveyards.plugin.models.location.world.AvailableWorld;
import com.winterhavenmc.savagegraveyards.plugin.models.location.world.ImmutableWorld;
import com.winterhavenmc.savagegraveyards.plugin.models.location.world.InvalidWorld;
import com.winterhavenmc.savagegraveyards.plugin.models.location.world.UnavailableWorld;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ImmutableWorldTest
{
	@Mock World worldMock;
	@Mock Player playerMock;


	@Test
	void of_returns_InvalidWorld_when_name_null()
	{
		// Arrange
		UUID uid = new UUID(42, 42);

		// Act
		ImmutableWorld result = ImmutableWorld.of(null, uid);

		// Assert
		assertInstanceOf(InvalidWorld.class, result);
		assertEquals("The world name was null.", ((InvalidWorld) result).reason());
	}


	@Test
	void of_returns_InvalidWorld_when_name_blank()
	{
		// Arrange
		String name = "";
		UUID uid = new UUID(42, 42);

		// Act
		ImmutableWorld result = ImmutableWorld.of(name, uid);

		// Assert
		assertInstanceOf(InvalidWorld.class, result);
		assertEquals("The world name was blank.", ((InvalidWorld) result).reason());
	}


	@Test
	void of_returns_InvalidWorld_when_uid_null()
	{
		// Arrange
		String name = "world name";

		// Act
		ImmutableWorld result = ImmutableWorld.of(name, null);

		// Assert
		assertInstanceOf(InvalidWorld.class, result);
		assertEquals("The world UUID was null.", ((InvalidWorld) result).reason());
	}


	@Test
	void of_returns_UnavailableWorld_when_bukkit_world_null()
	{
		// Arrange
		UUID uid = new UUID(42, 42);

		try (MockedStatic<Bukkit> mocked = mockStatic(Bukkit.class))
		{
			mocked.when(() -> Bukkit.getWorld(uid)).thenReturn(null);

			String name = "world";

			// Act
			ImmutableWorld result = ImmutableWorld.of(name, uid);

			// Assert
			assertInstanceOf(UnavailableWorld.class, result);
			assertEquals("world", ((UnavailableWorld) result).name());
			assertEquals(new UUID(42, 42), ((UnavailableWorld) result).uid());
		}
	}


	@Test
	void of_returns_AvailableWorld_when_bukkit_world_exists()
	{
		// Arrange
		String name = "world";
		UUID uid = new UUID(42, 42);

		try (MockedStatic<Bukkit> mocked = mockStatic(Bukkit.class))
		{
			mocked.when(() -> Bukkit.getWorld(uid)).thenReturn(worldMock);

			// Act
			ImmutableWorld result = ImmutableWorld.of(name, uid);

			// Assert
			assertInstanceOf(AvailableWorld.class, result);
			assertEquals("world", ((AvailableWorld) result).name());
			assertEquals(new UUID(42, 42), ((AvailableWorld) result).uid());
		}
	}


	@Test
	void of_returns_InvalidWorld_when_world_null()
	{
		// Arrange & Act
		ImmutableWorld result = ImmutableWorld.of((World) null);

		// Assert
		assertInstanceOf(InvalidWorld.class, result);
		assertEquals("The world was null.", ((InvalidWorld) result).reason());
	}


	@Test
	void of_returns_InvalidWorld_when_world_name_blank()
	{
		// Arrange
		String name = "";
		when(worldMock.getName()).thenReturn(name);

		// Act
		ImmutableWorld result = ImmutableWorld.of(worldMock);

		// Assert
		assertInstanceOf(InvalidWorld.class, result);
		assertEquals("The world name was blank.", ((InvalidWorld) result).reason());
	}


	@Test
	void of_returns_AvailableWorld_when_world_valid()
	{
		// Arrange
		String name = "mock world";
		UUID uid = new UUID(42, 42);
		when(worldMock.getName()).thenReturn(name);
		when(worldMock.getUID()).thenReturn(uid);

		// Act
		ImmutableWorld result = ImmutableWorld.of(worldMock);

		// Assert
		assertInstanceOf(AvailableWorld.class, result);
		assertEquals("mock world", ((AvailableWorld) result).name());
	}


	@Test
	void of_returns_AvailableWorld_when_player_valid()
	{
		// Arrange
		when(playerMock.getWorld()).thenReturn(worldMock);
		when(worldMock.getName()).thenReturn("mock world");
		when(worldMock.getUID()).thenReturn(new UUID(42, 42));

		// Act
		ImmutableWorld result = ImmutableWorld.of(playerMock);

		// Assert
		assertInstanceOf(AvailableWorld.class, result);
		assertEquals("mock world", ((AvailableWorld) result).name());
		assertEquals(new UUID(42, 42), ((AvailableWorld) result).uid());
	}

}
