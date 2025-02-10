package com.winterhavenmc.savagegraveyards;

import com.winterhavenmc.savagegraveyards.sounds.SoundId;
import com.winterhavenmc.savagegraveyards.util.Config;
import org.bukkit.configuration.Configuration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConfigTests {

    private ServerMock server;
    private PluginMain plugin;


    @BeforeAll
    public void setUp() {

        // Start the mock server
        server = MockBukkit.mock();

        // start the mock plugin
        plugin = MockBukkit.load(PluginMain.class);
    }

    @AfterAll
    public void tearDown() {

        // cancel all tasks
        server.getScheduler().cancelTasks(plugin);

        // Stop the mock server
        MockBukkit.unmock();
    }


    @Nested
    @DisplayName("Test plugin config.")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class DefaultConfigFileTests {

        final Configuration config = plugin.getConfig();
        final Set<String> enumConfigKeyStrings = new HashSet<>();

        public DefaultConfigFileTests() {
            for (Config config : Config.values()) {
                this.enumConfigKeyStrings.add(config.asFileKey());
            }
        }

        @Test
        @DisplayName("config not null.")
        void configNotNull() {
            Assertions.assertNotNull(config);
        }

        @Test
        @DisplayName("test configured language.")
        void getLanguage() {
            Assertions.assertEquals("en-US", config.getString("language"),
                    "configured language does not match en-US");
        }

        @SuppressWarnings("unused")
        Set<String> configFileKeys() {
            return plugin.getConfig().getKeys(false);
        }

        @ParameterizedTest
        @DisplayName("default config file key is contained in Config enum.")
        @MethodSource("configFileKeys")
        void configFileKeyNotNull(String key) {
            Assertions.assertNotNull(key);
            Assertions.assertTrue(enumConfigKeyStrings.contains(key),
                    "default config file key is not contained in Config enum.");
        }

        @ParameterizedTest
        @DisplayName("default config file keys conform to yaml naming convention (lower kebab case).")
        @MethodSource("configFileKeys")
        void configFileKeyNamingConvention(String key) {
            Assertions.assertEquals(key, Config.Case.LOWER_KEBAB.convert(key),
                    "default config file key does not conform to yaml naming convention (lower kebab case).");
        }

        @ParameterizedTest
        @EnumSource(Config.class)
        @DisplayName("Enum members conform to Java constant naming convention (upper snake case).")
        void configEnumStringNamingConvention(Config config) {
            Assertions.assertEquals(config.name(), config.toUpperSnakeCase(),
                    "Enum member name does not conform to Java constant naming convention (upper snake case).");
        }

        @ParameterizedTest()
        @EnumSource(value=Config.class, mode=EXCLUDE, names={"DEBUG", "STORAGE_TYPE"})
        @DisplayName("Config enum matches config file key/value pairs.")
        void configFileKeysContainsEnumKey(final Config configMember) {
            Assertions.assertEquals(configMember.getDefaultObject().toString(), plugin.getConfig().getString(configMember.asFileKey()),
                    "Enum member " + configMember.name() + " does not match default config.yml file vale for key: " + configMember.asFileKey());
        }
    }


    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("Test Sounds config.")
    class SoundTests {

        // collection of enum sound name strings
        final Collection<String> enumSoundNames = new HashSet<>();

        // class constructor
        SoundTests() {
            // add all SoundId enum values to collection
            for (SoundId soundId : SoundId.values()) {
                enumSoundNames.add(soundId.name());
            }
        }

        @Test
        @DisplayName("Sounds config is not null.")
        void soundConfigNotNull() {
            Assertions.assertNotNull(plugin.soundConfig);
        }

        final Collection<String> getConfigFileKeys() {
            return plugin.soundConfig.getSoundConfigKeys();
        }

        @ParameterizedTest
        @EnumSource(SoundId.class)
        @DisplayName("enum member soundId is contained in getConfig() keys.")
        void fileKeysContainsEnumValue(SoundId soundId) {
            Assertions.assertTrue(plugin.soundConfig.isValidSoundConfigKey(soundId.name()),
                    "Enum value '" + soundId.name() + "' does not have matching key in sounds.yml.");
        }

        @ParameterizedTest
        @MethodSource("getConfigFileKeys")
        @DisplayName("config file key has matching key in enum sound names")
        void soundConfigEnumContainsAllFileSounds(String key) {
            Assertions.assertTrue(enumSoundNames.contains(key),
                    "File key does not have matching key in enum sound names.");
        }

        @ParameterizedTest
        @MethodSource("getConfigFileKeys")
        @DisplayName("sound file key has valid bukkit sound name")
        void soundConfigFileHasValidBukkitSound(String key) {
            String bukkitSoundName = plugin.soundConfig.getBukkitSoundName(key);
            Assertions.assertTrue(plugin.soundConfig.isValidBukkitSoundName(bukkitSoundName),
                    "File key '" + key + "' has invalid bukkit sound name: " + bukkitSoundName);
        }
    }

}
