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

package com.winterhavenmc.savagegraveyards.commands.bukkit;


public enum SubcommandType
{
	CLOSEST()
			{
				@Override
				public Subcommand create(final CommandCtx ctx)
				{
					return new ClosestSubcommand(ctx);
				}
			},

	CREATE()
			{
				@Override
				public Subcommand create(final CommandCtx ctx)
				{
					return new CreateSubcommand(ctx);
				}
			},

	DELETE()
			{
				@Override
				public Subcommand create(final CommandCtx ctx)
				{
					return new DeleteSubcommand(ctx);
				}
			},

	FORGET()
			{
				@Override
				public Subcommand create(final CommandCtx ctx)
				{
					return new ForgetSubcommand(ctx);
				}
			},

	LIST()
			{
				@Override
				public Subcommand create(final CommandCtx ctx)
				{
					return new ListSubcommand(ctx);
				}
			},

	RELOAD()
			{
				@Override
				public Subcommand create(final CommandCtx ctx)
				{
					return new ReloadSubcommand(ctx);
				}
			},

	SET()
			{
				@Override
				public Subcommand create(final CommandCtx ctx)
				{
					return new SetSubcommand(ctx);
				}
			},

	SHOW()
			{
				@Override
				public Subcommand create(final CommandCtx ctx)
				{
					return new ShowSubcommand(ctx);
				}
			},

	STATUS()
			{
				@Override
				public Subcommand create(final CommandCtx ctx)
				{
					return new StatusSubcommand(ctx);
				}
			},

	TELEPORT()
			{
				@Override
				public Subcommand create(final CommandCtx ctx)
				{
					return new TeleportSubcommand(ctx);
				}
			};

	public abstract Subcommand create(final CommandCtx ctx);

}
