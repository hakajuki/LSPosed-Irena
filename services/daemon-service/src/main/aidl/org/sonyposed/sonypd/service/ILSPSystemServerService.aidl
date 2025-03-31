package org.sonyposed.sonypd.service;

import org.sonyposed.sonypd.service.ILSPApplicationService;

interface ILSPSystemServerService {
    ILSPApplicationService requestApplicationService(int uid, int pid, String processName, IBinder heartBeat);
}
