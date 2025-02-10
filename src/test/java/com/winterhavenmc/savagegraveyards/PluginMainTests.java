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

import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PluginMainTests {

	private ServerMock server;
	private PluginMain plugin;


	@BeforeEach
	public void setUp() {

		// Start the mock server
		server = MockBukkit.mock();

		// start the mock plugin
		plugin = MockBukkit.load(PluginMain.class);
	}

	@AfterEach
	public void tearDown() {

		// cancel all tasks
		server.getScheduler().cancelTasks(plugin);

		// Stop the mock server
		MockBukkit.unmock();
	}


	@Nested
	@DisplayName("Test mocking setup.")
	class MockingTests {

		@Test
		@DisplayName("test for null server instance")
		void serverNotNull() {
			Assertions.assertNotNull(server, "server is null.");
		}

		@Test
		@DisplayName("test for null plugin instance")
		void pluginNotNull() {
			Assertions.assertNotNull(plugin, "plugin is null.");
		}

		@Test
		@DisplayName("test if plugin is enabled")
		void pluginEnabled() {
			Assertions.assertTrue(plugin.isEnabled(),"plugin is not enabled.");
		}
	}


	@Nested
	@DisplayName("Test plugin main objects.")
	class PluginTests {
		@Test
		@DisplayName("message builder not null.")
		void messageBuilderNotNull() {
			Assertions.assertNotNull(plugin.messageBuilder);
		}

		@Test
		@DisplayName("world manager not null.")
		void worldManagerNotNull() {
			Assertions.assertNotNull(plugin.worldManager);
		}

		@Test
		@DisplayName("sound config not null.")
		void soundConfigNotNull() {
			Assertions.assertNotNull(plugin.soundConfig);
		}
	}

}
