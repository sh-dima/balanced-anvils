package io.gitlab.shdima.anvils.utils;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.List;

public final class MaterialUtils {
	private MaterialUtils() {}

	private static final List<Item> HELMETS = List.of(
			Items.LEATHER_HELMET,
			Items.CHAINMAIL_HELMET,
			Items.COPPER_HELMET,
			Items.IRON_HELMET,
			Items.GOLDEN_HELMET,
			Items.DIAMOND_HELMET,
			Items.TURTLE_HELMET
	);

	private static final List<Item> CHESTPLATES = List.of(
			Items.LEATHER_CHESTPLATE,
			Items.CHAINMAIL_CHESTPLATE,
			Items.COPPER_CHESTPLATE,
			Items.IRON_CHESTPLATE,
			Items.GOLDEN_CHESTPLATE,
			Items.DIAMOND_CHESTPLATE
	);

	private static final List<Item> LEGGINGS = List.of(
			Items.LEATHER_LEGGINGS,
			Items.CHAINMAIL_LEGGINGS,
			Items.COPPER_LEGGINGS,
			Items.IRON_LEGGINGS,
			Items.GOLDEN_LEGGINGS,
			Items.DIAMOND_LEGGINGS
	);

	private static final List<Item> BOOTS = List.of(
			Items.LEATHER_BOOTS,
			Items.CHAINMAIL_BOOTS,
			Items.COPPER_BOOTS,
			Items.IRON_BOOTS,
			Items.GOLDEN_BOOTS,
			Items.DIAMOND_BOOTS
	);

	private static final List<Item> SWORDS = List.of(
			Items.WOODEN_SWORD,
			Items.STONE_SWORD,
			Items.COPPER_SWORD,
			Items.IRON_SWORD,
			Items.GOLDEN_SWORD,
			Items.DIAMOND_SWORD
	);

	private static final List<Item> SPEARS = List.of(
			Items.WOODEN_SPEAR,
			Items.STONE_SPEAR,
			Items.COPPER_SPEAR,
			Items.IRON_SPEAR,
			Items.GOLDEN_SPEAR,
			Items.DIAMOND_SPEAR
	);

	private static final List<Item> PICKAXES = List.of(
			Items.WOODEN_PICKAXE,
			Items.STONE_PICKAXE,
			Items.COPPER_PICKAXE,
			Items.IRON_PICKAXE,
			Items.GOLDEN_PICKAXE,
			Items.DIAMOND_PICKAXE
	);

	private static final List<Item> AXES = List.of(
			Items.WOODEN_AXE,
			Items.STONE_AXE,
			Items.COPPER_AXE,
			Items.IRON_AXE,
			Items.GOLDEN_AXE,
			Items.DIAMOND_AXE
	);

	private static final List<Item> SHOVELS = List.of(
			Items.WOODEN_SHOVEL,
			Items.STONE_SHOVEL,
			Items.COPPER_SHOVEL,
			Items.IRON_SHOVEL,
			Items.GOLDEN_SHOVEL,
			Items.DIAMOND_SHOVEL
	);

	private static final List<Item> HOES = List.of(
			Items.WOODEN_HOE,
			Items.STONE_HOE,
			Items.COPPER_HOE,
			Items.IRON_HOE,
			Items.GOLDEN_HOE,
			Items.DIAMOND_HOE
	);

	private static final List<Item> NETHERITE_ITEMS = List.of(
			Items.NETHERITE_HELMET,
			Items.NETHERITE_CHESTPLATE,
			Items.NETHERITE_LEGGINGS,
			Items.NETHERITE_BOOTS,

			Items.NETHERITE_SWORD,
			Items.NETHERITE_SPEAR,
			Items.NETHERITE_PICKAXE,
			Items.NETHERITE_AXE,
			Items.NETHERITE_SHOVEL,
			Items.NETHERITE_HOE
	);

	public static int getMaterialCost(@NonNull ItemStack itemStack) {
		Item item = itemStack.getItem();

		// Armor
		if (HELMETS.contains(item)) return 5;
		if (CHESTPLATES.contains(item)) return 8;
		if (LEGGINGS.contains(item)) return 7;
		if (BOOTS.contains(item)) return 4;

		// Tools / weapons
		if (SWORDS.contains(item)) return 2;
		if (SPEARS.contains(item)) return 1;
		if (PICKAXES.contains(item)) return 3;
		if (AXES.contains(item)) return 3;
		if (SHOVELS.contains(item)) return 1;
		if (HOES.contains(item)) return 2;
		if (NETHERITE_ITEMS.contains(item)) return 1;

		// Misc. items
		if (item == Items.SHIELD) return 6;
		if (item == Items.MACE) return 1;
		if (item == Items.WOLF_ARMOR) return 6;

		return 4; // Default
	}
}
