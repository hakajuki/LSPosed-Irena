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
 * Copyright (C) 2022 LSPosed Contributors
 */

package org.sonyposed.sonypd.core;

import android.os.IBinder;
import android.os.Process;

import org.sonyposed.sonypd.service.ILSPApplicationService;
import org.sonyposed.sonypd.util.ParasiticManagerHooker;
import org.sonyposed.sonypd.util.ParasiticManagerSystemHooker;
import org.sonyposed.sonypd.util.Utils;
import org.sonyposed.sonypd.BuildConfig;

public class Main {

    public static void forkCommon(boolean isSystem, String niceName, String appDir, IBinder binder) {
        if (isSystem) {
            ParasiticManagerSystemHooker.start();
        }
        Startup.initXposed(isSystem, niceName, appDir, ILSPApplicationService.Stub.asInterface(binder));
        if (niceName.equals(BuildConfig.DEFAULT_MANAGER_PACKAGE_NAME) && ParasiticManagerHooker.start()) {
            Utils.logI("Loaded manager, skipping next steps");
            return;
        }
        Utils.logI("Loading xposed for " + niceName + "/" + Process.myUid());
        Startup.bootstrapXposed();
    }
}
