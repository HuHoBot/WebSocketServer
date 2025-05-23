package cn.huohuas001.commands;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class helpCommand extends baseCommand {
    public boolean run(List<String> args) {
        log.info("可用指令:");
        log.info("  help    - 显示帮助信息");
        log.info("  exit    - 退出应用程序");
        log.info("  ban     - 封禁服务器");
        log.info("  unban   - 解封服务器");
        log.info("  list    - 查看链接数量");
        log.info("  shutdown - 关闭服务器");
        return true;
    }
}
