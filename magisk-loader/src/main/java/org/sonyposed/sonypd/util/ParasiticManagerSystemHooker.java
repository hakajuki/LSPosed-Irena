package org.sonyposed.sonypd.util;

import android.annotation.SuppressLint;
import android.app.ProfilerInfo;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.util.Log;

import org.sonyposed.sonypd.hooker.HandleSystemServerProcessHooker;
import org.sonyposed.sonypd.impl.LSPosedHelper;
import org.sonyposed.sonypd.service.BridgeService;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.annotations.AfterInvocation;
import io.github.libxposed.api.annotations.XposedHooker;

public class ParasiticManagerSystemHooker implements HandleSystemServerProcessHooker.Callback {
    public static void start() {
        HandleSystemServerProcessHooker.callback = new ParasiticManagerSystemHooker();
    }

    /*@XposedHooker
    private static class Hooker2 implements XposedInterface.Hooker {
        @BeforeInvocation
        public static void beforeHookedMethod(XposedInterface.BeforeHookCallback callback) throws Throwable {
            Log.d("SonyPosed", "checking new activity");
            var self = callback.getThisObject();
            if (self == null) return;
            var request = XposedHelpers.getObjectField(self, "mRequest");
            Log.d("SonyPosed", "start activity intent=" + XposedHelpers.getObjectField(request, "intent") + " ai=" + XposedHelpers.getObjectField(request, "activityInfo"), new Throwable());
        }
    }*/

    @XposedHooker
    private static class Hooker implements XposedInterface.Hooker {
        @AfterInvocation
        public static void afterHookedMethod(XposedInterface.AfterHookCallback callback) throws Throwable {
            var intent = (Intent) callback.getArgs()[0];
            if (intent == null) return;
            if (!intent.hasCategory("org.sonyposed.manager.LAUNCH_MANAGER")) return;
            var aInfo = (ActivityInfo) callback.getResult();
            if (aInfo == null || !"com.android.shell".equals(aInfo.packageName)) return;
            // We shouldn't pollute system's object
            aInfo = new ActivityInfo(aInfo);
            // use a different process name
            aInfo.processName = "org.sonyposed.manager";
            // choose a theme that has transition animation
            aInfo.theme = android.R.style.Theme_DeviceDefault_Settings;
            // remove some annoying flags
            aInfo.flags = aInfo.flags & ~(ActivityInfo.FLAG_EXCLUDE_FROM_RECENTS | ActivityInfo.FLAG_FINISH_ON_CLOSE_SYSTEM_DIALOGS);
            BridgeService.getService().preStartManager();
            callback.setResult(aInfo);
        }
    }

    @SuppressLint("PrivateApi")
    @Override
    public void onSystemServerLoaded(ClassLoader classLoader) {
        try {
            Class<?> supervisorClass;
            try {
                // 15-12.0
                supervisorClass = Class.forName("com.android.server.wm.ActivityTaskSupervisor", false, classLoader);
            } catch (ClassNotFoundException ignore) {
                try {
                    // 11-10
                    supervisorClass = Class.forName("com.android.server.wm.ActivityStackSupervisor", false, classLoader);
                } catch (ClassNotFoundException ignore2) {
                    // 9-8.1
                    supervisorClass = Class.forName("com.android.server.am.ActivityStackSupervisor", false, classLoader);
                }
            }
            LSPosedHelper.hookMethod(Hooker.class, supervisorClass, "resolveActivity", Intent.class, ResolveInfo.class, int.class, ProfilerInfo.class);
            /*
            for (var method: Class.forName("com.android.server.wm.ActivityStarter", false, classLoader).getDeclaredMethods()) {
                if ("execute".equals(method.getName()))
                    HookBridge.deoptimizeMethod(method);
            }
            LSPosedHelper.hookAllMethods(Hooker2.class, Class.forName("com.android.server.wm.ActivityStarter", false, classLoader), "execute");*/
            Log.d("SonyPosed", "hooked activity starter");
        } catch (Throwable e) {
            Log.e("SonyPosed", "onSystemServerLoaded: ", e);
        }
    }
}
