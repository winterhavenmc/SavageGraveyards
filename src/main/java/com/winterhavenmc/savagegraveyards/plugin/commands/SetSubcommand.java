/*
 * Copyright (c) 2022-2025 Tim Savage.
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

package com.winterhavenmc.savagegraveyards.plugin.commands;

import com.winterhavenmc.savagegraveyards.plugin.PluginMain;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.DisplayName;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.SearchKey;
import com.winterhavenmc.savagegraveyards.plugin.models.location.ImmutableLocation;
import com.winterhavenmc.savagegraveyards.plugin.util.SoundId;
import com.winterhavenmc.savagegraveyards.plugin.util.Macro;
import com.winterhavenmc.savagegraveyards.plugin.util.MessageId;
import com.winterhavenmc.savagegraveyards.plugin.util.Config;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.*;


/**
 * Set command implementation<br>
 * changes graveyard settings
 */
final class SetSubcommand extends AbstractSubcommand implements Subcommand
{
	private final PluginMain plugin;

	private final static int CONFIG_DEFAULT = -1;

	// list of possible attributes
	private final static List<String> ATTRIBUTES = List.of("enabled", "hidden", "location", "name", "safetytime",
					"discoveryrange", "discoverymessage", "respawnmessage");


	/**
	 * Class constructor
	 * @param plugin reference to plugin main class instance
	 */
	SetSubcommand(final PluginMain plugin)
	{
		this.plugin = Objects.requireNonNull(plugin);
		this.name = "set";
		this.usageString = "/graveyard set <graveyard> <attribute> <value>";
		this.description = MessageId.COMMAND_HELP_SET;
		this.permissionNode = "graveyard.set";
		this.minArgs = 2;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender,
	                                  final Command command,
									  final String alias,
									  final String[] args)
	{
		return switch (args.length)
		{
			case 2 -> plugin.dataStore.graveyards().getMatchingKeys(args[1]);
			case 3 -> getAttributes(sender, args[2]);
			default -> Collections.emptyList();
		};
	}

	private List<String> getAttributes(CommandSender sender, String partialMatch)
	{
		List<String> returnList = new ArrayList<>();

		for (String attribute : ATTRIBUTES)
		{
			if (sender.hasPermission("graveyard.set." + attribute) && attribute.startsWith(partialMatch))
			{
				returnList.add(attribute);
			}
		}
		return returnList;
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// check minimum arguments
		if (args.size() < minArgs)
		{
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			displayUsage(sender);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get graveyard key from arguments ArrayList
		SearchKey searchKey = SearchKey.of(args.removeFirst());

		switch (searchKey)
		{
			case SearchKey.Invalid invalidKey -> sendFailInvalidKey(sender, invalidKey);
			case SearchKey.Valid validKey ->
			{
				// fetch graveyard from datastore
				Graveyard graveyard = plugin.dataStore.graveyards().get(validKey);

				switch (graveyard)
				{
					case Graveyard.Invalid invalid -> sendFailSelect(sender, invalid);
					case Graveyard.Valid validGraveyard ->
					{
						// get attribute name and remove from arguments ArrayList
						String attribute = args.removeFirst();

						// get value by joining remaining arguments
						String value = String.join(" ", args).trim();

						switch (attribute.toLowerCase())
						{
							case "location":
								return setLocation(sender, validGraveyard);
							case "name":
								return setName(sender, validGraveyard, value);
							case "enabled":
								return setEnabled(sender, validGraveyard, value);
							case "hidden":
								return setHidden(sender, validGraveyard, value);
							case "discoveryrange":
								return setDiscoveryRange(sender, validGraveyard, value);
							case "discoverymessage":
								return setDiscoveryMessage(sender, validGraveyard, value);
							case "respawnmessage":
								return setRespawnMessage(sender, validGraveyard, value);
							case "group":
								return setGroup(sender, validGraveyard, value);
							case "safetytime":
								return setSafetyTime(sender, validGraveyard, value);
						}

						sendFailNoMatch(sender, validGraveyard);
					}
				}
			}
		}
		return true;
	}

	private void sendFailInvalidKey(CommandSender sender, SearchKey.Invalid invalidKey)
	{
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_SET_INVALID_KEY)
				.setMacro(Macro.SEARCH_KEY, invalidKey.string())
				.send();
	}


