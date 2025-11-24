/**
 * Represents a graveyard display name as an algebraic data type, implemented using a sealed interface
 * with permitted types of {@link com.winterhavenmc.savagegraveyards.models.displayname.ValidDisplayName}
 * and {@link com.winterhavenmc.savagegraveyards.models.displayname.InvalidDisplayName}.
 * <p>
 * DisplayName instances are validated at creation by the static factory method(s) provided in the interface,
 * and return a valid or invalid type after validation.
 * <p>
 * <img src="doc-files/DisplayName_structure.svg" alt="DisplayName Structure">
 */

package com.winterhavenmc.savagegraveyards.models.displayname;
