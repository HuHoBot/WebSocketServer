package cn.huohuas001.events.Bot;

import cn.huohuas001.events.Bot.EventEnum.BotClientSendEvent;
import cn.huohuas001.tools.ClientManager;

public class Bot_handleHeart extends BotEvent {

    public Bot_handleHeart(ClientManager clientManager) {
        super(clientManager);
    }

    @Override
    public boolean run() {
        botClient.sendMessage(BotClientSendEvent.HEART, body, packId);
        botClient.updateLastHeartbeatTime();
        return true;
    }
}
