package org.sonyposed.sonypd.models;
import org.sonyposed.sonypd.models.PreLoadedApk;
import org.sonyposed.sonypd.service.ILSPInjectedModuleService;

parcelable Module {
    String packageName;
    int appId;
    String apkPath;
    PreLoadedApk file;
    ApplicationInfo applicationInfo;
    ILSPInjectedModuleService service;
}
