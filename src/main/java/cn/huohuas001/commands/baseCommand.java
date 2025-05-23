package cn.huohuas001.commands;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class baseCommand {
    public abstract boolean run(List<String> args);
}
