package com.winterhavenmc.savagegraveyards.commands;

import com.winterhavenmc.savagegraveyards.messages.MessageId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;


abstract class AbstractCommand implements Subcommand {

	private String name;
	private Collection<String> aliases = new HashSet<>();
	private String usageString;
	private MessageId description;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public Collection<String> getAliases() {
		return aliases;
	}

	@Override
	public void setAliases(final Collection<String> aliases) {
		this.aliases = aliases;
	}

	@Override
	public void addAlias(final String alias) {
		this.aliases.add(alias);
	}

	@Override
	public String getUsage() {
		return usageString;
	}

	@Override
	public void displayUsage(final CommandSender sender) {
		sender.sendMessage(usageString);
	}

	@Override
	public void setUsage(final String usageString) {
		this.usageString = usageString;
	}

	@Override
	public MessageId getDescription() {
		return description;
	}

	@Override
	public void setDescription(final MessageId description) {
		this.description = description;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
									  final String alias, final String[] args) {

		return Collections.emptyList();
	}

}
