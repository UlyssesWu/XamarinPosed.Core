# XamarinPosed.Core
Xposed module using Xamarin & .NET 8 🐱‍💻

This is the latest version of [XamarinPosed](https://github.com/UlyssesWu/XamarinPosed), with .NET 8 support.

## Limitations
The app startup time would be slightly longer because of mono initialization. But hey, who cares? Welcome to the **.NET** world.

The module app won't be able to launch directly after you enable it as a Xposed module. 
That's because the same native libraries are already loaded by the module's ClassLoader, so the APK's ClassLoader won't be able to use them.

It's recommended to make a seperate app if you need to configure the module.

## Usage
Please use the newest stable version of Visual Studio and Xamarin.

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

XamarinPosed.Core is licensed under **MIT** license.

------

by Ulysses (wdwxy12345{at}gmail.com)