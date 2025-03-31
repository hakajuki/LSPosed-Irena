package org.sonyposed.sonypd.service;

import org.sonyposed.sonypd.service.ILSPApplicationService;

interface ILSPosedService {
    ILSPApplicationService requestApplicationService(int uid, int pid, String processName, IBinder heartBeat);

    oneway void dispatchSystemServerContext(in IBinder activityThread, in IBinder activityToken, String api);

    boolean preStartManager();

    boolean setManagerEnabled(boolean enabled);
}
