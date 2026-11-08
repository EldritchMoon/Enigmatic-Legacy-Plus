package auviotre.enigmatic.legacy.contents.attachement;

import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class IchorPermeation implements ValueIOSerializable {
    private boolean infected = false;

    public boolean isInfected() {
        return this.infected;
    }

    public void setInfected(boolean infected) {
        this.infected = infected;
    }

    // 26.1.2-port: ValueIOSerializable replaces INBTSerializable; same key as before.
    @Override
    public void serialize(ValueOutput output) {
        output.putBoolean("IchorPermeation", this.infected);
    }

    @Override
    public void deserialize(ValueInput input) {
        this.infected = input.getBooleanOr("IchorPermeation", false);
    }
}
