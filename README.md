![1708678347009](https://github.com/JohnRichard4096/SimpleChat/assets/67693593/62581726-c4e0-4333-bc06-b599645d2562)
# SimpleChat

为了防止玩家间的不友好聊天，这个插件诞生了。这是一个bukkit插件，目前仅支持新的bukkit api，使用java17编译，这意味着它最低支持到1.17（这是我们确定且测试过的最稳定的最低版本）


我们自带1k+的屏蔽词，通常情况对于汉语敏感词屏蔽可以直接使用


目前插件处于测试版本阶段，我们不保证插件百分百不会出现任何bug，也不保证用户体验。


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


  enableDefaultBadWords: false

  
  importDefaultBadWords: false


2.badwords.yml

如你所见，它的格式是这样的，yml列表的形式，如果你觉得麻烦，完全可以使用指令来添加。

ForbiddenWords:
 
 —— Example

 —— Example
