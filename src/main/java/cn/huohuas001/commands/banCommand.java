package cn.huohuas001.commands;

import cn.huohuas001.tools.BanManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class banCommand extends baseCommand {
    @Override
    public boolean run(List<String> args) {
        if (args.isEmpty()) {
            log.info("用法: ban <serverId> <reason>");
            return false;
        }

        String serverId = args.get(0);
        String reason = "";
        if (args.size() > 1) {
            reason = args.get(1);
        }

        if (BanManager.banServer(serverId, reason)) {
            log.info("成功封禁服务器: {}", serverId);
            return true;
        } else {
            log.error("封禁失败");
            return false;
        }
    }
}
