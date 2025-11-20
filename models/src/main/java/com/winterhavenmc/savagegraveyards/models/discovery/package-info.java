/**
 * Represents a player/graveyard discovery event as an algebraic data type, implemented using a sealed interface
 * with permitted types of {@link com.winterhavenmc.savagegraveyards.models.discovery.ValidDiscovery}
 * or {@link com.winterhavenmc.savagegraveyards.models.discovery.InvalidDiscovery}.
 * <p>
 * Discovery instances are validated at creation by the static factory method(s) provided in the interface,
 * and return a valid or invalid type after validation.
 * <p>
 * <img src="doc-files/Discovery_structure.svg" alt="Discovery Structure"/>
 */
package com.winterhavenmc.savagegraveyards.models.discovery;
