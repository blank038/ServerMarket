# ServerMarket Patches

> [!CAUTION]
> 可能存在数据安全隐患，需要您手动备份数据后再执行

当您在使用过程中遇到一些 BUG，需要进行修复时确定 ServerMarket 已经有修复的版本再执行下面的步骤，
将插件更新至最新版，通过执行命令 `/market patch <补丁编号>` 来修复，补丁编号支持命令补全。

|  补丁编号   |                                   补丁功能                                   |
|:-------:|:------------------------------------------------------------------------:|
| 251-U-1 | 修复离线记录金额超过数值范围，例：[#2](https://github.com/blank038/ServerMarket/issues/2) |
| 270-F-1 |    修复 MySQL 离线记录表(`servermarket_offline_transactions`) `buyer` 字段不存在     |