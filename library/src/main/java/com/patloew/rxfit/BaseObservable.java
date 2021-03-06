package com.patloew.rxfit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/* Copyright (C) 2015 Michał Charmas (http://blog.charmas.pl)
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
 *
 * ---------------
 *
 * FILE MODIFIED by Patrick Löwenstein, 2016
 *
 */
public abstract class BaseObservable<T> extends BaseRx<T> implements Observable.OnSubscribe<T> {
    private final boolean handleResolution;

    private final Map<GoogleApiClient, Subscriber<? super T>> subscriptionInfoMap = new ConcurrentHashMap<>();

    protected BaseObservable(@NonNull RxFit rxFit, Long timeout, TimeUnit timeUnit) {
        super(rxFit, timeout, timeUnit);
        handleResolution = true;
    }

    protected BaseObservable(@NonNull RxFit rxFit, Long timeout, TimeUnit timeUnit, boolean handleResolution) {
        super(rxFit, timeout, timeUnit);
        this.handleResolution = handleResolution;
    }

    protected BaseObservable(@NonNull Context ctx, @NonNull Api<? extends Api.ApiOptions.NotRequiredOptions>[] services, Scope[] scopes) {
        super(ctx, services, scopes);
        handleResolution = false;
    }

    @Override
    public final void call(Subscriber<? super T> subscriber) {
        final GoogleApiClient apiClient = createApiClient(new ApiClientConnectionCallbacks(subscriber));
        subscriptionInfoMap.put(apiClient, subscriber);

        try {
            apiClient.connect();
        } catch (Throwable ex) {
            subscriber.onError(ex);
        }

        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                if (apiClient.isConnected() || apiClient.isConnecting()) {
                    onUnsubscribed(apiClient);
                    apiClient.disconnect();
                }

                subscriptionInfoMap.remove(apiClient);
            }
        }));
    }

    protected abstract void onGoogleApiClientReady(GoogleApiClient apiClient, Subscriber<? super T> subscriber);

    protected final void handleResolutionResult(int resultCode, ConnectionResult connectionResult) {
        for (Map.Entry<GoogleApiClient, Subscriber<? super T>> entry : subscriptionInfoMap.entrySet()) {
            if (!entry.getValue().isUnsubscribed()) {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        entry.getKey().connect();
                    } catch (Throwable ex) {
                        entry.getValue().onError(ex);
                    }
                } else {
                    entry.getValue().onError(new GoogleAPIConnectionException("Error connecting to GoogleApiClient, resolution was not successful.", connectionResult));
                }
            }
        }
    }

    protected class ApiClientConnectionCallbacks extends BaseRx.ApiClientConnectionCallbacks {

        final protected Subscriber<? super T> subscriber;

        private GoogleApiClient apiClient;

        private ApiClientConnectionCallbacks(Subscriber<? super T> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onConnected(Bundle bundle) {
            try {
                onGoogleApiClientReady(apiClient, subscriber);
            } catch (Throwable ex) {
                subscriber.onError(ex);
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            subscriber.onError(new GoogleAPIConnectionSuspendedException(cause));
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            if(handleResolution && connectionResult.hasResolution()) {
                observableSet.add(BaseObservable.this);

                if(!ResolutionActivity.isResolutionShown()) {
                    Intent intent = new Intent(ctx, ResolutionActivity.class);
                    intent.putExtra(ResolutionActivity.ARG_CONNECTION_RESULT, connectionResult);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(intent);
                    if (ctx instanceof Activity) {
                        ((Activity)ctx).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                }
            } else {
                subscriber.onError(new GoogleAPIConnectionException("Error connecting to GoogleApiClient.", connectionResult));
            }
        }

        public void setClient(GoogleApiClient client) {
            this.apiClient = client;
        }
    }
}
