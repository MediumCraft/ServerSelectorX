package xyz.derkades.serverselectorx.actions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.derkades.serverselectorx.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Action {

	private static final Action[] DEFAULT_ACTIONS = {
			new AddEffectAction(),
			new AdvMessageAction(),
			new ChatAction(),
			new CloseAction(),
			new ConsoleCommandAction(),
			new DelayAction(),
			new FirstAvailableServerAction(),
			new MessageAction(),
			new OpenMenuAction(),
			new PlayerCommandAction(),
			new PostJoinCommandAction(),
			new RandomServerAction(),
			new RoundRobinServerAction(),
			new ServerAction(),
			new ServerGroupAction(),
			new SoundAction(),
			new TeleportAction(),
			new ToggleEffectAction(),
			new ToggleHideOthersAction(),
			new UrlAction(),
	};

	public static final List<Action> ACTIONS = new ArrayList<>();

	static {
		ACTIONS.addAll(Arrays.asList(DEFAULT_ACTIONS));
	}

	private final String name;
	private final boolean requiresValue;

	public Action(final String name, final boolean requiresValue) {
		this.name = name;
		this.requiresValue = requiresValue;
	}

	public String getName() {
		return this.name;
	}

	public boolean requiresValue() {
		return this.requiresValue;
	}

	public abstract boolean apply(Player player, String value);

	public static boolean runAction(final Player player, final String actionString) {
		boolean hasValue;
		String actionName;
		if (actionString.contains(":")) {
			hasValue = true;
			actionName = actionString.split(":")[0];
		} else {
			hasValue = false;
			actionName = actionString;
		}

		for (final Action action : ACTIONS) {
			if (action.getName().equals(actionName)) {
				if (action.requiresValue()) {
					if (hasValue) {
//						final String value = actionString.split(":")[1];
						final String value = actionString.substring(actionName.length() + 1); // remove action name and :
						return action.apply(player, value);
					} else {
						player.sendMessage("Action '" + actionName + "' requires a value");
						return true;
					}
				} else {
					if (!hasValue) {
						return action.apply(player, null);
					} else {
						player.sendMessage("Action '" + actionName + "' does not require a value");
						return true;
					}
				}
			}
		}

		player.sendMessage("Invalid action: '" + actionName + "'");
		return true;
	}

	public static boolean runActions(final Player player, final List<String> actionStrings) {
		if (actionStrings.isEmpty()) {
			return false;
		}

		boolean close = false;
		for (final String actionString : actionStrings) {
			if (runAction(player, actionString)) {
				close = true;
			}
		}

		// Hotbar items may need changing, if the actions that have been run affects conditions.
		// For example, for the 'open-menu', 'has-effect', or 'has-hidden-others' conditions.
		// Menus are opened in the next tick. Items need to be refreshed after the menu is opened,
		// so updateSsxItems() should be in a scheduled task as well.
		Bukkit.getScheduler().runTaskLater(
				Main.getPlugin(),
				() -> Main.getPlugin().getHotbarItemManager().updateSsxItems(player),
				0);

		return close;
	}

}
