package xyz.derkades.serverselectorx;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import xyz.derkades.derkutils.Cooldown;
import xyz.derkades.serverselectorx.conditional.ConditionalItem;

import java.util.logging.Logger;

public class ItemClickListener implements Listener {

	@EventHandler(priority = EventPriority.HIGH)
	public void onInteract(final PlayerInteractEvent event) {
		Logger logger = Main.getPlugin().getLogger();

		if (Main.ITEM_DEBUG) {
			logger.info("[Click debug] Player " + event.getPlayer().getName() + " performed action " + event.getAction() + " on item using " + event.getHand());
		}

		if (event.getAction() == Action.PHYSICAL) {
			if (Main.ITEM_DEBUG) {
				logger.info("[Click debug] Event was ignored because it was not a click event");
			}
			return;
		}

		final FileConfiguration inventory = Main.getConfigurationManager().getInventoryConfiguration();

		if (event.isCancelled() && inventory.getBoolean("ignore-cancelled", false)) {
			if (Main.ITEM_DEBUG) {
				logger.info("[Click debug] Event was ignored because it was cancelled and ignore-cancelled is enabled");
			}
			return;
		}

		final ItemStack item = event.getItem();

		if (item == null || item.getType() == Material.AIR) {
			if (Main.ITEM_DEBUG) {
				logger.info("[Click debug] Event was ignored because the item was AIR");
			}
			return;
		}
		
		final NBTItem nbt = new NBTItem(item);

		if (!nbt.hasTag("SSXActions")) {
			if (Main.ITEM_DEBUG) {
				logger.info("[Click debug] Event was ignored because the clicked item is not an SSX item");
			}
			// Not an SSX item
			return;
		}
		
		// Event canceling needs to happen before the menu open check, because we don't want
		// the player to be able to place blocks due to interact events while the menu is open.
		final boolean cancel = inventory.getBoolean("cancel-click-event", false);
		if (cancel) {
			if (Main.ITEM_DEBUG) {
				logger.info("[Click debug] The event has been cancelled, because cancel-click-event is enabled");
			}
			event.setCancelled(true);
		}

		final Player player = event.getPlayer();
		
		// 1.16 triggers interact events when clicking items in a menu for some reason
		// We need to ignore these
		// If the player does not have an open inventory, getOpenInventory returns their crafting or creative inventory
		if (player.getOpenInventory().getType() != InventoryType.CRAFTING &&
				player.getOpenInventory().getType() != InventoryType.CREATIVE) {
			if (Main.ITEM_DEBUG) {
				logger.info("[Click debug] Event was ignored because the player had an open inventory: " + player.getOpenInventory().getType());
			}
			return;
		}

		// this cooldown is added when the menu closes
		String globalCooldownId = player.getName() + "ssxitemglobal";
		if (Cooldown.getCooldown(globalCooldownId) > 0) {
			if (Main.ITEM_DEBUG) {
				logger.info("[Click debug] Event was ignored because the global cooldown was in effect (this cooldown cannot be disabled)");
			}
			return;
		}
		Cooldown.addCooldown(globalCooldownId, 100);

		ConditionalItem.runActions(event);

		if (Main.ITEM_DEBUG) {
			logger.info("[Click debug] Event handling completed, actions have been run.");
		}
	}

}
