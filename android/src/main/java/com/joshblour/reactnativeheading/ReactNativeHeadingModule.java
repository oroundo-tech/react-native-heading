package com.joshblour.reactnativeheading;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.GeomagneticField;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.location.LocationManager;


import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.joshblour.discovery.BLEUser;
import com.joshblour.discovery.Discovery;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import com.joshblour.reactnativeheading.OrientationManager;


public class ReactNativeHeadingModule extends ReactContextBaseJavaModule {

    private static Context mApplicationContext;
    private int heading = 0; // degree
    private int newHeading = 0; // degree
    private float mFilter = 5;
    private SensorManager mSensorManager;
    private OrientationManager mOrientationManager;
    private LocationManager mLocationManager;

    public ReactNativeHeadingModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mApplicationContext = reactContext.getApplicationContext();
    }

    @Override
    public String getName() {
        return "ReactNativeHeading";
    }

    private final OrientationManager.OnChangedListener mHeadingListener =
        new OrientationManager.OnChangedListener() {

        @Override
        public void onOrientationChanged(OrientationManager orientationManager) {
            int newHeading = (int) orientationManager.getHeading();

            //dont react to changes smaller than the filter value
            if (Math.abs(heading - newHeading) < mFilter) {
                return;
            }

            getReactApplicationContext()
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("headingUpdated", newHeading);

            heading = newHeading;
        }

        @Override
        public void onLocationChanged(OrientationManager orientationManager) {

        }

        @Override
        public void onAccuracyChanged(OrientationManager orientationManager) {

        }
    };


    @ReactMethod
    public void start(int filter, Promise promise) {

        if (mSensorManager == null) {
            mSensorManager = (SensorManager) mApplicationContext.getSystemService(Context.SENSOR_SERVICE);
        }

        if (mLocationManager == null) {
            mLocationManager = (LocationManager) mApplicationContext.getSystemService(Context.LOCATION_SERVICE);
        }

        mOrientationManager = new OrientationManager(mSensorManager, mLocationManager);

        mOrientationManager.addOnChangedListener(mHeadingListener);
        boolean isStarted = mOrientationManager.start();


        mFilter = filter;
        promise.resolve(isStarted);
    }

    @ReactMethod
    public void stop() {
        mOrientationManager.removeOnChangedListener(mHeadingListener);
        mOrientationManager.stop();
    }

    @ReactMethod
    public void isGyroscopeAvailable(Promise promise) {

        if (mSensorManager == null) {
            mSensorManager = (SensorManager) mApplicationContext.getSystemService(Context.SENSOR_SERVICE);
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
            promise.resolve(true);
        } else {
            promise.resolve(false);
        }
    }
}
