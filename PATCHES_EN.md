# ServerMarket Patches

> [!CAUTION]
> There may be potential data security risks. Please manually back up your data before proceeding.

When you encounter bugs during use that need to be fixed, make sure that ServerMarket already has a version with the fix
before following the steps below. Update the plugin to the latest version and use the
command `/market patch <patch number>` to apply the fix.
> The patch number supports command completion.

| Patch Number |                                                             Patch Function                                                              |
|:------------:|:---------------------------------------------------------------------------------------------------------------------------------------:|
|   251-U-1    | Fixed an issue where the offline recording amount exceeded the numerical range. [#2](https://github.com/blank038/ServerMarket/issues/2) |
|   270-F-1    |              Fixed the non-existent `buyer` field in the MySQL offline record table.(`servermarket_offline_transactions`)               |