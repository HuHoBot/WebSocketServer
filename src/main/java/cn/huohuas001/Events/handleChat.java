package cn.huohuas001.Events;

import cn.huohuas001.client.BotClient;
import cn.huohuas001.tools.ClientManager;

public class handleChat extends BaseEvent {

    public handleChat(ClientManager clientManager) {
        super(clientManager);
    }

    @Override
    public boolean run() {
        BotClient botClient = clientManager.getBotClient();
        botClient.sendMessage(BotClientSendEvent.CHAT, body, packId); //消息转发至BotClient
        return true;
    }
}
