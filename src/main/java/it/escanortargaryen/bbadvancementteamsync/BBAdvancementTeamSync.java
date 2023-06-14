package it.escanortargaryen.bbadvancementteamsync;

import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.events.team.AsyncTeamUpdateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.events.island.IslandDeleteEvent;
import world.bentobox.bentobox.api.events.team.TeamDeleteEvent;
import world.bentobox.bentobox.api.events.team.TeamJoinedEvent;
import world.bentobox.bentobox.api.events.team.TeamKickEvent;
import world.bentobox.bentobox.api.events.team.TeamLeaveEvent;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.RanksManager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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
                islandWorldManager = BentoBox.getInstance().getIWM();

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

            try {
                api.movePlayerInNewTeam(uuid).get();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            } catch (ExecutionException ex) {
                throw new RuntimeException(ex);
            }

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

    @EventHandler
    public void onIslandDelete(IslandDeleteEvent e) {
        for (UUID uuid : e.getIsland().getMemberSet(RanksManager.MEMBER_RANK)) {

            api.movePlayerInNewTeam(uuid);

        }
    }

    // ===================================================
    // Sync from UltimateAdvancementAPI to BentoBox

    @EventHandler
    public void onTeam(AsyncTeamUpdateEvent e) {

        new BukkitRunnable() {
            AsyncTeamUpdateEvent.Action action = e.getAction();
            UUID player = e.getPlayerUUID();
            TeamProgression progression = e.getTeamProgression();

            @Override
            public void run() {

                switch (action) {

                    case JOIN -> {
                        if (islandWorldManager.getOverWorlds().size() > 0 && islandWorldManager.getOverWorlds().get(0) != null) {
                            Island i = BentoBox.getInstance().getIslands().getIsland(islandWorldManager.getOverWorlds().get(0), progression.getAMember());
                            if (i != null) {

                                BentoBox.getInstance().getIslandsManager().setJoinTeam(i, player);
                            }

                        }

                    }
                    case LEAVE -> {

                        if (islandWorldManager.getOverWorlds().size() > 0 && islandWorldManager.getOverWorlds().get(0) != null) {
                            Island i = BentoBox.getInstance().getIslands().getIsland(islandWorldManager.getOverWorlds().get(0), player);
                            if (i != null) {

                                if (i.getOwner().equals(player)) {
                                    for (UUID uuid : i.getMemberSet(RanksManager.MEMBER_RANK)) {
                                        BentoBox.getInstance().getIslandsManager().removePlayer(islandWorldManager.getOverWorlds().get(0), uuid);
                                        try {
                                            api.movePlayerInNewTeam(e.getPlayerUUID()).get();
                                        } catch (InterruptedException ex) {
                                            throw new RuntimeException(ex);
                                        } catch (ExecutionException ex) {
                                            throw new RuntimeException(ex);
                                        }
                                    }
                                }

                                BentoBox.getInstance().getIslandsManager().removePlayer(islandWorldManager.getOverWorlds().get(0), player);
                            }

                        }

                    }

                }
            }
        }.runTask(getPlugin());

    }

}