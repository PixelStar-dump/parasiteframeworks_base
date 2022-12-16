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

package android.media.projection;

import static android.view.Display.DEFAULT_DISPLAY;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.annotation.IntDef;
import android.annotation.IntRange;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.Parcelable;

import com.android.internal.util.AnnotationValidations;
import com.android.internal.util.DataClass;

import java.lang.annotation.Retention;

/**
 * Configure the {@link MediaProjection} session requested from
 * {@link MediaProjectionManager#createScreenCaptureIntent(MediaProjectionConfig)}.
 */
@DataClass(
        genEqualsHashCode = true,
        genAidl = true,
        genSetters = false,
        genConstructor = false,
        genBuilder = false,
        genToString = false,
        genHiddenConstDefs = true,
        genHiddenGetters = true,
        genConstDefs = false
)
public final class MediaProjectionConfig implements Parcelable {

    /**
     * The user, rather than the host app, determines which region of the display to capture.
     * @hide
     */
    public static final int CAPTURE_REGION_USER_CHOICE = 0;

    /**
     * The host app specifies a particular display to capture.
     * @hide
     */
    public static final int CAPTURE_REGION_FIXED_DISPLAY = 1;

    /** @hide */
    @IntDef(prefix = "CAPTURE_REGION_", value = {
            CAPTURE_REGION_USER_CHOICE,
            CAPTURE_REGION_FIXED_DISPLAY
    })
    @Retention(SOURCE)
    public @interface CaptureRegion {
    }

    /**
     * The particular display to capture. Only used when {@link #getRegionToCapture()} is
     * {@link #CAPTURE_REGION_FIXED_DISPLAY}; ignored otherwise.
     *
     * Only supports values of {@link android.view.Display#DEFAULT_DISPLAY}.
     */
    @IntRange(from = DEFAULT_DISPLAY, to = DEFAULT_DISPLAY)
    private int mDisplayToCapture;

    /**
     * The region to capture. Defaults to the user's choice.
     */
    @CaptureRegion
    private int mRegionToCapture = CAPTURE_REGION_USER_CHOICE;

    /**
     * Default instance, with region set to the user's choice.
     */
    private MediaProjectionConfig() {
    }

    /**
     * Customized instance, with region set to the provided value.
     */
    private MediaProjectionConfig(@CaptureRegion int captureRegion) {
        mRegionToCapture = captureRegion;
    }

    /**
     * Returns an instance which restricts the user to capturing a particular display.
     *
     * @param displayId The id of the display to capture. Only supports values of
     *                  {@link android.view.Display#DEFAULT_DISPLAY}.
     * @throws IllegalArgumentException If the given {@code displayId} is outside the range of
     * supported values.
     */
    @NonNull
    public static MediaProjectionConfig createConfigForDisplay(
            @IntRange(from = DEFAULT_DISPLAY, to = DEFAULT_DISPLAY) int displayId) {
        if (displayId != DEFAULT_DISPLAY) {
            throw new IllegalArgumentException(
                    "A config for capturing the non-default display is not supported; requested "
                            + "display id "
                            + displayId);
        }
        MediaProjectionConfig config = new MediaProjectionConfig(CAPTURE_REGION_FIXED_DISPLAY);
        config.mDisplayToCapture = displayId;
        return config;
    }

    /**
     * Returns an instance which allows the user to decide which region is captured. The consent
     * dialog presents the user with all possible options. If the user selects display capture,
     * then only the {@link android.view.Display#DEFAULT_DISPLAY} is supported.
     *
     * <p>
     * When passed in to
     * {@link MediaProjectionManager#createScreenCaptureIntent(MediaProjectionConfig)}, the consent
     * dialog shown to the user will be the same as if just
     * {@link MediaProjectionManager#createScreenCaptureIntent()} was invoked.
     * </p>
     */
    @NonNull
    public static MediaProjectionConfig createConfigForUserChoice() {
        return new MediaProjectionConfig(CAPTURE_REGION_USER_CHOICE);
    }

    /**
     * Returns string representation of the captured region.
     */
    @NonNull
    private static String captureRegionToString(int value) {
        switch (value) {
            case CAPTURE_REGION_USER_CHOICE:
                return "CAPTURE_REGION_USERS_CHOICE";
            case CAPTURE_REGION_FIXED_DISPLAY:
                return "CAPTURE_REGION_GIVEN_DISPLAY";
            default:
                return Integer.toHexString(value);
        }
    }

    @Override
    public String toString() {
        return "MediaProjectionConfig { "
                + "displayToCapture = " + mDisplayToCapture + ", "
                + "regionToCapture = " + captureRegionToString(mRegionToCapture)
                + " }";
    }





    // Code below generated by codegen v1.0.23.
    //
    // DO NOT MODIFY!
    // CHECKSTYLE:OFF Generated code
    //
    // To regenerate run:
    // $ codegen $ANDROID_BUILD_TOP/frameworks/base/media/java/android/media/projection/MediaProjectionConfig.java
    //
    // To exclude the generated code from IntelliJ auto-formatting enable (one-time):
    //   Settings > Editor > Code Style > Formatter Control
    //@formatter:off


