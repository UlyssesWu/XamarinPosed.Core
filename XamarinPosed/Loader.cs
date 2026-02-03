using Android.Runtime;
using Android.Util;
using Android.Views;
using DE.Robv.Android.Xposed;
using DE.Robv.Android.Xposed.Callbacks;

namespace XamarinPosed
{
     public partial class Main
    {
        /// <summary>
        /// Write your logic here
        /// </summary>
        [Register("xamarin/posed/Main_Loader")]
        public class Loader : Java.Lang.Object, IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources
        {
            public string BaseApkPath;
            public string PackageName;
            public bool IsXamarinApp = false;

            public Loader()
            {
                Log.Info("XamarinPosed", "XamarinPosed Core Loader created.");
            }

            public Loader(IntPtr handle, JniHandleOwnership transfer) : base(handle, transfer) { }

            public Loader(string baseApkPath, string packageName)
            {
                BaseApkPath = baseApkPath;
                PackageName = packageName;
            }

            /// <summary>
            /// Write your logic here
            /// </summary>
            /// <param name="param"></param>
            public void HandleLoadPackage(XC_LoadPackage.LoadPackageParam? param)
            {
                DetectAndFixXamarinApp(param);
                Log.Info("XamarinPosed", "XamarinPosed HandleLoadPackage: " + param.PackageName);
                //This is a demo, remove it
                HookMyself(param);
            }

            /// <summary>
            /// Write your logic here
            /// </summary>
            /// <param name="param"></param>
            public void InitZygote(IXposedHookZygoteInit.StartupParam? param)
            {
                Log.Info("XamarinPosed", "XamarinPosed InitZygote: " + param?.ModulePath);
            }

            /// <summary>
            /// Write your logic here
            /// </summary>
            /// <param name="param"></param>
            public void HandleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam? param)
            {
                Log.Info("XamarinPosed", "XamarinPosed HandleInitPackageResources: " + param?.PackageName);
            }

            private bool DetectAndFixXamarinApp(XC_LoadPackage.LoadPackageParam param)
            {
                var nativeDir = param.AppInfo?.NativeLibraryDir;
                if (nativeDir == null)
                {
                    Log.Info("XamarinPosed", "native dir is null");
                    IsXamarinApp = false;
                    return false;
                }

                foreach (var file in Directory.EnumerateFiles(nativeDir))
                {
                    var lib = Path.GetFileName(file);
                    if (lib == "libxamarin-app.so" || lib == "libmono-native.so" || lib == "libmonodroid.so" || lib == "libxamarin-debug-app-helper.so")
                    {
                        Log.Info("XamarinPosed", "XamarinPosed found Xamarin App: " + param.PackageName);
                        //TODO:
                        //var unhook = XposedHelpers.FindAndHookMethod("android.content.Context", param.ClassLoader, "getClassLoader",
                        //    new Context_GetClassLoaderHook());

                        IsXamarinApp = true;
                        return true;
                    }
                }

                return false;
            }


            private static bool isThisAppHooked = false;

            private void HookMyself(XC_LoadPackage.LoadPackageParam param)
            {
                Log.Debug("XamarinPosed", $"LoadPackageParam: {param.PackageName}, {param.ProcessName}, {param.AppInfo}, {param.ClassLoader}");
                if (isThisAppHooked)
                {
                    return;
                }
                
                if (param.PackageName.Equals("com.companyname.NetAndroidApp", StringComparison.InvariantCultureIgnoreCase))
                {
                    Log.Info("XamarinPosed", "XamarinPosed HookMyself: " + param.PackageName);
                    var unhook = XposedHelpers.FindAndHookMethod("crc647b8f161eb0a155af.MainActivity_MySnackBarClickListener", param.ClassLoader, "onClick", "android.view.View", new SelfDemoHook());
                    isThisAppHooked = true;
                    Log.Info("XamarinPosed", "XamarinPosed HookMyself done");
                }
            }

        }
    }

    class SelfDemoHook : XC_MethodHook
    {
        protected override void BeforeHookedMethod(MethodHookParam? param)
        {
            var toast = Toast.MakeText(((View)param!.Args![0]).Context, "Greeting from C#:\nAll your base are belong to us!", ToastLength.Long);
            toast?.Show();

            base.BeforeHookedMethod(param);
        }
    }
}