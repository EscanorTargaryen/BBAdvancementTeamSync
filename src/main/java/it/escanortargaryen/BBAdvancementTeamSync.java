package it.escanortargaryen;

import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.events.team.AsyncTeamUpdateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.events.team.TeamDeleteEvent;
import world.bentobox.bentobox.api.events.team.TeamJoinedEvent;
import world.bentobox.bentobox.api.events.team.TeamKickEvent;
import world.bentobox.bentobox.api.events.team.TeamLeaveEvent;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.RanksManager;

import java.util.UUID;

public class BBAdvancementTeamSync extends Addon implements Listener {
    private UltimateAdvancementAPI api;
    private IslandWorldManager islandWorldManager;

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
                islandWorldManager = new IslandWorldManager(getPlugin());

            }
        }.runTaskLater(getPlugin(), 2);

    }

    @Override
    public void onDisable() {
    }

    // Sync from BentoBox to UltimateAdvancementAPI

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

    // ===================================================
    // Sync from UltimateAdvancementAPI to BentoBox

    @EventHandler
    public void onTeam(AsyncTeamUpdateEvent e) {

        switch (e.getAction()) {

            case JOIN -> {

                if (islandWorldManager.getOverWorlds().get(0) != null) {
                    Island i = BentoBox.getInstance().getIslands().getIsland(islandWorldManager.getOverWorlds().get(0), e.getTeamProgression().getAMember());
                    i.addMember(e.getPlayerUUID());
                }

            }
            case LEAVE -> {
               
                if (islandWorldManager.getOverWorlds().get(0) != null) {
                    Island i = BentoBox.getInstance().getIslands().getIsland(islandWorldManager.getOverWorlds().get(0), e.getTeamProgression().getAMember());
                    i.removeMember(e.getPlayerUUID());
                }

            }

        }

    }

}