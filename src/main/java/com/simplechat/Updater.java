package com.simplechat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.*;

import static org.bukkit.Bukkit.getLogger;

public class Updater {
    int Version = 114514;




    public void UseUpdater() {
        String versionUrl = "http://cube.lichen0459.top:1145/Version.txt";
        try {
            Thread.sleep(10 * 60 * 1000); // 10分钟的毫秒数
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            try {
                Thread.sleep(10 * 60 * 1000); // 10分钟的毫秒数
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                try {
                    URL url = new URL(versionUrl);
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                        String versionStr = reader.readLine();
                        if (versionStr != null) {
                            int version = Integer.parseInt(versionStr);
                            if (version < Version) {
                                getLogger().warning("你已经落后" + (Version - version) + "个开发版本了！");
                            } else if (version > Version) {
                                getLogger().warning("这是哪个版本？自己构建的？");
                            } else if (version == Version) {
                                getLogger().info("您正在运行最新版本");
                            }
                        } else {
                            getLogger().warning("无法从版本号文件中读取版本号");
                        }
                    } catch (IOException | NumberFormatException e) {
                        getLogger().warning("无法读取或解析版本号");
                    }
                } catch (MalformedURLException e) {
                    getLogger().warning("无法获取版本！");
                }
            });
            executor.shutdown();

        }

    }
}

