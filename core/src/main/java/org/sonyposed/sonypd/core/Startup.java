/*
 * This file is part of LSPosed.
 *
 * LSPosed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LSPosed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LSPosed.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 EdXposed Contributors
 * Copyright (C) 2021 - 2022 LSPosed Contributors
 */

package org.sonyposed.sonypd.core;

import android.app.ActivityThread;
import android.app.LoadedApk;
import android.content.pm.ApplicationInfo;
import android.content.res.CompatibilityInfo;

import com.android.internal.os.ZygoteInit;

import org.sonyposed.sonypd.deopt.PrebuiltMethodsDeopter;
import org.sonyposed.sonypd.hooker.AttachHooker;
import org.sonyposed.sonypd.hooker.CrashDumpHooker;
import org.sonyposed.sonypd.hooker.HandleSystemServerProcessHooker;
import org.sonyposed.sonypd.hooker.LoadedApkCtorHooker;
import org.sonyposed.sonypd.hooker.LoadedApkCreateCLHooker;
import org.sonyposed.sonypd.hooker.OpenDexFileHooker;
import org.sonyposed.sonypd.impl.LSPosedContext;
import org.sonyposed.sonypd.impl.LSPosedHelper;
import org.sonyposed.sonypd.service.ILSPApplicationService;
import org.sonyposed.sonypd.util.Utils;

import java.util.List;

import dalvik.system.DexFile;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedInit;

public class Startup {
    private static void startBootstrapHook(boolean isSystem) {
        Utils.logD("startBootstrapHook starts: isSystem = " + isSystem);
        LSPosedHelper.hookMethod(CrashDumpHooker.class, Thread.class, "dispatchUncaughtException", Throwable.class);
        if (isSystem) {
            LSPosedHelper.hookAllMethods(HandleSystemServerProcessHooker.class, ZygoteInit.class, "handleSystemServerProcess");
        } else {
            LSPosedHelper.hookAllMethods(OpenDexFileHooker.class, DexFile.class, "openDexFile");
            LSPosedHelper.hookAllMethods(OpenDexFileHooker.class, DexFile.class, "openInMemoryDexFile");
            LSPosedHelper.hookAllMethods(OpenDexFileHooker.class, DexFile.class, "openInMemoryDexFiles");
        }
        LSPosedHelper.hookConstructor(LoadedApkCtorHooker.class, LoadedApk.class,
                ActivityThread.class, ApplicationInfo.class, CompatibilityInfo.class,
                ClassLoader.class, boolean.class, boolean.class, boolean.class);
        LSPosedHelper.hookMethod(LoadedApkCreateCLHooker.class, LoadedApk.class, "createOrUpdateClassLoaderLocked", List.class);
        LSPosedHelper.hookAllMethods(AttachHooker.class, ActivityThread.class, "attach");
    }

    public static void bootstrapXposed() {
        // Initialize the Xposed framework
        try {
            startBootstrapHook(XposedInit.startsSystemServer);
            XposedInit.loadLegacyModules();
        } catch (Throwable t) {
            Utils.logE("error during Xposed initialization", t);
        }
    }

    public static void initXposed(boolean isSystem, String processName, String appDir, ILSPApplicationService service) {
        // init logger
        ApplicationServiceClient.Init(service, processName);
        XposedBridge.initXResources();
        XposedInit.startsSystemServer = isSystem;
        LSPosedContext.isSystemServer = isSystem;
        LSPosedContext.appDir = appDir;
        LSPosedContext.processName = processName;
        PrebuiltMethodsDeopter.deoptBootMethods(); // do it once for secondary zygote
    }
}
