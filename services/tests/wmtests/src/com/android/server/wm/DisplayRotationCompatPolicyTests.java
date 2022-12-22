/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.android.server.wm;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LOCKED;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

import static com.android.dx.mockito.inline.extended.ExtendedMockito.doAnswer;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.doReturn;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.spyOn;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.when;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;

import android.content.ComponentName;
import android.content.pm.ActivityInfo.ScreenOrientation;
import android.content.res.Configuration.Orientation;
import android.content.res.Resources;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.platform.test.annotations.Presubmit;
import android.view.Display;

import androidx.test.filters.SmallTest;

import com.android.internal.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Executor;

/**
 * Tests for {@link DisplayRotationCompatPolicy}.
 *
 * Build/Install/Run:
 *  atest WmTests:DisplayRotationCompatPolicyTests
 */
@SmallTest
@Presubmit
@RunWith(WindowTestRunner.class)
public final class DisplayRotationCompatPolicyTests extends WindowTestsBase {

    private static final String TEST_PACKAGE_1 = "com.test.package.one";
    private static final String TEST_PACKAGE_2 = "com.test.package.two";
    private static final String CAMERA_ID_1 = "camera-1";
    private static final String CAMERA_ID_2 = "camera-2";

    private CameraManager mMockCameraManager;
    private Handler mMockHandler;
    private Resources mResources;

    private DisplayRotationCompatPolicy mDisplayRotationCompatPolicy;
    private CameraManager.AvailabilityCallback mCameraAvailabilityCallback;

    private ActivityRecord mActivity;
    private Task mTask;

    @Before
    public void setUp() throws Exception {
        mResources = mContext.getResources();
        spyOn(mResources);
        when(mResources.getBoolean(R.bool.config_isWindowManagerCameraCompatTreatmentEnabled))
                .thenReturn(true);

        mMockCameraManager = mock(CameraManager.class);
        doAnswer(invocation -> {
            mCameraAvailabilityCallback = invocation.getArgument(1);
            return null;
        }).when(mMockCameraManager).registerAvailabilityCallback(
                any(Executor.class), any(CameraManager.AvailabilityCallback.class));

        spyOn(mContext);
        when(mContext.getSystemService(CameraManager.class)).thenReturn(mMockCameraManager);

        spyOn(mDisplayContent);

        mDisplayContent.setIgnoreOrientationRequest(true);

        mMockHandler = mock(Handler.class);

        when(mMockHandler.postDelayed(any(Runnable.class), anyLong())).thenAnswer(
                invocation -> {
                    ((Runnable) invocation.getArgument(0)).run();
                    return null;
                });
        mDisplayRotationCompatPolicy = new DisplayRotationCompatPolicy(
                mDisplayContent, mMockHandler);
    }