	private Graveyard sendFailSelect(CommandSender sender, Graveyard graveyard)
	{
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_NO_RECORD)
				.setMacro(Macro.GRAVEYARD, graveyard.displayName().colorString())
				.send();
		return graveyard;
	}


	private Graveyard sendFailNoMatch(CommandSender sender, Graveyard graveyard)
	{
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_INVALID_ATTRIBUTE).send();
		return graveyard;
	}


	/**
	 * Set new location for existing graveyard
	 *
	 * @param sender    the player that issued the command
	 * @param graveyard the existing graveyard to be updated
	 * @return always returns {@code true} to suppress display of bukkit command usage
	 * @throws NullPointerException if any parameter is null
	 */
	boolean setLocation(final CommandSender sender, final Graveyard.Valid graveyard)
	{
		// sender must be in game player
		if (!(sender instanceof Player player))
		{
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// check player permission
		if (!player.hasPermission("graveyard.set.location"))
		{
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_SET_LOCATION).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// create new graveyard object from existing graveyard with player location
		Graveyard newGraveyard = Graveyard.of(graveyard.displayName(),
				graveyard.attributes(), ImmutableLocation.of(player));

		if (newGraveyard instanceof Graveyard.Valid validGraveyard)
		{
			// update graveyard record in datastore
			plugin.dataStore.graveyards().update(validGraveyard);

			// send success message
			plugin.messageBuilder.compose(player, MessageId.COMMAND_SUCCESS_SET_LOCATION)
					.setMacro(Macro.GRAVEYARD, validGraveyard.displayName().colorString())
					.setMacro(Macro.LOCATION, validGraveyard.getLocation())
					.send();

			// play success sound
			plugin.soundConfig.playSound(player, SoundId.COMMAND_SUCCESS_SET);
		}
		return true;
	}


	/**
	 * Set new display name for existing graveyard
	 *
	 * @param sender       the player that issued the command
	 * @param graveyard    the existing graveyard to be updated
	 * @param passedString the new display name for the graveyard
	 * @return always returns {@code true} to suppress display of bukkit command usage
	 * @throws NullPointerException if any parameter is null
	 */
	private boolean setName(final CommandSender sender,
							final Graveyard.Valid graveyard,
							final String passedString)
	{
		// check for null parameters
		Objects.requireNonNull(sender);
		Objects.requireNonNull(graveyard);
		Objects.requireNonNull(passedString);

		// check sender permission
		if (!sender.hasPermission("graveyard.set.name"))
		{
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_SET_NAME).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// Note: displayName.of(string) converts underscores to spaces
		final DisplayName newDisplayName = DisplayName.of(passedString);

		// if new name is blank, send invalid name message
		if (newDisplayName.noColorString().isBlank())
		{
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_SET_INVALID_NAME).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		if (newDisplayName instanceof DisplayName.Valid validDisplayName)
		{
			// get original name
			final DisplayName.Valid oldDisplayName = graveyard.displayName();

			// create new graveyard object from existing graveyard with new name
			Graveyard newGraveyard = Graveyard.of(validDisplayName,
					graveyard.attributes(), graveyard.location());

			if (newGraveyard instanceof Graveyard.Valid validGraveyard)
			{
				plugin.dataStore.graveyards().update(oldDisplayName, validGraveyard);
				plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_SET);
				plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_SET_NAME)
						.setMacro(Macro.GRAVEYARD, validGraveyard.displayName().colorString())
						.setMacro(Macro.VALUE, oldDisplayName)
						.send();
			}
		}
		return true;
	}


	/**
	 * Set new enabled setting for existing graveyard
	 *
	 * @param sender       the player that issued the command
	 * @param graveyard    the existing graveyard to be updated
	 * @param passedString the new enabled setting for the graveyard
	 * @return always returns {@code true} to suppress display of bukkit command usage
	 * @throws NullPointerException if any parameter is null
	 */
	private boolean setEnabled(final CommandSender sender,
							   final Graveyard.Valid graveyard,
							   final String passedString)
	{
		// check for null parameters
		Objects.requireNonNull(sender);
		Objects.requireNonNull(graveyard);
		Objects.requireNonNull(passedString);

		// check sender permission
		if (!sender.hasPermission("graveyard.set.enabled"))
		{
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_SET_ENABLED).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get value from passed string trimmed
		String value = passedString;
		boolean enabled;

		// if value is empty, set to true
		if (value.isEmpty())
		{
			value = "true";
		}

		// if value is "default", set to configured default setting
		if (value.equalsIgnoreCase("default"))
		{
			enabled = Config.DEFAULT_ENABLED.getBoolean(plugin.getConfig());
		}
		else if (value.equalsIgnoreCase("true")
				|| value.equalsIgnoreCase("yes")
				|| value.equalsIgnoreCase("y"))
		{
			enabled = true;
		}
		else if (value.equalsIgnoreCase("false")
				|| value.equalsIgnoreCase("no")
				|| value.equalsIgnoreCase("n"))
		{
			enabled = false;
		}
		else
		{
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_SET_INVALID_BOOLEAN).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// create new graveyard object from existing graveyard with new enabled setting
		Graveyard newGraveyard = Graveyard.of(graveyard.displayName(),
				graveyard.attributes().withEnabled(enabled), graveyard.location());

		// set value to string representation of enabled boolean
		value = String.valueOf(enabled);

		if (newGraveyard instanceof Graveyard.Valid valid)
		{
			// update record in data store
			plugin.dataStore.graveyards().update(valid);

			// send success message
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_SET_ENABLED)
					.setMacro(Macro.GRAVEYARD, valid.displayName().colorString())
					.setMacro(Macro.VALUE, value)
					.send();

			// play success sound
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_SET);
		}
		return true;
	}


	/**
	 * Set new hidden setting for existing graveyard
	 *
	 * @param sender       the player that issued the command
	 * @param graveyard    the existing graveyard to be updated
	 * @param passedString the new hidden setting for the graveyard
	 * @return always returns {@code true} to suppress display of bukkit command usage
	 * @throws NullPointerException if any parameter is null
	 */
	private boolean setHidden(final CommandSender sender,
							  final Graveyard.Valid graveyard,
							  final String passedString)
	{
		enum HiddenStatus
		{
			TRUE(List.of("TRUE", "YES", "Y")),
			FALSE(List.of("FALSE", "NO", "N")),
			DEFAULT(List.of("DEFAULT"));

			private final List<String> status;

			HiddenStatus(final List<String> status) {
				this.status = status;
			}

			boolean contains(String value) {
				return this.status.contains(value.toUpperCase());
			}
		}

		// check for null parameters
		Objects.requireNonNull(sender);
		Objects.requireNonNull(graveyard);
		Objects.requireNonNull(passedString);

		// check sender permission
		if (!sender.hasPermission("graveyard.set.hidden"))
		{
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_SET_HIDDEN).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get value from passed string
		String value = passedString;
		boolean hidden;

		// if value is empty, set to true
		if (value.isEmpty())
		{
			value = "true";
		}

		if (HiddenStatus.DEFAULT.contains(value))
		{
			hidden = Config.DEFAULT_HIDDEN.getBoolean(plugin.getConfig());
		}

		else if (HiddenStatus.TRUE.contains(value))
		{
			hidden = true;
		}
		else if (HiddenStatus.FALSE.contains(value))
		{
			hidden = false;
		}
		else
		{
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_SET_INVALID_BOOLEAN).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// create new graveyard object from existing graveyard with new hidden setting
		Graveyard newGraveyard = Graveyard.of(graveyard.displayName(),
				graveyard.attributes().withHidden(hidden), graveyard.location());

		if (newGraveyard instanceof Graveyard.Valid validGraveyard)
		{
			plugin.dataStore.graveyards().update(validGraveyard);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_SET);
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_SET_HIDDEN)
					.setMacro(Macro.GRAVEYARD, newGraveyard.displayName().colorString())
					.setMacro(Macro.VALUE, value)
					.send();
		}
		return true;
	}


	/**
	 * Set new discovery range for existing graveyard
	 *
	 * @param sender       the player that issued the command
	 * @param graveyard    the existing graveyard to be updated
	 * @param passedString the new hidden setting for the graveyard
	 * @return always returns {@code true} to suppress display of bukkit command usage
	 * @throws NullPointerException if any parameter is null
	 */
	private boolean setDiscoveryRange(final CommandSender sender,
									  final Graveyard.Valid graveyard,
									  final String passedString)
	{
		// check for null parameters
		Objects.requireNonNull(sender);
		Objects.requireNonNull(graveyard);
		Objects.requireNonNull(passedString);

		// check sender permission
		if (!sender.hasPermission("graveyard.set.discoveryrange"))
		{
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_SET_DISCOVERYRANGE).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		int discoveryRange;

		// if no distance given, or string "default", use configured default value
		if (passedString.isEmpty() || passedString.equalsIgnoreCase("default"))
		{
			//noinspection ConstantConditions
			discoveryRange = CONFIG_DEFAULT;
		}

		// if value is string "player", attempt to use player distance
		else if ((passedString.equalsIgnoreCase("player")
				|| passedString.equalsIgnoreCase("current"))
				&& sender instanceof Player player
				&& player.getWorld().getUID().equals(graveyard.location().world().uid()))
		{
			discoveryRange = (int) player.getLocation().distance(graveyard.getLocation());
		}

		else
		{
			try
			{
				discoveryRange = Integer.parseInt(passedString);
			}
			catch (NumberFormatException exception)
			{
				plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_SET_INVALID_INTEGER).send();
				plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
				return true;
			}
		}

		// create new graveyard object from existing graveyard with new discovery range
		Graveyard newGraveyard = Graveyard.of(graveyard.displayName(),
				graveyard.attributes().withDiscoveryRange(discoveryRange), graveyard.location());

		if (newGraveyard instanceof Graveyard.Valid validGraveyard)
		{
			// update graveyard in datastore
			plugin.dataStore.graveyards().update(validGraveyard);

			// send success message
			if (discoveryRange < 0)
			{
				plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_SET_DISCOVERYRANGE_DEFAULT)
						.setMacro(Macro.GRAVEYARD, validGraveyard.displayName().colorString())
						.setMacro(Macro.VALUE, Config.DISCOVERY_RANGE.getInt(plugin.getConfig()))
						.send();
			}
			else
			{
				plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_SET_DISCOVERYRANGE)
						.setMacro(Macro.GRAVEYARD, validGraveyard.displayName().colorString())
						.setMacro(Macro.VALUE, String.valueOf(discoveryRange))
						.send();
			}

			// play success sound
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_SET);
		}
		return true;
	}


	/**
	 * Set new discovery message for existing graveyard
	 *
	 * @param sender       the player that issued the command
	 * @param graveyard    the existing graveyard to be updated
	 * @param passedString the new discovery message for the graveyard
	 * @return always returns {@code true} to suppress display of bukkit command usage
	 * @throws NullPointerException if any parameter is null
	 */
	private boolean setDiscoveryMessage(final CommandSender sender,
										final Graveyard.Valid graveyard,
										final String passedString)
	{
		// check for null parameters
		Objects.requireNonNull(sender);
		Objects.requireNonNull(graveyard);
		Objects.requireNonNull(passedString);

		// check sender permission
		if (!sender.hasPermission("graveyard.set.discoverymessage"))
		{
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_SET_DISCOVERYMESSAGE).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get discovery message from passed string
		String discoveryMessage = passedString;

		// if message is 'default', set message to empty string
		if (discoveryMessage.equalsIgnoreCase("default"))
		{
			discoveryMessage = "";
		}

		Graveyard newGraveyard = Graveyard.of(graveyard.displayName(),
				graveyard.attributes().withDiscoveryMessage(discoveryMessage), graveyard.location());

		if (newGraveyard instanceof Graveyard.Valid validGraveyard)
		{
			// update graveyard record in datastore
			plugin.dataStore.graveyards().update(validGraveyard);

			// send success message
			if (discoveryMessage.isEmpty())
			{
				plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_SET_DISCOVERYMESSAGE_DEFAULT)
						.setMacro(Macro.GRAVEYARD, validGraveyard.displayName().colorString())
						.send();
			}
			else
			{
				plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_SET_DISCOVERYMESSAGE)
						.setMacro(Macro.GRAVEYARD, validGraveyard.displayName().colorString())
						.send();
			}

			// play success sound
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_SET);
		}
		return true;
	}


	/**
	 * Set new respawn message for existing graveyard
	 *
	 * @param sender       the player that issued the command
	 * @param graveyard    the existing graveyard to be updated
	 * @param passedString the new respawn message for the graveyard
	 * @return always returns {@code true} to suppress display of bukkit command usage
	 * @throws NullPointerException if any parameter is null
	 */
	private boolean setRespawnMessage(final CommandSender sender,
									  final Graveyard.Valid graveyard,
									  final String passedString) {

		// check for null parameters
		Objects.requireNonNull(sender);
		Objects.requireNonNull(graveyard);
		Objects.requireNonNull(passedString);

		// check sender permission
		if (!sender.hasPermission("graveyard.set.respawnmessage"))
		{
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_SET_RESPAWNMESSAGE).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get respawn message from passed string
		String respawnMessage = passedString;

		// if message is 'default', set message to empty string
		if (respawnMessage.equalsIgnoreCase("default"))
		{
			respawnMessage = "";
		}

		// create new graveyard object with new respawn message
		Graveyard newGraveyard = Graveyard.of(graveyard.displayName(),
				graveyard.attributes().withRespawnMessage(respawnMessage), graveyard.location());

		if (newGraveyard instanceof Graveyard.Valid validGraveyard)
		{
			// update record in data store
			plugin.dataStore.graveyards().update(validGraveyard);

			// send success message
			if (respawnMessage.isEmpty())
			{
				plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_SET_RESPAWNMESSAGE_DEFAULT)
						.setMacro(Macro.GRAVEYARD, validGraveyard.displayName().colorString())
						.send();
			}
			else
			{
				plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_SET_RESPAWNMESSAGE)
						.setMacro(Macro.GRAVEYARD, validGraveyard.displayName().colorString())
						.send();
			}

			// play success sound
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_SET);
		}
		return true;
	}


	/**
	 * Set new group for existing graveyard
	 *
	 * @param sender       the player that issued the command
	 * @param graveyard    the existing graveyard to be updated
	 * @param passedString the new group for the graveyard
	 * @return always returns {@code true} to suppress display of bukkit command usage
	 * @throws NullPointerException if any parameter is null
	 */
	private boolean setGroup(final CommandSender sender,
							 final Graveyard.Valid graveyard,
							 final String passedString)
	{
		// check for null parameters
		Objects.requireNonNull(sender);
		Objects.requireNonNull(graveyard);
		Objects.requireNonNull(passedString);

		// check sender permission
		if (!sender.hasPermission("graveyard.set.group"))
		{
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_SET_GROUP).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// create new graveyard object from existing graveyard with new group
		Graveyard newGraveyard = Graveyard.of(graveyard.displayName(),
				graveyard.attributes().withGroup(passedString), graveyard.location());

		if (newGraveyard instanceof Graveyard.Valid validGraveyard)
		{
			// update graveyard record in datastore
			plugin.dataStore.graveyards().update(validGraveyard);

			// send success message
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_SET_GROUP)
					.setMacro(Macro.GRAVEYARD, validGraveyard.displayName().colorString())
					.setMacro(Macro.VALUE, validGraveyard.attributes().group().value())
					.send();

			// play success sound
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_SET);
		}
		return true;
	}


	/**
	 * Set new safety time for existing graveyard
	 *
	 * @param sender       the player that issued the command
	 * @param graveyard    the existing graveyard to be updated
	 * @param passedString the new safety time for the graveyard
	 * @return always returns {@code true} to suppress display of bukkit command usage
	 * @throws NullPointerException if any parameter is null
	 */
	private boolean setSafetyTime(final CommandSender sender,
								  final Graveyard.Valid graveyard,
								  final String passedString)
	{
		// check for null parameters
		Objects.requireNonNull(sender);
		Objects.requireNonNull(graveyard);
		Objects.requireNonNull(passedString);

		// check sender permission
		if (!sender.hasPermission("graveyard.set.safetytime"))
		{
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_SET_SAFETYTIME).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		long value;
		try
		{
			value = Long.parseLong(passedString);
		}
		catch (NumberFormatException exception)
		{
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_SET_INVALID_INTEGER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		if (value < 0)
		{
			value = CONFIG_DEFAULT;
		}

		// declare safety time to be set
		Duration safetyTime  = Duration.ofSeconds(value);

		// create new graveyard object with from existing graveyard with new safety time
		Graveyard newGraveyard = Graveyard.of(graveyard.displayName(),
				graveyard.attributes().withSafetyTime(safetyTime), graveyard.location());

		if (newGraveyard instanceof Graveyard.Valid valid)
		{
			// update graveyard record in datastore
			plugin.dataStore.graveyards().update(valid);

			// send success message
			if (safetyTime.equals(Duration.ofSeconds(CONFIG_DEFAULT)))
			{
				plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_SET_SAFETYTIME_DEFAULT)
						.setMacro(Macro.GRAVEYARD, valid.displayName().colorString())
						.setMacro(Macro.DURATION, Duration.ofSeconds(Config.SAFETY_TIME.getInt(plugin.getConfig())))
						.send();
			}
			else
			{
				plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_SET_SAFETYTIME)
						.setMacro(Macro.GRAVEYARD, valid.displayName().colorString())
						.setMacro(Macro.DURATION, valid.attributes().safetyTime())
						.send();
			}

			// play success sound
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_SET);
		}
		return true;
	}

}