    /**
     * The particular display to capture. Only used when {@link #getRegionToCapture()} is
     * {@link #CAPTURE_REGION_FIXED_DISPLAY}; ignored otherwise.
     *
     * Only supports values of {@link android.view.Display#DEFAULT_DISPLAY}.
     *
     * @hide
     */
    @DataClass.Generated.Member
    public @IntRange(from = DEFAULT_DISPLAY, to = DEFAULT_DISPLAY) int getDisplayToCapture() {
        return mDisplayToCapture;
    }

    /**
     * The region to capture. Defaults to the user's choice.
     *
     * @hide
     */
    @DataClass.Generated.Member
    public @CaptureRegion int getRegionToCapture() {
        return mRegionToCapture;
    }

    @Override
    @DataClass.Generated.Member
    public boolean equals(@Nullable Object o) {
        // You can override field equality logic by defining either of the methods like:
        // boolean fieldNameEquals(MediaProjectionConfig other) { ... }
        // boolean fieldNameEquals(FieldType otherValue) { ... }

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        @SuppressWarnings("unchecked")
        MediaProjectionConfig that = (MediaProjectionConfig) o;
        //noinspection PointlessBooleanExpression
        return true
                && mDisplayToCapture == that.mDisplayToCapture
                && mRegionToCapture == that.mRegionToCapture;
    }

    @Override
    @DataClass.Generated.Member
    public int hashCode() {
        // You can override field hashCode logic by defining methods like:
        // int fieldNameHashCode() { ... }

        int _hash = 1;
        _hash = 31 * _hash + mDisplayToCapture;
        _hash = 31 * _hash + mRegionToCapture;
        return _hash;
    }

    @Override
    @DataClass.Generated.Member
    public void writeToParcel(@NonNull android.os.Parcel dest, int flags) {
        // You can override field parcelling by defining methods like:
        // void parcelFieldName(Parcel dest, int flags) { ... }

        dest.writeInt(mDisplayToCapture);
        dest.writeInt(mRegionToCapture);
    }

    @Override
    @DataClass.Generated.Member
    public int describeContents() { return 0; }

    /** @hide */
    @SuppressWarnings({"unchecked", "RedundantCast"})
    @DataClass.Generated.Member
    /* package-private */ MediaProjectionConfig(@NonNull android.os.Parcel in) {
        // You can override field unparcelling by defining methods like:
        // static FieldType unparcelFieldName(Parcel in) { ... }

        int displayToCapture = in.readInt();
        int regionToCapture = in.readInt();

        this.mDisplayToCapture = displayToCapture;
        AnnotationValidations.validate(
                IntRange.class, null, mDisplayToCapture,
                "from", DEFAULT_DISPLAY,
                "to", DEFAULT_DISPLAY);
        this.mRegionToCapture = regionToCapture;
        AnnotationValidations.validate(
                CaptureRegion.class, null, mRegionToCapture);

        // onConstructed(); // You can define this method to get a callback
    }

    @DataClass.Generated.Member
    public static final @NonNull Parcelable.Creator<MediaProjectionConfig> CREATOR
            = new Parcelable.Creator<MediaProjectionConfig>() {
        @Override
        public MediaProjectionConfig[] newArray(int size) {
            return new MediaProjectionConfig[size];
        }

        @Override
        public MediaProjectionConfig createFromParcel(@NonNull android.os.Parcel in) {
            return new MediaProjectionConfig(in);
        }
    };

    @DataClass.Generated(
            time = 1671030124845L,
            codegenVersion = "1.0.23",
            sourceFile = "frameworks/base/media/java/android/media/projection/MediaProjectionConfig.java",
            inputSignatures = "public static final  int CAPTURE_REGION_USER_CHOICE\npublic static final  int CAPTURE_REGION_FIXED_DISPLAY\nprivate @android.annotation.IntRange int mDisplayToCapture\nprivate @android.media.projection.MediaProjectionConfig.CaptureRegion int mRegionToCapture\npublic static @android.annotation.NonNull android.media.projection.MediaProjectionConfig createConfigForDisplay(int)\npublic static @android.annotation.NonNull android.media.projection.MediaProjectionConfig createConfigForUserChoice()\nprivate static @android.annotation.NonNull java.lang.String captureRegionToString(int)\npublic @java.lang.Override java.lang.String toString()\nclass MediaProjectionConfig extends java.lang.Object implements [android.os.Parcelable]\n@com.android.internal.util.DataClass(genEqualsHashCode=true, genAidl=true, genSetters=false, genConstructor=false, genBuilder=false, genToString=false, genHiddenConstDefs=true, genHiddenGetters=true, genConstDefs=false)")
    @Deprecated
    private void __metadata() {}


    //@formatter:on
    // End of generated code

}
