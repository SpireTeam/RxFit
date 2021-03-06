package com.patloew.rxfit;

import android.app.PendingIntent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataUpdateListenerRegistrationRequest;

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
public class HistoryRegisterDataUpdateListenerSingle extends BaseSingle<Status> {

    private final DataUpdateListenerRegistrationRequest request;

    HistoryRegisterDataUpdateListenerSingle(RxFit rxFit, PendingIntent pendingIntent, DataSource dataSource, DataType dataType, Long timeout, TimeUnit timeUnit) {
        super(rxFit, timeout, timeUnit);

        DataUpdateListenerRegistrationRequest.Builder builder = new DataUpdateListenerRegistrationRequest.Builder();
        builder.setPendingIntent(pendingIntent);
        if(dataSource != null) { builder.setDataSource(dataSource); }
        if(dataType != null) { builder.setDataType(dataType); }

        request = builder.build();
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final SingleSubscriber<? super Status> subscriber) {
        ResultCallback<Status> resultCallback = new StatusResultCallBack(subscriber);

        setupFitnessPendingResult(Fitness.HistoryApi.registerDataUpdateListener(apiClient, request), resultCallback);
    }
}
