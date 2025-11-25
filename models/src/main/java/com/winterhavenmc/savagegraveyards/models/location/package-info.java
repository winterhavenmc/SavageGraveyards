/**
 * Represents a location as an algebraic data type, implemented using a sealed interface
 * with permitted types of {@link com.winterhavenmc.savagegraveyards.models.location.ValidLocation}
 * and {@link com.winterhavenmc.savagegraveyards.models.location.InvalidLocation}.
 * <p>
 * ConfirmedLocation instances are validated at creation by the static factory method(s) provided in the interface,
 * and return a valid or invalid type after validation.
 * <p>
 * NOTE: The location world may become unavailable or invalid anytime after the creation of a ConfirmedLocation.
 * Instances of ConfirmedLocations are immutable, still be used to reference the unavailable world by UUID,
 * and the location fields will still contain valid values, including the world name and the location coordinates.
 * <p>
 * <img src="doc-files/ConfirmedLocation_structure.svg" alt="ConfirmedLocation Structure"/>
 */
package com.winterhavenmc.savagegraveyards.models.location;
