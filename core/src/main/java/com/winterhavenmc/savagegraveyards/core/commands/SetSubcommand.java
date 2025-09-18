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

package com.winterhavenmc.savagegraveyards.core.commands;

import com.winterhavenmc.savagegraveyards.core.PluginController;
import com.winterhavenmc.savagegraveyards.models.displayname.DisplayName;
import com.winterhavenmc.savagegraveyards.models.displayname.ValidDisplayName;
import com.winterhavenmc.savagegraveyards.models.graveyard.*;
import com.winterhavenmc.savagegraveyards.models.location.ImmutableLocation;
import com.winterhavenmc.savagegraveyards.core.util.SoundId;
import com.winterhavenmc.savagegraveyards.core.util.Macro;
import com.winterhavenmc.savagegraveyards.core.util.MessageId;
import com.winterhavenmc.savagegraveyards.core.util.Config;

import com.winterhavenmc.savagegraveyards.models.searchkey.InvalidSearchKey;
import com.winterhavenmc.savagegraveyards.models.searchkey.SearchKey;
import com.winterhavenmc.savagegraveyards.models.searchkey.ValidSearchKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Set command implementation<br>
 * changes graveyard settings
 */
@SuppressWarnings("SameReturnValue")
final class SetSubcommand extends AbstractSubcommand implements Subcommand
{
	private final PluginController.CommandContextContainer ctx;

	private final static int CONFIG_DEFAULT = -1;

	// list of possible attributes
	private final static List<String> ATTRIBUTES = List.of("enabled", "hidden", "location", "name", "safetytime",
					"discoveryrange", "discoverymessage", "respawnmessage");


