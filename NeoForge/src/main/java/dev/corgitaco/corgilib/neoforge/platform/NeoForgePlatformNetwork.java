package dev.corgitaco.corgilib.neoforge.platform;

import com.google.auto.service.AutoService;
import corgitaco.corgilib.network.Packet;
import corgitaco.corgilib.platform.PlatformNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

@AutoService(PlatformNetwork.class)
public class NeoForgePlatformNetwork implements PlatformNetwork {
    @Override
    public <P extends Packet> void sendToClient(ServerPlayer player, P packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    @Override
    public <P extends Packet> void sendToServer(P packet) {
        PacketDistributor.sendToServer(packet);
    }
}
