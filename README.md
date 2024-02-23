# SimpleChat

为了防止玩家间的不友好聊天，这个插件诞生了。目前仅支持新的bukkit api，使用java17编译，这意味着它最低支持到1.17


目前插件正在开发中，将在不久后上线


# 指令帮助:


/schat 以获取帮助


/schat-relaod 重载所有配置文件（需要schat.reload节点，默认op拥有）



/schat-addbadword <badword> 增加屏蔽词（需要achat.add权限，默认op拥有）


/schat-delbadword <badword> 移除屏蔽词（需要schat.del权限，默认op拥有）


/schat-list 列出全部违禁词（需要schat.list权限，默认op拥有）


/schat-mute <player> [reason] [time]禁言玩家（需要schat.mute默认op，时间用tick计算，游戏内默认每秒20tick）


/schat unmute <player> 解除玩家禁言（需要schat.unmute节点默认op）


/schat-undo <action> <player> 回溯操作（mute,unmute,restore)




# -------YML------


其中有两个YML文件随着插件启用而生成


1.config.yml


它默认应该是这样的


banConfiguration:
  
  
  violationThreshold: 5
  
  
  #达到多少次禁言
  
  
  banDuration: 6000
  
  
  #禁言时间（tick）


2.badwords.yml

如你所见，它的格式是这样的，yml列表的形式，如果你觉得麻烦，完全可以使用指令来添加。

ForbiddenWords:
 
 —— Example

 —— Example
