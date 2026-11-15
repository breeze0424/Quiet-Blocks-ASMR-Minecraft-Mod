package com.cmcplugins.asmr;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public final class AsmrNetwork {
    private static final SimpleChannel CHANNEL = ChannelBuilder.named("asmr:main")
        .networkProtocolVersion(5).simpleChannel();

    private AsmrNetwork() {}

    public static void register() {
        CHANNEL.messageBuilder(AsmrPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(AsmrPacket::encode)
            .decoder(AsmrPacket::decode)
            .consumerMainThread((message, context) -> {
                if (FMLEnvironment.dist.isClient()) {
                    if (message.openMenu) ClientAsmr.openMenu();
                    else ClientAsmr.receive(message);
                }
            })
            .add();
        CHANNEL.build();
    }

    public static void send(ServerPlayer player, AsmrPacket message) {
        CHANNEL.send(message, PacketDistributor.PLAYER.with(player));
    }
}
