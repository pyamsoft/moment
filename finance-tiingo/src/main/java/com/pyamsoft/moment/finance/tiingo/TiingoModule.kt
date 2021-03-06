/*
 * Copyright 2020 Peter Kenji Yamanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.moment.finance.tiingo

import android.app.Application
import android.content.Context
import com.pyamsoft.cachify.Cached
import com.pyamsoft.moment.finance.FinanceSource
import com.pyamsoft.pydroid.bootstrap.network.DelegatingSocketFactory
import com.squareup.moshi.Moshi
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.MessageAdapter
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.StreamAdapter
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.tinder.streamadapter.coroutines.CoroutinesStreamAdapterFactory
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class InternalApi

@Module
abstract class TiingoModule {

    @Binds
    internal abstract fun bindFinanceSource(impl: Tiingo): FinanceSource

    @Module
    companion object {

        private const val REST_URL = "https://api.tiingo.com/"
        private const val WSS_URL = "wss://api.tiingo.com/iex"

        // For some reason we can't @Bind this, why not?
        // complains that @Binds method paramter type must be assignable to return typ
        // but obviously it is.
        @Provides
        @JvmStatic
        @InternalApi
        internal fun provideCache(
            context: Context,
            service: TiingoRestService
        ): Cached<List<String>> {
            return TiingoTickerCache(
                context,
                service
            )
        }

        @Provides
        @JvmStatic
        @InternalApi
        internal fun provideMoshi(): Moshi {
            return Moshi.Builder().build()
        }

        @Provides
        @JvmStatic
        @InternalApi
        internal fun provideOkHttpClient(@Named("debug") debug: Boolean): OkHttpClient {
            return OkHttpClient.Builder()
                .socketFactory(DelegatingSocketFactory.create())
                .also {
                    if (debug) {
                        val logging = HttpLoggingInterceptor()
                        logging.level = HttpLoggingInterceptor.Level.BODY
                        it.addInterceptor(logging)
                    }
                }
                .build()
        }

        @Provides
        @JvmStatic
        @InternalApi
        internal fun provideCallFactory(@InternalApi client: Lazy<OkHttpClient>): Call.Factory {
            return object : Call.Factory {

                override fun newCall(request: Request): Call {
                    return client.get().newCall(request)
                }
            }
        }

        @Provides
        @JvmStatic
        @InternalApi
        internal fun provideConverterFactory(@InternalApi moshi: Moshi): Converter.Factory {
            return MoshiConverterFactory.create(moshi)
        }

        @Provides
        @JvmStatic
        @InternalApi
        internal fun createRetrofit(
            @InternalApi callFactory: Call.Factory,
            @InternalApi converterFactory: Converter.Factory
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl(REST_URL)
                .callFactory(callFactory)
                .addConverterFactory(converterFactory)
                .build()
        }

        @Provides
        @JvmStatic
        @InternalApi
        internal fun providedMessageAdapterFactory(@InternalApi moshi: Moshi): MessageAdapter.Factory {
            return MoshiMessageAdapter.Factory(moshi)
        }

        @Provides
        @JvmStatic
        @InternalApi
        internal fun providedStreamAdapterFactory(): StreamAdapter.Factory {
            return CoroutinesStreamAdapterFactory()
        }

        @Provides
        @JvmStatic
        @InternalApi
        internal fun providedLifecycle(application: Application): Lifecycle {
            return AndroidLifecycle.ofApplicationForeground(application)
        }


        @Provides
        @JvmStatic
        @InternalApi
        internal fun providedWebSocketFactory(@InternalApi client: Lazy<OkHttpClient>): WebSocket.Factory {
            return object : WebSocket.Factory {

                private val delegate by lazy { client.get().newWebSocketFactory(WSS_URL) }

                override fun create(): WebSocket {
                    return delegate.create()
                }

            }
        }

        @Provides
        @JvmStatic
        @InternalApi
        internal fun createScarlet(
            @InternalApi lifecycle: Lifecycle,
            @InternalApi messageAdapterFactory: MessageAdapter.Factory,
            @InternalApi streamAdapterFactory: StreamAdapter.Factory,
            @InternalApi webSocketFactory: WebSocket.Factory
        ): Scarlet {
            return Scarlet.Builder()
                .lifecycle(lifecycle)
                .webSocketFactory(webSocketFactory)
                .addMessageAdapterFactory(messageAdapterFactory)
                .addStreamAdapterFactory(streamAdapterFactory)
                .build()
        }

        @Provides
        @JvmStatic
        @Singleton
        internal fun createWebsocketService(@InternalApi scarlet: Scarlet): TiingoWebSocketService {
            return scarlet.create(TiingoWebSocketService::class.java)
        }

        @Provides
        @JvmStatic
        @Singleton
        internal fun createRestService(@InternalApi retrofit: Retrofit): TiingoRestService {
            return retrofit.create(TiingoRestService::class.java)
        }
    }
}
