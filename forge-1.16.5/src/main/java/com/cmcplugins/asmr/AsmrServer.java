package com.cmcplugins.asmr;

import com.cmcplugins.asmr.core.AsmrKind;
import com.cmcplugins.asmr.core.AsmrMode;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;

public final class AsmrServer {
    private static final Set<UUID> ACTIVE = new HashSet<>();

    private AsmrServer() {}

    public static void start(ServerPlayerEntity player, AsmrKind kind, AsmrMode mode) {
        ACTIVE.add(player.getUUID());
        AsmrNetwork.send(player, new AsmrPacket(true, kind, mode));
    }

    public static boolean stop(ServerPlayerEntity player) {
        if (!ACTIVE.remove(player.getUUID())) return false;
        AsmrNetwork.send(player, new AsmrPacket(false, AsmrKind.TAP, AsmrMode.ALTERNATE));
        return true;
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE.remove(event.getPlayer().getUUID());
    }
}
