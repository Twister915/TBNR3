package net.tbnr.dev.signs;

import lombok.Data;
import net.cogzmc.core.Core;
import net.cogzmc.core.network.NetworkServer;
import net.cogzmc.core.util.Point;
import net.cogzmc.core.util.Region;
import net.tbnr.dev.Game;
import net.tbnr.dev.ServerHelper;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.*;

@Data
public final class ServerSignMatrix {
    private final Region region;
    private final World world;
    private final Set<ServerSign> signSet;
    private final Game game;

    public ServerSignMatrix(Region region, World world, Game game) {
        this.region = region;
        this.game = game;
        this.world = world;
        this.signSet = new LinkedHashSet<>();

    }

    public ServerSign getSignAt(Point point) {
        for (ServerSign serverSign : signSet) {
            if (serverSign.getPoint().equals(point)) return serverSign;
        }
        return null;
    }

    public void update() {
        if (Core.getNetworkManager() == null) return;
        List<NetworkServer> servers = ServerHelper.getServers(game);
        Iterator<NetworkServer> iterator = servers.iterator();
        Collections.sort(servers, new Comparator<NetworkServer>() {
            @Override
            public int compare(NetworkServer o1, NetworkServer o2) {
                return getServerNumber(o1)-getServerNumber(o2);
            }
        });
        signSet.clear();
        for (Double y = region.getMax().getY(); y >= region.getMin().getY(); y--) {
            for (Double x = region.getMax().getX(); x >= region.getMin().getX(); x--) {
                for (Double z = region.getMin().getZ(); z <= region.getMax().getZ(); z++) {
                    Block blockAt = world.getBlockAt(x.intValue(), y.intValue(), z.intValue());
                    if (blockAt.getType() == Material.WALL_SIGN || blockAt.getType() == Material.SIGN_POST) signSet.add(new ServerSign(Point.of(blockAt), this));
                }
            }
        }
        for (ServerSign aSignSet : signSet) {
            aSignSet.update(iterator.hasNext() ? iterator.next() : null);
        }
    }

    public static Integer getServerNumber(NetworkServer server) {
        String name = server.getName();
        StringBuilder number = new StringBuilder();
        for (int x = name.length()-1; x >= 0; x--) {
            char c = name.charAt(x);
            if (c > '9' || c < '0') break;
            number.append(c);
        }
        String serverNumber = number.reverse().toString();
        return Integer.valueOf(serverNumber);
    }
}
