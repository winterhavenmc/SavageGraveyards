/**
 * Represents a graveyard as an algebraic data type, implemented using a sealed interface
 * with permitted types of {@link com.winterhavenmc.savagegraveyards.models.graveyard.ValidGraveyard}
 * and {@link com.winterhavenmc.savagegraveyards.models.graveyard.InvalidGraveyard}.
 * <p>
 * Graveyard instances are validated at creation by the static factory method(s) provided in the interface,
 * and return a valid or invalid type after validation.
 * <p>
 * <img src="doc-files/Graveyard_structure.svg" alt="Graveyard Structure"/>
 */
package com.winterhavenmc.savagegraveyards.models.graveyard;
