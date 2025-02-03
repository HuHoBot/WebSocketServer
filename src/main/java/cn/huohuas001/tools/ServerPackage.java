package cn.huohuas001.tools;

import cn.huohuas001.client.ServerClient;

public class ServerPackage{
    private String serverId;
    private ServerClient serverClient;

    public ServerPackage(String serverId, ServerClient serverClient) {
        this.serverId = serverId;
        this.serverClient = serverClient;
    }
    public String getServerId() {
        return serverId;
    }
    public ServerClient getServerClient() {
        return serverClient;
    }
}
