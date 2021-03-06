package com.patloew.rxfit;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.BleDevice;

import java.util.concurrent.TimeUnit;

import rx.SingleSubscriber;

/* Copyright 2016 Patrick Löwenstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */
public class BleUnclaimDeviceSingle extends BaseSingle<Status> {

    private final BleDevice bleDevice;
    private final String deviceAddress;

    BleUnclaimDeviceSingle(RxFit rxFit, BleDevice bleDevice, String deviceAddress, Long timeout, TimeUnit timeUnit) {
        super(rxFit, timeout, timeUnit);
        this.bleDevice = bleDevice;
        this.deviceAddress = deviceAddress;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final SingleSubscriber<? super Status> subscriber) {
        ResultCallback<Status> resultCallback = new StatusResultCallBack(subscriber);

        if(bleDevice != null) {
            setupFitnessPendingResult(Fitness.BleApi.unclaimBleDevice(apiClient, bleDevice), resultCallback);
        } else {
            setupFitnessPendingResult(Fitness.BleApi.unclaimBleDevice(apiClient, deviceAddress), resultCallback);
        }
    }
}
