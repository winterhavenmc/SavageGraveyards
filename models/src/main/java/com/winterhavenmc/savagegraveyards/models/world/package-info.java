/**
 * Represents a world as an algebraic data type, implemented using a sealed interface
 * with permitted types of {@link com.winterhavenmc.savagegraveyards.models.world.ValidWorld}
 * and {@link com.winterhavenmc.savagegraveyards.models.world.InvalidWorld}.
 * <p>
 * ConfirmedWorld instances are validated at creation by the static factory method(s) provided in the interface,
 * and return a valid or invalid type after validation.
 * <p>
 * NOTE: The world may become unavailable or invalid anytime after the creation of a ConfirmedWorld instance.
 * Instances of ConfirmedWorld are immutable, and may still be used to reference the unavailable world by UUID,
 * and the location fields will still contain valid values, including the world name and the location coordinates.
 * <p>
 * <img src="doc-files/ConfirmedWorld_structure.svg" alt="ConfirmedWorld Structure"/>
 */
package com.winterhavenmc.savagegraveyards.models.world;
