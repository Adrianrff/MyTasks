package com.adrapps.mytasks.domain;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class LocalList implements Serializable, Comparable {

    private String title, id;
    private int intId;



    private int syncStatus;
    private long serverUpdated, localUpdated;


    public LocalList(){

    }

    //GETTERS

    public int getSyncStatus() {
        return syncStatus;
    }
    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public int getIntId() {
        return intId;
    }

    public long getServerUpdated() {
        return serverUpdated;
    }

    public long getLocalUpdated() {
        return localUpdated;
    }



    //SETTERS
    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIntId(int intId) {
        this.intId = intId;
    }

    public void setServerUpdated(long server_updated) {
        this.serverUpdated = server_updated;
    }

    public void setLocalUpdated(long local_updated) {
        this.localUpdated = local_updated;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        return 0;
    }
}
