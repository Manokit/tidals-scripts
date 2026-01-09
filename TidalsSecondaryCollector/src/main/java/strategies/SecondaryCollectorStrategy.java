package strategies;

import main.TidalsSecondaryCollector.State;

// base interface for all secondary collector strategies
// each secondary type (mort myre fungus, red spiders eggs, etc) implements this
public interface SecondaryCollectorStrategy {

    // determine what state we should be in based on current conditions
    State determineState();

    // verify player has required items equipped
    boolean verifyRequirements();

    // collect the secondary (bloom + pickup for fungus, etc)
    int collect();

    // teleport to bank and deposit items
    int bank();

    // restore prayer at an altar
    int restorePrayer();

    // return to collection area
    int returnToArea();
}