	/**
	 * Class constructor
	 */
	SetSubcommand(final PluginController.CommandContextContainer ctx)
	{
		this.ctx = ctx;
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
			case 2 -> ctx.graveyards().getMatchingKeys(args[1]);
			case 3 -> matchPermittedAttributes(sender, args[2]);
			default -> List.of();
		};
	}


	private List<String> matchPermittedAttributes(CommandSender sender, String partialMatch)
	{
		return ATTRIBUTES.stream()
				.filter(attribute -> sender.hasPermission("graveyard.set." + attribute))
				.filter(attribute -> attribute.startsWith(partialMatch))
				.collect(Collectors.toList());
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// check minimum arguments
		if (args.size() < minArgs)
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			displayUsage(sender);
			return true;
		}

		// get graveyard key from arguments ArrayList
		SearchKey searchKey = SearchKey.of(args.removeFirst());

		switch (searchKey)
		{
			case InvalidSearchKey invalidKey -> sendFailInvalidKey(sender, invalidKey);
			case ValidSearchKey validKey ->
			{
				switch (ctx.graveyards().get(validKey))
				{
					case InvalidGraveyard invalid -> sendFailSelect(sender, invalid);
					case ValidGraveyard validGraveyard ->
					{
						// get attribute name and remove from arguments ArrayList
						String attribute = args.removeFirst();

						// get value by joining remaining arguments
						String value = String.join(" ", args).trim();

						return switch (attribute.toLowerCase())
						{
							case "location" -> setLocation(sender, validGraveyard);
							case "name" -> setName(sender, validGraveyard, value);
							case "enabled" -> setEnabled(sender, validGraveyard, value);
							case "hidden" -> setHidden(sender, validGraveyard, value);
							case "discoveryrange" -> setDiscoveryRange(sender, validGraveyard, value);
							case "discoverymessage" -> setDiscoveryMessage(sender, validGraveyard, value);
							case "respawnmessage" -> setRespawnMessage(sender, validGraveyard, value);
							case "group" -> setGroup(sender, validGraveyard, value);
							case "safetytime" -> setSafetyTime(sender, validGraveyard, value);
							default -> sendFailNoMatch(sender, validGraveyard);
						};
					}
				}
			}
		}
		return true;
	}

	private void sendFailInvalidKey(CommandSender sender, InvalidSearchKey invalidKey)
	{
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_SET_INVALID_KEY)
				.setMacro(Macro.SEARCH_KEY, invalidKey.string())
				.send();
	}


	private void sendFailSelect(CommandSender sender, Graveyard graveyard)
	{
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_NO_RECORD)
				.setMacro(Macro.GRAVEYARD, graveyard)
				.send();
	}


	private boolean sendFailNoMatch(CommandSender sender, Graveyard graveyard)
	{
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_INVALID_ATTRIBUTE)
				.setMacro(Macro.GRAVEYARD, graveyard)
				.send();
		return true;
	}


	/**
	 * Set new location for existing graveyard
	 *
	 * @param sender    the player that issued the command
	 * @param graveyard the existing graveyard to be updated
	 * @return always returns {@code true} to suppress display of bukkit command usage
	 * @throws NullPointerException if any parameter is null
	 */
	boolean setLocation(final CommandSender sender, final ValidGraveyard graveyard)
	{
		// sender must be in game player
		if (!(sender instanceof Player player))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_CONSOLE)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.send();
			return true;
		}

		// check player permission
		if (!player.hasPermission("graveyard.set.location"))
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_SET_LOCATION)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.send();
			return true;
		}

		// create new graveyard object from existing graveyard with player location
		Graveyard newGraveyard = Graveyard.of(graveyard.displayName(),
				graveyard.attributes(), ImmutableLocation.of(player));

		if (newGraveyard instanceof ValidGraveyard validGraveyard)
		{
			// update graveyard record in datastore
			ctx.graveyards().update(validGraveyard);

			// send success message
			ctx.messageBuilder().compose(player, MessageId.COMMAND_SUCCESS_SET_LOCATION)
					.setMacro(Macro.GRAVEYARD, validGraveyard)
					.send();

			// play success sound
			ctx.soundConfig().playSound(player, SoundId.COMMAND_SUCCESS_SET);
		}
		return true;
	}


	/**
	 * Set new display name for existing graveyard
	 *
	 * @param sender       the player that issued the command
	 * @param originalGraveyard    the existing graveyard to be updated
	 * @param passedString the new display name for the graveyard
	 * @return always returns {@code true} to suppress display of bukkit command usage
	 * @throws NullPointerException if any parameter is null
	 */
	private boolean setName(final CommandSender sender,
							final ValidGraveyard originalGraveyard,
							final String passedString)
	{
		// check for null parameters
		Objects.requireNonNull(sender);
		Objects.requireNonNull(originalGraveyard);
		Objects.requireNonNull(passedString);

		// check sender permission
		if (!sender.hasPermission("graveyard.set.name"))
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_SET_NAME)
					.setMacro(Macro.GRAVEYARD, originalGraveyard)
					.setMacro(Macro.VALUE, passedString)
					.send();
			return true;
		}

		// Note: displayName.of(string) converts underscores to spaces
		final DisplayName newDisplayName = DisplayName.of(passedString);

		// if new name is blank, send invalid name message
		if (newDisplayName.noColorString().isBlank())
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_SET_INVALID_NAME)
					.setMacro(Macro.GRAVEYARD, originalGraveyard)
					.setMacro(Macro.VALUE, passedString)
					.send();
			return true;
		}

		if (newDisplayName instanceof ValidDisplayName validNewDisplayName)
		{
			// create new graveyard object from existing graveyard with new name
			Graveyard newGraveyard = Graveyard.of(validNewDisplayName,
					originalGraveyard.attributes(), originalGraveyard.location());

			if (newGraveyard instanceof ValidGraveyard validNewGraveyard)
			{
				ctx.graveyards().update(originalGraveyard.searchKey(), validNewGraveyard);
				ctx.soundConfig().playSound(sender, SoundId.COMMAND_SUCCESS_SET);
				ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_SET_NAME)
						.setMacro(Macro.GRAVEYARD, validNewGraveyard)
						.setMacro(Macro.VALUE, originalGraveyard.displayName())
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
							   final ValidGraveyard graveyard,
							   final String passedString)
	{
		// check for null parameters
		Objects.requireNonNull(sender);
		Objects.requireNonNull(graveyard);
		Objects.requireNonNull(passedString);

		// check sender permission
		if (!sender.hasPermission("graveyard.set.enabled"))
		{
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_SET_ENABLED).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
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
		if (BooleanConfigSetting.DEFAULT.contains(value))
		{
			enabled = Config.DEFAULT_ENABLED.getBoolean(ctx.plugin().getConfig());
		}
		else if (BooleanConfigSetting.TRUE.contains(value))
		{
			enabled = true;
		}
		else if (BooleanConfigSetting.FALSE.contains(value))
		{
			enabled = false;
		}
		else
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_SET_INVALID_BOOLEAN).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// create new graveyard object from existing graveyard with new enabled setting
		Graveyard newGraveyard = Graveyard.of(graveyard.displayName(),
				graveyard.attributes().withEnabled(enabled), graveyard.location());

		// set value to string representation of enabled boolean
		value = String.valueOf(enabled);

		if (newGraveyard instanceof ValidGraveyard valid)
		{
			// update record in data store
			ctx.graveyards().update(valid);

			// send success message
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_SET_ENABLED)
					.setMacro(Macro.GRAVEYARD, valid)
					.setMacro(Macro.VALUE, value)
					.send();

			// play success sound
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_SUCCESS_SET);
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
							  final ValidGraveyard graveyard,
							  final String passedString)
	{

		// check for null parameters
		Objects.requireNonNull(sender);
		Objects.requireNonNull(graveyard);
		Objects.requireNonNull(passedString);

		// check sender permission
		if (!sender.hasPermission("graveyard.set.hidden"))
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_SET_HIDDEN)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.setMacro(Macro.VALUE, passedString).send();
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

		if (BooleanConfigSetting.DEFAULT.contains(value))
		{
			hidden = Config.DEFAULT_HIDDEN.getBoolean(ctx.plugin().getConfig());
		}

		else if (BooleanConfigSetting.TRUE.contains(value))
		{
			hidden = true;
		}
		else if (BooleanConfigSetting.FALSE.contains(value))
		{
			hidden = false;
		}
		else
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_SET_INVALID_BOOLEAN)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.setMacro(Macro.VALUE, passedString)
					.send();
			return true;
		}

		// create new graveyard object from existing graveyard with new hidden setting
		Graveyard newGraveyard = Graveyard.of(graveyard.displayName(),
				graveyard.attributes().withHidden(hidden), graveyard.location());

		if (newGraveyard instanceof ValidGraveyard validGraveyard)
		{
			ctx.graveyards().update(validGraveyard);
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_SUCCESS_SET);
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_SET_HIDDEN)
					.setMacro(Macro.GRAVEYARD, newGraveyard)
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
									  final ValidGraveyard graveyard,
									  final String passedString)
	{
		// check for null parameters
		Objects.requireNonNull(sender);
		Objects.requireNonNull(graveyard);
		Objects.requireNonNull(passedString);

		// check sender permission
		if (!sender.hasPermission("graveyard.set.discoveryrange"))
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_SET_DISCOVERYRANGE)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.setMacro(Macro.VALUE, passedString)
					.send();
			return true;
		}

		int discoveryRange;

		// if no distance given, or string "default", use configured default value
		if (passedString.isEmpty() || passedString.equalsIgnoreCase("default"))
		{
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
				ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
				ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_SET_INVALID_INTEGER)
						.setMacro(Macro.GRAVEYARD, graveyard)
						.setMacro(Macro.VALUE, passedString)
						.send();
				return true;
			}
		}

		// create new graveyard object from existing graveyard with new discovery range
		Graveyard newGraveyard = Graveyard.of(graveyard.displayName(),
				graveyard.attributes().withDiscoveryRange(discoveryRange), graveyard.location());

		if (newGraveyard instanceof ValidGraveyard validGraveyard)
		{
			// update graveyard in datastore
			ctx.graveyards().update(validGraveyard);

			// send success message
			if (discoveryRange < 0)
			{
				ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_SET_DISCOVERYRANGE_DEFAULT)
						.setMacro(Macro.GRAVEYARD, validGraveyard)
						.setMacro(Macro.VALUE, Config.DISCOVERY_RANGE.getInt(ctx.plugin().getConfig()))
						.send();
			}
			else
			{
				ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_SET_DISCOVERYRANGE)
						.setMacro(Macro.GRAVEYARD, validGraveyard)
						.setMacro(Macro.VALUE, String.valueOf(discoveryRange))
						.send();
			}

			// play success sound
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_SUCCESS_SET);
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
										final ValidGraveyard graveyard,
										final String passedString)
	{
		// check for null parameters
		Objects.requireNonNull(sender);
		Objects.requireNonNull(graveyard);
		Objects.requireNonNull(passedString);

		// check sender permission
		if (!sender.hasPermission("graveyard.set.discoverymessage"))
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_SET_DISCOVERYMESSAGE)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.setMacro(Macro.VALUE, passedString)
					.send();
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

		if (newGraveyard instanceof ValidGraveyard validGraveyard)
		{
			// update graveyard record in datastore
			ctx.graveyards().update(validGraveyard);

			// send success message
			if (discoveryMessage.isEmpty())
			{
				ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_SET_DISCOVERYMESSAGE_DEFAULT)
						.setMacro(Macro.GRAVEYARD, validGraveyard)
						.setMacro(Macro.VALUE, passedString)
						.send();
			}
			else
			{
				ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_SET_DISCOVERYMESSAGE)
						.setMacro(Macro.GRAVEYARD, validGraveyard)
						.setMacro(Macro.VALUE, passedString)
						.send();
			}

			// play success sound
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_SUCCESS_SET);
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
									  final ValidGraveyard graveyard,
									  final String passedString)
	{
		// check for null parameters
		Objects.requireNonNull(sender);
		Objects.requireNonNull(graveyard);
		Objects.requireNonNull(passedString);

		// check sender permission
		if (!sender.hasPermission("graveyard.set.respawnmessage"))
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_SET_RESPAWNMESSAGE)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.setMacro(Macro.VALUE, passedString)
					.send();
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

		if (newGraveyard instanceof ValidGraveyard validGraveyard)
		{
			// update record in data store
			ctx.graveyards().update(validGraveyard);

			// send success message
			if (respawnMessage.isEmpty())
			{
				ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_SET_RESPAWNMESSAGE_DEFAULT)
						.setMacro(Macro.GRAVEYARD, validGraveyard)
						.setMacro(Macro.VALUE, passedString)
						.send();
			}
			else
			{
				ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_SET_RESPAWNMESSAGE)
						.setMacro(Macro.GRAVEYARD, validGraveyard)
						.setMacro(Macro.VALUE, passedString)
						.send();
			}

			// play success sound
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_SUCCESS_SET);
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
							 final ValidGraveyard graveyard,
							 final String passedString)
	{
		// check for null parameters
		Objects.requireNonNull(sender);
		Objects.requireNonNull(graveyard);
		Objects.requireNonNull(passedString);

		// check sender permission
		if (!sender.hasPermission("graveyard.set.group"))
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_SET_GROUP)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.setMacro(Macro.VALUE, passedString)
					.send();
			return true;
		}

		// create new graveyard object from existing graveyard with new group
		Graveyard newGraveyard = Graveyard.of(graveyard.displayName(),
				graveyard.attributes().withGroup(passedString), graveyard.location());

		if (newGraveyard instanceof ValidGraveyard validGraveyard)
		{
			// update graveyard record in datastore
			ctx.graveyards().update(validGraveyard);

			// send success message
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_SET_GROUP)
					.setMacro(Macro.GRAVEYARD, validGraveyard)
					.setMacro(Macro.VALUE, validGraveyard.attributes().group().value())
					.send();

			// play success sound
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_SUCCESS_SET);
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
								  final ValidGraveyard graveyard,
								  final String passedString)
	{
		// check for null parameters
		Objects.requireNonNull(sender);
		Objects.requireNonNull(graveyard);
		Objects.requireNonNull(passedString);

		// check sender permission
		if (!sender.hasPermission("graveyard.set.safetytime"))
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_SET_SAFETYTIME)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.setMacro(Macro.VALUE, passedString)
					.send();
			return true;
		}

		long value;
		try
		{
			value = Long.parseLong(passedString);
		}
		catch (NumberFormatException exception)
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_SET_INVALID_INTEGER)
					.setMacro(Macro.GRAVEYARD, graveyard)
					.setMacro(Macro.VALUE, passedString)
					.send();
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

		if (newGraveyard instanceof ValidGraveyard valid)
		{
			// update graveyard record in datastore
			ctx.graveyards().update(valid);

			// send success message
			if (safetyTime.equals(Duration.ofSeconds(CONFIG_DEFAULT)))
			{
				ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_SET_SAFETYTIME_DEFAULT)
						.setMacro(Macro.GRAVEYARD, valid)
						.setMacro(Macro.DURATION, Duration.ofSeconds(Config.SAFETY_TIME.getInt(ctx.plugin().getConfig())))
						.send();
			}
			else
			{
				ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_SET_SAFETYTIME)
						.setMacro(Macro.GRAVEYARD, valid)
						.setMacro(Macro.DURATION, valid.attributes().safetyTime())
						.send();
			}

			// play success sound
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_SUCCESS_SET);
		}
		return true;
	}

	private enum BooleanConfigSetting
	{
		TRUE(List.of("TRUE", "YES", "Y")),
		FALSE(List.of("FALSE", "NO", "N")),
		DEFAULT(List.of("DEFAULT"));

		private final List<String> status;

		BooleanConfigSetting(final List<String> status) {
			this.status = status;
		}

		boolean contains(String value) {
			return this.status.contains(value.toUpperCase());
		}
	}
}
