/**
 * Represents a graveyard search key string as an algebraic data type, implemented using a sealed interface
 * with permitted types of {@link com.winterhavenmc.savagegraveyards.models.searchkey.ValidSearchKey}
 * and {@link com.winterhavenmc.savagegraveyards.models.searchkey.InvalidSearchKey}.
 * <p>
 * SearchKey instances are validated at creation by the static factory method(s) provided in the interface,
 * and return a valid or invalid type after validation.
 * <p>
 * <img src="doc-files/SearchKey_structure.svg" alt="SearchKey Structure"/>
 */
package com.winterhavenmc.savagegraveyards.models.searchkey;
