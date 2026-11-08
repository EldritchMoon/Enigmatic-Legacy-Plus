package auviotre.enigmatic.legacy.contents.gui;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.contents.crafting.SpellstoneTableRecipe;
import auviotre.enigmatic.legacy.registries.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpellstoneTableMenu extends AbstractContainerMenu {
    protected final Container coreSlot = new InputContainer(1) {
        @Override
        public int getMaxStackSize() {
            return 1;
        }
    };
    protected final Container debrisSlot = new InputContainer(1);
    protected final Container spellstoneSlot = new InputContainer(1);
    protected final Container ingredientSlot = new InputContainer(7);
    protected final ResultContainer result = new ResultContainer();
    private final ContainerLevelAccess access;
    private final Player player;

    public SpellstoneTableMenu(int syncID, Inventory inventory) {
        this(syncID, inventory, ContainerLevelAccess.create(inventory.player.level(), inventory.player.blockPosition()));
    }

    public SpellstoneTableMenu(int syncID, Inventory inventory, FriendlyByteBuf buf) {
        this(syncID, inventory, ContainerLevelAccess.create(inventory.player.level(), inventory.player.blockPosition()));
    }

    public SpellstoneTableMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(EnigmaticMenus.SPELLSTONE_TABLE_MENU.get(), id);
        this.access = access;
        this.player = inventory.player;

        this.addSlot(new Slot(this.debrisSlot, 0, 27, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(EnigmaticItems.SPELLSTONE_DEBRIS);
            }

            @Override
            public Pair<Identifier, Identifier> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, EnigmaticLegacy.location("slot/empty_spellstone_debris_slot"));
            }
        });
        this.addSlot(new Slot(this.spellstoneSlot, 0, 133, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(EnigmaticTags.Items.SPELLSTONES);
            }

            @Override
            public Pair<Identifier, Identifier> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, EnigmaticLegacy.location("slot/empty_spellstone_slot"));
            }
        });
        this.addSlot(new Slot(this.coreSlot, 0, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(EnigmaticItems.SPELLCORE);
            }

            @Override
            public Pair<Identifier, Identifier> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, EnigmaticLegacy.location("slot/empty_spellcore_slot"));
            }
        });
        this.addSlot(new Slot(this.ingredientSlot, 0, 60, 55));
        this.addSlot(new Slot(this.ingredientSlot, 1, 53, 35));
        this.addSlot(new Slot(this.ingredientSlot, 2, 60, 15));
        this.addSlot(new Slot(this.ingredientSlot, 3, 80, 8));
        this.addSlot(new Slot(this.ingredientSlot, 4, 100, 15));
        this.addSlot(new Slot(this.ingredientSlot, 5, 107, 35));
        this.addSlot(new Slot(this.ingredientSlot, 6, 100, 55));
        this.addSlot(new Slot(this.result, 0, 80, 62) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player playerIn) {
                return this.hasItem();
            }

            @Override
            public void onTake(Player thePlayer, ItemStack stack) {
                this.setChanged();
                SpellstoneTableRecipe.Input input = SpellstoneTableMenu.this.getInput();
                NonNullList<ItemStack> remains = player.level().getRecipeManager().getRemainingItemsFor(EnigmaticRecipes.SPELLSTONE_CRAFTING.get(), input, player.level());
                debrisSlot.setItem(0, remains.getFirst());
                coreSlot.clearContent();
                spellstoneSlot.clearContent();
                for (int i = 2; i < remains.size(); i++) {
                    ItemStack item = ingredientSlot.getItem(i - 2);
                    ItemStack remain = remains.get(i);
                    if (!item.isEmpty()) {
                        ingredientSlot.removeItem(i - 2, 1);
                        item = ingredientSlot.getItem(i - 2);
                    }
                    if (!remain.isEmpty()) {
                        if (item.isEmpty()) {
                            ingredientSlot.setItem(i - 2, remain);
                        } else if (ItemStack.isSameItemSameComponents(item, remain)) {
                            remain.grow(item.getCount());
                            ingredientSlot.setItem(i - 2, remain);
                        } else if (player.getInventory().add(remain)) {
                            player.drop(remain, false);
                        }
                    }
                }
            }

            @Override
            public boolean isFake() {
                return true;
            }
        });

        for (int l = 0; l < 3; ++l) {
            for (int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new Slot(inventory, i1 + l * 9 + 9, 8 + i1 * 18, 84 + l * 18));
            }
        }
        for (int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(inventory, l, 8 + l * 18, 142));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        this.access.execute((level, pos) -> {
            MinecraftServer server = level.getServer();
            ItemStack output = ItemStack.EMPTY;
            if (server != null) {
                if (this.debrisSlot.isEmpty() && this.coreSlot.isEmpty() && this.ingredientSlot.isEmpty() && !this.spellstoneSlot.isEmpty()) {
                    output = EnigmaticItems.SPELLSTONE_DEBRIS.toStack(4);
                    this.result.setItem(0, output);
                    return;
                }
                SpellstoneTableRecipe.Input input = this.getInput();
                Optional<RecipeHolder<SpellstoneTableRecipe>> optional = server.getRecipeManager().getRecipeFor(EnigmaticRecipes.SPELLSTONE_CRAFTING.get(), input, level, (RecipeHolder<SpellstoneTableRecipe>) null);
                if (optional.isPresent()) {
                    SpellstoneTableRecipe recipe = optional.get().value();
                    ItemStack result = recipe.assemble(input, level.registryAccess());
                    if (result.isItemEnabled(level.enabledFeatures())) {
                        output = result;
                    }
                }
            }
            this.result.setItem(0, output);
        });
    }

    @Override
    public ItemStack quickMoveStack(Player player, int id) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(id);
        if (slot.hasItem()) {
            ItemStack selected = slot.getItem();
            stack = selected.copy();
            if (id == 10) {
                this.access.execute((level, pos) -> selected.getItem().onCraftedBy(selected, level, player));
                if (!this.moveItemStackTo(selected, 11, 47, true)) return ItemStack.EMPTY;
                slot.onQuickCraft(selected, stack);
            } else if (id > 10 && id < 47) {
                if (!this.moveItemStackTo(selected, 0, 10, false))
                    return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(selected, 11, 47, false))
                return ItemStack.EMPTY;

            if (selected.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();

            if (selected.getCount() == stack.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, selected);
        }
        return stack;
    }

    private SpellstoneTableRecipe.Input getInput() {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < this.ingredientSlot.getContainerSize(); i++)
            list.add(this.ingredientSlot.getItem(i));
        return new SpellstoneTableRecipe.Input(this.coreSlot.getItem(0), this.debrisSlot.getItem(0), list, this.spellstoneSlot.isEmpty());
    }

    public boolean hasOutput() {
        return !this.result.isEmpty();
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != this.result && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> {
            this.clearContainer(player, this.coreSlot);
            this.clearContainer(player, this.debrisSlot);
            this.clearContainer(player, this.ingredientSlot);
            this.clearContainer(player, this.spellstoneSlot);
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, EnigmaticBlocks.SPELLSTONE_TABLE.get());
    }

    public static class Provider implements MenuProvider {
        private final Component name;

        public Provider(Component name) {
            this.name = name;
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
            return new SpellstoneTableMenu(id, inventory);
        }

        @Override
        public Component getDisplayName() {
            return this.name;
        }
    }

    public class InputContainer extends SimpleContainer {
        public InputContainer(int size) {
            super(size);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            SpellstoneTableMenu.this.slotsChanged(this);
        }
    }
}
