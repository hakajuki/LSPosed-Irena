package org.sonyposed.sonypd.service;

interface IRemotePreferenceCallback {
    oneway void onUpdate(in Bundle map);
}
