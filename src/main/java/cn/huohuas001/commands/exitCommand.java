package cn.huohuas001.commands;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class exitCommand extends baseCommand {
    public boolean run(List<String> args) {
        log.info("正在关闭服务器...");
        System.exit(0);
        return true;
    }
}
