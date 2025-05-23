package cn.huohuas001.tools;

import java.util.ArrayList;
import java.util.List;

public class CommandHelper {

    /**
     * 切割命令参数（支持带引号的参数）
     *
     * @param params 输入参数字符串
     * @return 切割后的参数列表
     */
    public static List<String> splitCommandParams(String params) {
        List<String> result = new ArrayList<>();
        if (params == null || params.trim().isEmpty()) {
            return result;
        }

        StringBuilder current = new StringBuilder();
        boolean inQuote = false;

        for (int i = 0; i < params.length(); i++) {
            char c = params.charAt(i);

            if (c == '"') {
                if (inQuote && i > 0 && params.charAt(i - 1) != '\\') {
                    // 结束引号
                    result.add(current.toString());
                    current.setLength(0);
                    inQuote = false;
                } else if (!inQuote) {
                    // 开始引号
                    inQuote = true;
                } else {
                    // 转义的引号
                    current.append(c);
                }
            } else if (Character.isWhitespace(c) && !inQuote) {
                // 非引号内的空格分割
                if (current.length() > 0) {
                    result.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }

        // 添加最后一个参数
        if (current.length() > 0) {
            result.add(current.toString());
        }

        return result;
    }
}
