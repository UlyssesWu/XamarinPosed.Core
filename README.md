# XamarinPosed.Core
Xposed module using Xamarin & .NET 🐱‍💻 | 使用.NET Android （Xamarin Android）实现Xposed模块。

Current stable branch is [.NET 8](https://github.com/UlyssesWu/XamarinPosed.Core/tree/net8)

For classic Xamarin (Xamarin.Android) version, please check [XamarinPosed](https://github.com/UlyssesWu/XamarinPosed)

## Limitations
The app startup time would be slightly longer because of mono initialization. But hey, who cares? Welcome to the **.NET** world.

The module app won't be able to launch directly after you enable it as a Xposed module. 
That's because the same native libraries are already loaded by the module's ClassLoader, so the APK's ClassLoader won't be able to use them.

It's recommended to make a seperate app if you need to configure the module.

**"Xposed API call protection" feature (LSPosed settings) must be turned off.**

For .NET 10+, `InitZygote` will not be invoked in real time but before the first call of `HandleLoadPackage`. This is a limitation due to .NET Android implementation (`MainThreadDsoLoader` related). If you do need a real `InitZygote`, use [.NET 8](https://github.com/UlyssesWu/XamarinPosed.Core/tree/net8).

## Usage
Use Visual Studio 2026.

Clone this repo and implement your `InitZygote`, `HandleLoadPackage`, `HandleInitPackageResources` in `XamarinPosed\Loader.cs`.

(`HandleInitPackageResources` is disabled by default. To enable it, remove `VXP` from XaraminPosed Properties - Conditional Compilation Symbols)

Archive or deploy `XamarinPosed` project with release config. It will be a Xposed module apk. 

## Hints
### How to change xposed module name?
Edit `XamarinPosed.csproj` - `ApplicationId`.

### How to enable Resource hook?
Since resource hook is not supported by VirtualXposed, it's disabled by default. To enable it:

XamarinPosed Project Properties - Build - Conditional Compilation Symbols - remove "VXP"

## License

XamarinPosed.Core is licensed under the MIT License.

Please retain the LICENSE file or include the following attribution in your repository and distributions:

> XamarinPosed.Core - by Ulysses (wdwxy12345{at}gmail.com). Licensed under the MIT License.

# XamarinPosed.Core 中文说明

当前稳定分支为[.NET 8](https://github.com/UlyssesWu/XamarinPosed.Core/tree/net8)分支。.NET 10可用但有一定限制。

此外还有面向Xamarin Android（非.NET Core）的经典版本[XamarinPosed](https://github.com/UlyssesWu/XamarinPosed)。

## 限制
App启动时间会稍微变长，因为.NET/mono启动需要一点时间。

一个模块App一旦在Xposed中启用，就无法直接作为App打开。因为native so只能由1个ClassLoader加载，如果直接打开就会被加载2次，导致闪退。如果你需要通过图形化界面进行某些配置，建议另行实现一个App。

**LSPosed等方案中的“Xposed API 调用保护”功能必须关闭，否则模块无法正常生效。**

对于 .NET 10+ 版本，`InitZygote`无法在正常时机被调用，而是会在第一次执行`HandleLoadPackage`之前被调用。这是由于.NET Android的实现机制导致无法适配。如果你需要`InitZygote`在较早时机调用，请使用[.NET 8](https://github.com/UlyssesWu/XamarinPosed.Core/tree/net8)版本。

## 使用方式
.NET 10分支：VS 2026 + .NET 10

.NET 8分支：VS 2022/2026 + .NET 8/9

在`XamarinPosed\Loader.cs`中实现`InitZygote`, `HandleLoadPackage`, `HandleInitPackageResources`等逻辑。其中`HandleInitPackageResources`默认不启用。

以Release配置Archive（某些地方翻译成“存档”） XamarinPosed 项目。产物即为Xposed模块apk。

如果提示缺少Android SDK，请自行安装36.0版本（可通过Android Studio的SDK Manager安装）。如果提示缺少Java SDK也请自行安装。

## 常见问题
### 如何修改模块名称？
修改 `XamarinPosed.csproj` - `ApplicationId`。不要修改程序集名称、命名空间、`xposed_init`文件等。

### 如何启用资源Hook
由于资源Hook（`HandleInitPackageResources`）无法在VirtualXposed中使用，因此本项目默认不启用。

如需启用，请在 `XamarinPosed`项目属性 - 生成 - 常规 - 条件编译符号 中取消勾选`VXP`

## 开源协议
MIT。请在您的公开发布二进制或仓库中保留本项目的`LICENSE`或标注以下文本：

> XamarinPosed.Core - by Ulysses (wdwxy12345{at}gmail.com). Licensed under the MIT License.

------

by Ulysses (wdwxy12345{at}gmail.com)