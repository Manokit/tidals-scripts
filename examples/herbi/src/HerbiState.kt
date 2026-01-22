import com.osmb.api.location.position.types.WorldPosition
import data.HerbiboarSearchSpot

sealed class HerbiState {
    object OpenInventory : HerbiState()
    object CloseInventory : HerbiState()
    object WalkingToStartingObject : HerbiState()
    object InteractWithStartingObject : HerbiState()
    data class WalkingToNextLocation(val nextLoc: HerbiboarSearchSpot) : HerbiState()
    data class InteractingWithMushroom(val loc: WorldPosition) : HerbiState()
    data class InteractingWithSeaweed(val worldPosition: WorldPosition) : HerbiState()
    data class InteractingWithMuddyPatch(val loc: WorldPosition) : HerbiState()
    data class InteractingWithTunnel(val loc: WorldPosition) : HerbiState()
    data class HarvestingHerbi(val longPress: Boolean) : HerbiState()
    object SettingGameChatFilter : HerbiState()
    object DrinkingEnergyRestore : HerbiState()
    object DetermineNextLocation : HerbiState()
    data class Resetting(val relog: Boolean) : HerbiState()
    object SettingZoomLevel : HerbiState()
    object WaitingForIdle : HerbiState()
    object DropVials : HerbiState()
    object ClosingChatDialog : HerbiState()

    data class Error(val reason: String) : Banking()

    data class MovingToTile(val worldPosition: WorldPosition): HerbiState()

    sealed class Banking : HerbiState() {
        object WalkingToBank : Banking()
        data class TraversingViaBoat(val destination: Destination) : Banking() {
            enum class Destination(val destinationTile: WorldPosition) {
                ISLAND(WorldPosition(3764, 3899, 0)),
                MAIN_LAND(WorldPosition(3733, 3893, 0)),
            }
        }
        object OpeningBank : Banking()
        object DepositingItems : Banking()
        object WithdrawalItems : Banking()
        object ClosingBank : Banking()
    }
}


fun HerbiState.toUIString(): String {
    return when (this) {
        is HerbiState.SettingGameChatFilter -> "Setting Game Chat filter"
        is HerbiState.DropVials -> "Dropping empty vials"
        is HerbiState.ClosingChatDialog -> "Closing Chat Dialog"
        is HerbiState.CloseInventory -> "Closing Inventory"
        is HerbiState.Error -> "Error: ${this.reason}"
        is HerbiState.DrinkingEnergyRestore -> "Drinking Energy Restore"
        is HerbiState.Banking.WithdrawalItems -> "Withdrawing Items"
        is HerbiState.Banking.WalkingToBank -> "Walking to Bank"
        is HerbiState.Banking.TraversingViaBoat -> "Traversing via Boat to ${this.destination}"
        is HerbiState.Banking.OpeningBank -> "Opening Bank"
        is HerbiState.Banking.DepositingItems -> "Depositing Items"
        is HerbiState.Banking.ClosingBank -> "Closing Bank"
        is HerbiState.WaitingForIdle -> "Waiting for Idle"
        is HerbiState.SettingZoomLevel -> "Setting Zoom Level"
        is HerbiState.MovingToTile -> "Moving off blocked tile..."
        is HerbiState.Resetting -> "Invalid state -> Resetting session"
        is HerbiState.OpenInventory -> "Opening Inventory"
        is HerbiState.WalkingToStartingObject -> "Walking to Starting Object"
        is HerbiState.InteractWithStartingObject -> "Interacting with Starting Object"
        is HerbiState.WalkingToNextLocation -> "Walking to next location (${nextLoc.type} at ${nextLoc.group})"
        is HerbiState.InteractingWithMushroom -> "Interacting with Mushroom"
        is HerbiState.InteractingWithSeaweed -> "Interacting with Seaweed at (${worldPosition.x}, ${worldPosition.y}, 0)"
        is HerbiState.InteractingWithMuddyPatch -> "Interacting with Muddy Patch"
        is HerbiState.InteractingWithTunnel -> "Interacting with Tunnel"
        is HerbiState.HarvestingHerbi -> "Harvesting Herbi"
        is HerbiState.DetermineNextLocation -> "Determining Next Location"
    }
}