    @Test
    public void testGetOrientation_treatmentNotEnabled_returnUnspecified() {
        when(mResources.getBoolean(R.bool.config_isWindowManagerCameraCompatTreatmentEnabled))
                .thenReturn(false);

        mDisplayRotationCompatPolicy = new DisplayRotationCompatPolicy(mDisplayContent);
        configureActivity(SCREEN_ORIENTATION_PORTRAIT);
        mCameraAvailabilityCallback.onCameraOpened(CAMERA_ID_1, TEST_PACKAGE_1);

        assertEquals(mDisplayRotationCompatPolicy.getOrientation(),
                SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Test
    public void testGetOrientation_multiWindowMode_returnUnspecified() {
        configureActivity(SCREEN_ORIENTATION_PORTRAIT);
        final TestSplitOrganizer organizer = new TestSplitOrganizer(mAtm, mDisplayContent);
        mActivity.getTask().reparent(organizer.mPrimary, WindowContainer.POSITION_TOP,
                false /* moveParents */, "test" /* reason */);

        mCameraAvailabilityCallback.onCameraOpened(CAMERA_ID_1, TEST_PACKAGE_1);

        assertTrue(mActivity.inMultiWindowMode());
        assertEquals(mDisplayRotationCompatPolicy.getOrientation(),
                SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Test
    public void testGetOrientation_orientationUnspecified_returnUnspecified() {
        configureActivity(SCREEN_ORIENTATION_UNSPECIFIED);

        mCameraAvailabilityCallback.onCameraOpened(CAMERA_ID_1, TEST_PACKAGE_1);

        assertEquals(mDisplayRotationCompatPolicy.getOrientation(),
                SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Test
    public void testGetOrientation_orientationLocked_returnUnspecified() {
        configureActivity(SCREEN_ORIENTATION_LOCKED);

        mCameraAvailabilityCallback.onCameraOpened(CAMERA_ID_1, TEST_PACKAGE_1);

        assertEquals(mDisplayRotationCompatPolicy.getOrientation(),
                SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Test
    public void testGetOrientation_orientationNoSensor_returnUnspecified() {
        configureActivity(SCREEN_ORIENTATION_NOSENSOR);

        mCameraAvailabilityCallback.onCameraOpened(CAMERA_ID_1, TEST_PACKAGE_1);

        assertEquals(mDisplayRotationCompatPolicy.getOrientation(),
                SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Test
    public void testGetOrientation_ignoreOrientationRequestIsFalse_returnUnspecified() {
        mDisplayContent.setIgnoreOrientationRequest(false);

        configureActivity(SCREEN_ORIENTATION_PORTRAIT);
        mCameraAvailabilityCallback.onCameraOpened(CAMERA_ID_1, TEST_PACKAGE_1);

        assertEquals(mDisplayRotationCompatPolicy.getOrientation(),
                SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Test
    public void testGetOrientation_displayNotInternal_returnUnspecified() {
        Display display = mDisplayContent.getDisplay();
        spyOn(display);

        configureActivity(SCREEN_ORIENTATION_PORTRAIT);
        mCameraAvailabilityCallback.onCameraOpened(CAMERA_ID_1, TEST_PACKAGE_1);

        when(display.getType()).thenReturn(Display.TYPE_EXTERNAL);
        assertEquals(mDisplayRotationCompatPolicy.getOrientation(),
                SCREEN_ORIENTATION_UNSPECIFIED);

        when(display.getType()).thenReturn(Display.TYPE_WIFI);
        assertEquals(mDisplayRotationCompatPolicy.getOrientation(),
                SCREEN_ORIENTATION_UNSPECIFIED);

        when(display.getType()).thenReturn(Display.TYPE_OVERLAY);
        assertEquals(mDisplayRotationCompatPolicy.getOrientation(),
                SCREEN_ORIENTATION_UNSPECIFIED);

        when(display.getType()).thenReturn(Display.TYPE_VIRTUAL);
        assertEquals(mDisplayRotationCompatPolicy.getOrientation(),
                SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Test
    public void testGetOrientation_noCameraConnection_returnUnspecified() {
        configureActivity(SCREEN_ORIENTATION_PORTRAIT);

        assertEquals(mDisplayRotationCompatPolicy.getOrientation(),
                SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Test
    public void testGetOrientation_cameraReconnected_returnNotUnspecified() {
        configureActivity(SCREEN_ORIENTATION_PORTRAIT);

        mCameraAvailabilityCallback.onCameraOpened(CAMERA_ID_1, TEST_PACKAGE_1);
        mCameraAvailabilityCallback.onCameraClosed(CAMERA_ID_1);
        mCameraAvailabilityCallback.onCameraOpened(CAMERA_ID_1, TEST_PACKAGE_1);

        assertEquals(mDisplayRotationCompatPolicy.getOrientation(),
                SCREEN_ORIENTATION_PORTRAIT);
    }

    @Test
    public void testGetOrientation_reconnectedToDifferentCamera_returnNotUnspecified() {
        configureActivity(SCREEN_ORIENTATION_PORTRAIT);

        mCameraAvailabilityCallback.onCameraOpened(CAMERA_ID_1, TEST_PACKAGE_1);
        mCameraAvailabilityCallback.onCameraClosed(CAMERA_ID_1);
        mCameraAvailabilityCallback.onCameraOpened(CAMERA_ID_2, TEST_PACKAGE_1);

        assertEquals(mDisplayRotationCompatPolicy.getOrientation(),
                SCREEN_ORIENTATION_PORTRAIT);
    }

    @Test
    public void testGetOrientation_cameraConnectionClosed_returnUnspecified() {
        configureActivity(SCREEN_ORIENTATION_PORTRAIT);

        mCameraAvailabilityCallback.onCameraOpened(CAMERA_ID_1, TEST_PACKAGE_1);

        assertEquals(mDisplayRotationCompatPolicy.getOrientation(),
                SCREEN_ORIENTATION_PORTRAIT);

        mCameraAvailabilityCallback.onCameraClosed(CAMERA_ID_1);

        assertEquals(mDisplayRotationCompatPolicy.getOrientation(),
                SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Test
    public void testGetOrientation_cameraOpenedForDifferentPackage_returnUnspecified() {
        configureActivity(SCREEN_ORIENTATION_PORTRAIT);

        mCameraAvailabilityCallback.onCameraOpened(CAMERA_ID_1, TEST_PACKAGE_2);

        assertEquals(mDisplayRotationCompatPolicy.getOrientation(),
                SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Test
    public void testGetOrientation_portraitActivity_portraitNaturalOrientation_returnPortrait() {
        testGetOrientationForActivityAndNaturalOrientations(
                /* activityOrientation */ SCREEN_ORIENTATION_PORTRAIT,
                /* naturalOrientation */ ORIENTATION_PORTRAIT,
                /* expectedOrientation */ SCREEN_ORIENTATION_PORTRAIT);
    }

    @Test
    public void testGetOrientation_portraitActivity_landscapeNaturalOrientation_returnLandscape() {
        testGetOrientationForActivityAndNaturalOrientations(
                /* activityOrientation */ SCREEN_ORIENTATION_PORTRAIT,
                /* naturalOrientation */ ORIENTATION_LANDSCAPE,
                /* expectedOrientation */ SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Test
    public void testGetOrientation_landscapeActivity_portraitNaturalOrientation_returnLandscape() {
        testGetOrientationForActivityAndNaturalOrientations(
                /* activityOrientation */ SCREEN_ORIENTATION_LANDSCAPE,
                /* naturalOrientation */ ORIENTATION_PORTRAIT,
                /* expectedOrientation */ SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Test
    public void testGetOrientation_landscapeActivity_landscapeNaturalOrientation_returnPortrait() {
        testGetOrientationForActivityAndNaturalOrientations(
                /* activityOrientation */ SCREEN_ORIENTATION_LANDSCAPE,
                /* naturalOrientation */ ORIENTATION_LANDSCAPE,
                /* expectedOrientation */ SCREEN_ORIENTATION_PORTRAIT);
    }

    private void testGetOrientationForActivityAndNaturalOrientations(
            @ScreenOrientation int activityOrientation,
            @Orientation int naturalOrientation,
            @ScreenOrientation int expectedOrientation) {
        configureActivityAndDisplay(activityOrientation, naturalOrientation);

        mCameraAvailabilityCallback.onCameraOpened(CAMERA_ID_1, TEST_PACKAGE_1);

        assertEquals(mDisplayRotationCompatPolicy.getOrientation(),
                expectedOrientation);
    }

    private void configureActivity(@ScreenOrientation int activityOrientation) {
        configureActivityAndDisplay(activityOrientation, ORIENTATION_PORTRAIT);
    }

    private void configureActivityAndDisplay(@ScreenOrientation int activityOrientation,
            @Orientation int naturalOrientation) {

        mTask = new TaskBuilder(mSupervisor)
                .setDisplay(mDisplayContent)
                .build();

        mActivity = new ActivityBuilder(mAtm)
                .setComponent(new ComponentName(TEST_PACKAGE_1, ".TestActivity"))
                .setScreenOrientation(activityOrientation)
                .setTask(mTask)
                .build();

        doReturn(mActivity).when(mDisplayContent).topRunningActivity(anyBoolean());
        doReturn(naturalOrientation).when(mDisplayContent).getNaturalOrientation();
    }
}
