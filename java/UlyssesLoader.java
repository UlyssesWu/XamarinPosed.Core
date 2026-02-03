//by Ulysses, wdwxy12345@gmail.com
package xamarin.posed;

import java.util.Locale;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import mono.android.BuildConfig;
import mono.android.DebugRuntime;
import mono.android.Runtime;
import net.dot.android.ApplicationRegistration;
import mono.MonoPackageManager_Resources;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import java.time.OffsetDateTime;
import java.util.Calendar;

public class XamarinPosedLoader
	//extends java.lang.Object
	implements
		//mono.android.IGCUserPeer,
		de.robv.android.xposed.IXposedHookLoadPackage,
		de.robv.android.xposed.IXposedHookZygoteInit
		//de.robv.android.xposed.IXposedHookInitPackageResources
{
/** @hide */
	public xamarin.posed.Main_Loader _loader;
	public boolean isInited = false;
	private boolean isZygoteInited = false;
	private String modulePath;
	static {}

	public XamarinPosedLoader ()
	{
		super ();
	}

	public void handleLoadPackage (de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam p0)
	{
		if (!isInited)
		{
			if (shouldSkipProcess(p0.processName))
			{
				Log.i("XamarinPosed", "Skip runtime init in process: " + p0.processName);
				return;
			}
			ensureRuntimeInitialized();
		}

		if (isInited && _loader != null)
		{
			_loader.handleLoadPackage(p0);
		}
	}

	public void initZygote (de.robv.android.xposed.IXposedHookZygoteInit.StartupParam p0)
	{
		modulePath = p0.modulePath;
		Log.i("XamarinPosed", "InitZygote modulePath: " + modulePath);
		//ensureRuntimeInitialized();
	}

	private void ensureRuntimeInitialized()
	{
		if (isInited)
		{
			return;
		}
		if (modulePath == null || modulePath.isEmpty())
		{
			Log.e("XamarinPosed", "Module path is not set, skip runtime init.");
			return;
		}

		if (!isInited)
		{
			String modulePath = this.modulePath; // /data/user/0/io.va.exposed/virtual/data/app/{package}/base.apk
			Locale locale = Locale.getDefault();
			String localeStr = locale.getLanguage() + "-" + locale.getCountry();

			Path currentModulePath = Paths.get(modulePath);
			String parent = currentModulePath.getParent().getFileName().toString();
			String packageName = parent;
			int subPos = parent.lastIndexOf("-");
			if (subPos > 0)
			{
				packageName = parent.substring(0, subPos);
			}
			Log.i("XamarinPosed", "packageName: " + packageName);

			File externalStorageDirectory = Environment.getExternalStorageDirectory();
			File filesDirFile = new File(externalStorageDirectory, "Android/data/" + packageName + "/files");
			File cachesDirFile = new File(externalStorageDirectory, "Android/data/" + packageName + "/files/cache");
			cachesDirFile.mkdirs();
			String filesDir = filesDirFile.getAbsolutePath();
			Log.i("XamarinPosed", "filesDir: " + filesDir);
			//String filesDir = context.getFilesDir().getAbsolutePath(); // /data/user/0/io.va.xposed/virtual/data/user/0/{package}/

			String cacheDir = cachesDirFile.getAbsolutePath();
			//String cacheDir = context.getCacheDir().getAbsolutePath(); // filesDir + "cache"
			String abi = Build.SUPPORTED_ABIS != null && Build.SUPPORTED_ABIS.length > 0
				? Build.SUPPORTED_ABIS[0]
				: "arm64-v8a";
			String nativeLibraryPath = modulePath + "!/lib/" + abi;

			ClassLoader classLoader = this.getClass().getClassLoader();
			//TODO: hook context.getClassLoader() and replaced to this classLoader
			//ClassLoader classLoader = de.robv.android.xposed.XposedBridge.BOOTCLASSLOADER;
			String externalOverrridePath = new File(externalStorageDirectory, "Android/data/" + packageName + "/files/.__override__").getAbsolutePath();
			String externalOverrridePathLegacy = new File(externalStorageDirectory, "../legacy/Android/data/" + packageName + "/files/.__override__").getAbsolutePath();

			String nativeLibraryPath2 = nativeLibraryPath; // apk!/lib/<abi>

			String[] sourceDirs = new String[1]; //append ApplicationInfo.splitPublicSourceDirs if needed
			sourceDirs[0] = modulePath;

			String[] initParams = {filesDir, cacheDir, nativeLibraryPath};
			//String[] externalOverrrideParams = {externalOverrridePath, externalOverrridePathLegacy}; //deprecated

			Log.i("XamarinPosed", "nativeLibraryPath: " + nativeLibraryPath2);
			String nativeLibraryPath3 = nativeLibraryPath + "/";

			int currentTime;
			if (Build.VERSION.SDK_INT >= 26) 
			{
                currentTime = OffsetDateTime.now().getOffset().getTotalSeconds();
            }
			else 
			{
                currentTime = (Calendar.getInstance().get(15) + Calendar.getInstance().get(16)) / 1000;
            }
			
			boolean isSplitApk = false;
			try
			{
				if (BuildConfig.Debug) 
				{
					System.load(nativeLibraryPath3 + "libxamarin-debug-app-helper.so");
					//DebugRuntime.init(sourceDirs, nativeLibraryPath2, initParams, externalOverrrideParams);
					DebugRuntime.init(sourceDirs, nativeLibraryPath2, initParams, isSplitApk); //the last is split apk, we don't support it yet'
				} 
				else 
				{
					System.load(nativeLibraryPath3 + "libmonosgen-2.0.so");
				}
			}
			catch (UnsatisfiedLinkError e) 
			{
				Log.e("XamarinPosed", "Failed to load mono lib, could be architecture mismatch (64bit module vs 32bit app or vice versa)", e);
				isInited = false;
				return;
			}

			System.load(nativeLibraryPath3 + "libxamarin-app.so");
			if (!BuildConfig.DotNetRuntime) 
			{
				try 
				{
					System.load(nativeLibraryPath3 + "libmono-native.so");
				} 
				catch (UnsatisfiedLinkError e) 
				{
					Log.i("monodroid", "Failed to preload libmono-native.so (may not exist), ignoring", e);
				}
			}

			System.load(nativeLibraryPath3 + "libmonodroid.so");
			System.load(nativeLibraryPath3 + "libSystem.Security.Cryptography.Native.Android.so");
			Log.i("XamarinPosed", "load lib done");
			//Runtime.initInternal(localeStr, sourceDirs, nativeLibraryPath2, initParams, classLoader, externalOverrrideParams, MonoPackageManager_Resources.Assemblies, Build.VERSION.SDK_INT, isEmulator());
			Runtime.initInternal(localeStr, sourceDirs, nativeLibraryPath2, initParams, currentTime, classLoader, MonoPackageManager_Resources.Assemblies, isEmulator(), isSplitApk);
			ApplicationRegistration.registerApplications();
			Log.i("XamarinPosed", "init internal done");
			
			// /data/data/com.my.app/files
			_loader = new xamarin.posed.Main_Loader();
			//_loader = new xamarin.posed.Main_Loader(modulePath, packageName);
			isInited = true;
			simulateInitZygote();
		}
	}

	public void handleInitPackageResources (de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam p0)
	{
		if (isInited && _loader != null)
		{
			_loader.handleInitPackageResources(p0);
		}
	}
	
    static boolean isEmulator() {
        String str = Build.HARDWARE;
        return str.contains("ranchu") || str.contains("goldfish");
    }

	private static boolean shouldSkipProcess(String processName)
	{
		if (processName == null || processName.isEmpty())
		{
			return false;
		}
		return processName.startsWith("usap") || processName.startsWith("zygote");
	}

	private void simulateInitZygote()
	{
		if (isZygoteInited || _loader == null)
		{
			return;
		}
		try
		{
			de.robv.android.xposed.IXposedHookZygoteInit.StartupParam param =
				new de.robv.android.xposed.IXposedHookZygoteInit.StartupParam();
			param.modulePath = modulePath;
			param.startsSystemServer = false;
			_loader.initZygote(param);
			isZygoteInited = true;
			Log.i("XamarinPosed", "Simulated initZygote invoked.");
		}
		catch (Throwable t)
		{
			Log.e("XamarinPosed", "Simulated initZygote failed.", t);
		}
	}
}
