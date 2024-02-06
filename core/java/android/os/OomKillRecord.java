/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.os;


/**
 * Expected data to get back from the OOM event's file.
 * Note that this should be equivalent to the struct <b>OomKill</b> inside
 * <pre>
 * system/memory/libmeminfo/libmemevents/include/memevents.h
 * </pre>
 *
 * @hide
 */
public final class OomKillRecord {
    private long mTimeStampInMillis;
    private int mPid;
    private int mUid;
    private String mProcessName;
    private short mOomScoreAdj;

    public OomKillRecord(long timeStampInMillis, int pid, int uid,
                            String processName, short oomScoreAdj) {
        this.mTimeStampInMillis = timeStampInMillis;
        this.mPid = pid;
        this.mUid = uid;
        this.mProcessName = processName;
        this.mOomScoreAdj = oomScoreAdj;
    }

    public long getTimestampMilli() {
        return mTimeStampInMillis;
    }

    public int getPid() {
        return mPid;
    }

    public int getUid() {
        return mUid;
    }

    public String getProcessName() {
        return mProcessName;
    }

    public short getOomScoreAdj() {
        return mOomScoreAdj;
    }
}
