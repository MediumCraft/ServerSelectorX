package xyz.derkades.serverselectorx.actions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ToggleEffectAction extends Action {

	public ToggleEffectAction() {
		super("toggleeffect", true);
	}

	@Override
	public boolean apply(final Player player, final String value) {
		final String[] split = value.split(":");
		if (split.length < 2 || split.length > 3) {
			player.sendMessage("Invalid effect format. It should be toggleeffect:name:amplifier or toggleeffect:name:amplifier:duration");
			return true;
		}
		
		final String effectName = split[0];
		final int amplifier;
		try {
			amplifier = Integer.parseInt(split[1]);
		} catch (final NumberFormatException e) {
			player.sendMessage("'" + split[1] + "' is not a valid number");
			return true;
		}
		
		final int duration;
		if (split.length == 3) {
			try {
				duration = Integer.parseInt(split[2]);
			} catch (final NumberFormatException e) {
				player.sendMessage("'" + split[2] + "' is not a valid number");
				return true;
			}
		} else {
			duration = Integer.MAX_VALUE;
		}

		final PotionEffectType effect = PotionEffectType.getByName(effectName);

		if (effect == null) {
			player.sendMessage("Invalid effect type '" + value + "'.");
			player.sendMessage("https://github.com/ServerSelectorX/ServerSelectorX/wiki/Effects");
			return true;
		}

		if (player.hasPotionEffect(effect)) {
			player.removePotionEffect(effect);
		} else {
			if (Bukkit.getVersion().contains("1.7")) {
				player.addPotionEffect(new PotionEffect(effect, duration, amplifier, true));
			} else {
				player.addPotionEffect(new PotionEffect(effect, duration, amplifier, true, true));
			}
		}

		return false;
	}

}
