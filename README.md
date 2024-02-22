# SimpleChat

为了防止玩家间的不友好聊天，这个插件诞生了。目前仅支持新的bukkit api，这意味着它最低支持到1.14.x
目前插件正在开发中，将在不就后上线
指令帮助:
/schat help 以获取帮助（需要schat.help节点，默认op拥有）
/schat 以获取版本（需要schat.info节点，默认op拥有）
/schat relaod 重载所有配置文件（需要schat.reload节点，默认op拥有）
/schat addbadword <badword> 增加屏蔽词（需要achat.add权限，默认op拥有）
/schat delbadword <badword> 移除屏蔽词（需要schat.del权限，默认op拥有）
/schat list 列出全部违禁词（需要schat.list权限，默认op拥有）
/schat mute <player> <time> <[reason]>禁言玩家（需要schat.mute默认op，时间用tick计算，游戏内默认每秒20tick其中[]为可选选项）
/schat unmute <player> 解除玩家禁言（需要schat.unmute节点默认op）


------------YML------------
其中有两个YML文件随着插件启用而生成
1.config.yml
2.badworda.yml
