package com.cmcplugins.asmr;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.Connection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class AsmrNetwork {
    private static final String VERSION = "5";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation("asmr", "main"), () -> VERSION,
        NetworkRegistry.acceptMissingOr(VERSION), VERSION::equals);

    private AsmrNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(0, AsmrPacket.class, AsmrPacket::encode, AsmrPacket::decode, (message, context) -> {
            context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> {
                    if (message.openMenu) ClientAsmr.openMenu();
                    else ClientAsmr.receive(message);
                }));
            context.get().setPacketHandled(true);
        });
    }

    public static void send(ServerPlayer player, AsmrPacket message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static boolean isRemotePresent(Connection connection) {
        return CHANNEL.isRemotePresent(connection);
    }
}
