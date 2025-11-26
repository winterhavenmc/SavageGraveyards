/**
 * Represents a world as an algebraic data type, implemented using a sealed interface
 * with permitted types of {@link com.winterhavenmc.savagegraveyards.models.world.ValidWorld}
 * and {@link com.winterhavenmc.savagegraveyards.models.world.InvalidWorld}.
 * <p>
 * ConfirmedWorld instances are validated at creation by the static factory method(s) provided in the interface,
 * and return a valid or invalid type after validation.
 * <p>
 * NOTE: The world may become unavailable or invalid anytime after the creation of a ConfirmedWorld instance.
 * Instances of ConfirmedWorld are immutable, and may still be used to reference the unavailable world UUID
 * and name fields contained in all variants of the {@code ConfirmedWorld} type. Instances of {@code InvalidWorld}
 * may have a symbolic indicating a null or blank name.
 * <p>
 * <img src="doc-files/ConfirmedWorld_structure.svg" alt="ConfirmedWorld Structure"/>
 */
package com.winterhavenmc.savagegraveyards.models.world;
