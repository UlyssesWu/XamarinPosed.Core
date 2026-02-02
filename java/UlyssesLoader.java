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
	private boolean pendingInit = false;
	private String cachedPackageName;
	private String cachedLocaleStr;
	private String cachedNativeLibraryPath;
	private String cachedNativeLibraryPath2;
	private String cachedNativeLibraryPath3;
	private String[] cachedSourceDirs;
	private String[] cachedInitParams;
	private int cachedCurrentTime;
	private boolean cachedIsSplitApk = false;
	private de.robv.android.xposed.IXposedHookZygoteInit.StartupParam startupParam;
	static {}

	public XamarinPosedLoader ()
	{
		super ();
	}

	private void cacheInitFromLoadPackage(de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam p0)
	{
		if (p0 == null || p0.appInfo == null)
		{
			Log.w("XamarinPosed", "LoadPackageParam or appInfo is null");
			return;
		}

		cachedPackageName = p0.packageName;
		if (cachedLocaleStr == null)
		{
			Locale locale = Locale.getDefault();
			cachedLocaleStr = locale.getLanguage() + "-" + locale.getCountry();
		}

		File externalStorageDirectory = Environment.getExternalStorageDirectory();
		File filesDirFile = new File(externalStorageDirectory, "Android/data/" + cachedPackageName + "/files");
		File cachesDirFile = new File(externalStorageDirectory, "Android/data/" + cachedPackageName + "/files/cache");
		cachesDirFile.mkdirs();
		String filesDir = filesDirFile.getAbsolutePath();
		String cacheDir = cachesDirFile.getAbsolutePath();

		String nativeLibraryPath = p0.appInfo.nativeLibraryDir;
		String nativeLibraryPath2 = p0.appInfo.nativeLibraryDir;
		String nativeLibraryPath3 = nativeLibraryPath2 + "/";

		String[] splitSourceDirs = p0.appInfo.splitSourceDirs;
		String[] sourceDirs;
		if (splitSourceDirs != null && splitSourceDirs.length > 0)
		{
			sourceDirs = new String[splitSourceDirs.length + 1];
			sourceDirs[0] = p0.appInfo.sourceDir;
			System.arraycopy(splitSourceDirs, 0, sourceDirs, 1, splitSourceDirs.length);
		}
		else
		{
			sourceDirs = new String[] { p0.appInfo.sourceDir };
		}

		int currentTime;
		if (Build.VERSION.SDK_INT >= 26) 
		{
			currentTime = OffsetDateTime.now().getOffset().getTotalSeconds();
		}
		else 
		{
			currentTime = (Calendar.getInstance().get(15) + Calendar.getInstance().get(16)) / 1000;
		}

		cachedNativeLibraryPath = nativeLibraryPath;
		cachedNativeLibraryPath2 = nativeLibraryPath2;
		cachedNativeLibraryPath3 = nativeLibraryPath3;
		cachedSourceDirs = sourceDirs;
		cachedInitParams = new String[] { filesDir, cacheDir, nativeLibraryPath };
		cachedCurrentTime = currentTime;
		cachedIsSplitApk = splitSourceDirs != null && splitSourceDirs.length > 0;
		pendingInit = true;
	}

	private void ensureRuntimeInitialized(ClassLoader classLoader)
	{
		if (isInited)
		{
			return;
		}
		if (classLoader == null)
		{
			Log.w("XamarinPosed", "ClassLoader is null, skip init");
			return;
		}
		if (cachedSourceDirs == null || cachedInitParams == null || cachedNativeLibraryPath3 == null || cachedLocaleStr == null)
		{
			Log.w("XamarinPosed", "Missing cached init data, skip init");
			return;
		}

		try
		{
			if (BuildConfig.Debug) 
			{
				System.load(cachedNativeLibraryPath3 + "libxamarin-debug-app-helper.so");
				DebugRuntime.init(cachedSourceDirs, cachedNativeLibraryPath2, cachedInitParams, cachedIsSplitApk); //the last is split apk, we don't support it yet'
			} 
			else 
			{
				System.load(cachedNativeLibraryPath3 + "libmonosgen-2.0.so");
			}
		}
		catch (UnsatisfiedLinkError e) 
		{
			Log.e("XamarinPosed", "Failed to load mono lib, could be architecture mismatch (64bit module vs 32bit app or vice versa)", e);
			isInited = false;
			return;
		}

		System.load(cachedNativeLibraryPath3 + "libxamarin-app.so");
		if (!BuildConfig.DotNetRuntime) 
		{
			try 
			{
				System.load(cachedNativeLibraryPath3 + "libmono-native.so");
			} 
			catch (UnsatisfiedLinkError e) 
			{
				Log.i("monodroid", "Failed to preload libmono-native.so (may not exist), ignoring", e);
			}
		}

		System.load(cachedNativeLibraryPath3 + "libmonodroid.so");
		Log.i("XamarinPosed", "load lib done");
		Runtime.initInternal(cachedLocaleStr, cachedSourceDirs, cachedNativeLibraryPath2, cachedInitParams, cachedCurrentTime, classLoader, MonoPackageManager_Resources.Assemblies, isEmulator(), cachedIsSplitApk);
		ApplicationRegistration.registerApplications();
		Log.i("XamarinPosed", "init internal done");

		_loader = new xamarin.posed.Main_Loader();
		isInited = true;
		pendingInit = false;

		if (startupParam != null && _loader != null)
		{
			_loader.initZygote(startupParam);
		}
	}

	public void handleLoadPackage (de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam p0)
	{
		if (!isInited)
		{
			cacheInitFromLoadPackage(p0);
			if (pendingInit)
			{
				ensureRuntimeInitialized(p0.classLoader);
			}
		}
		if (isInited && _loader != null)
		{
			_loader.handleLoadPackage(p0);
		}
	}

	public void initZygote (de.robv.android.xposed.IXposedHookZygoteInit.StartupParam p0)
	{
		startupParam = p0;
		if (!isInited)
		{
			if (cachedLocaleStr == null)
			{
				Locale locale = Locale.getDefault();
				cachedLocaleStr = locale.getLanguage() + "-" + locale.getCountry();
			}
		}

		if (isInited && _loader != null)
		{
			_loader.initZygote (p0);
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
}
