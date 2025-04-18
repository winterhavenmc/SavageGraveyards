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

package com.winterhavenmc.savagegraveyards;

import com.winterhavenmc.util.messagebuilder.MessageBuilder;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class PluginMainTests {

	@Mock PluginMain pluginMock;

	FileConfiguration pluginConfig;


	@BeforeEach
	public void setUp() {
		pluginConfig = new YamlConfiguration();
		pluginConfig.set("language", "en-US");
	}

	@AfterEach
	public void tearDown() {
	}


	@Nested
	@DisplayName("Test plugin main objects.")
	class PluginTests {

		@Test
		@DisplayName("message builder not null.")
		void messageBuilderNotNull() {
			when(pluginMock.getLogger()).thenReturn(Logger.getLogger(this.getClass().getName()));
			when(pluginMock.getConfig()).thenReturn(pluginConfig);
			MessageBuilder messageBuilder = MessageBuilder.create(pluginMock);
			assertNotNull(messageBuilder);
		}

		@Test
		@DisplayName("world manager not null.")
		void worldManagerNotNull() {
			assertNotNull(pluginMock.worldManager);
		}

		@Test
		@DisplayName("sound config not null.")
		void soundConfigNotNull() {
			assertNotNull(pluginMock.soundConfig);
		}
	}

}
