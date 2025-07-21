package cn.huohuas001.events.Server;

import cn.huohuas001.client.BotClient;
import cn.huohuas001.events.Bot.EventEnum.BotClientSendEvent;
import cn.huohuas001.tools.ClientManager;

public class Server_handleChat extends ServerEvent {

    public Server_handleChat(ClientManager clientManager) {
        super(clientManager);
    }

    @Override
    public boolean run() {
        BotClient botClient = clientManager.getBotClient();
        botClient.sendMessage(BotClientSendEvent.CHAT, body, packId); //消息转发至BotClient
        return true;
    }
}
