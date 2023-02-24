package it.escanortargaryen;

import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.events.team.TeamDeleteEvent;
import world.bentobox.bentobox.api.events.team.TeamJoinedEvent;
import world.bentobox.bentobox.api.events.team.TeamKickEvent;
import world.bentobox.bentobox.api.events.team.TeamLeaveEvent;
import world.bentobox.bentobox.managers.RanksManager;

import java.util.UUID;

public class BBAdvancementTeamSync extends Addon implements Listener {
    private UltimateAdvancementAPI api;

    @Override
    public void onEnable() {

        if (this.getPlugin() == null || !this.getPlugin().isEnabled()) {
            this.logError("BentoBox is not available or disabled!");
            this.setState(State.DISABLED);
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if UltimateAdvancementAPI exists.
                if (!getServer().getPluginManager().isPluginEnabled("UltimateAdvancementAPI")) {
                    logError("UltimateAdvancementAPI is not available or disabled!");
                    setState(State.DISABLED);
                    return;
                }

                api = UltimateAdvancementAPI.getInstance(getPlugin());
                registerListener(BBAdvancementTeamSync.this);

            }
        }.runTaskLater(getPlugin(), 2);

    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onTeamDel(TeamDeleteEvent e) {

        for (UUID uuid : e.getIsland().getMemberSet(RanksManager.MEMBER_RANK)) {
            api.movePlayerInNewTeam(uuid);

        }

    }

    @EventHandler
    public void onTeamJoined(TeamJoinedEvent e) {

        api.updatePlayerTeam(e.getPlayerUUID(), e.getOwner());

    }

    @EventHandler
    public void onTeamKick(TeamKickEvent e) {

        api.movePlayerInNewTeam(e.getPlayerUUID());

    }

    @EventHandler
    public void onTeamLeave(TeamLeaveEvent e) {

        api.movePlayerInNewTeam(e.getPlayerUUID());

    }

}