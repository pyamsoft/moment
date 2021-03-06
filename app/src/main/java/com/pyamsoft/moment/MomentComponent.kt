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

package com.pyamsoft.moment

import android.app.Application
import android.content.Context
import androidx.annotation.CheckResult
import com.pyamsoft.moment.chart.ChartComponent
import com.pyamsoft.moment.main.MainComponent
import com.pyamsoft.moment.finance.tiingo.TiingoModule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [MomentComponent.MomentProvider::class, TiingoModule::class])
internal interface MomentComponent {

    @CheckResult
    fun plusMainComponent(): MainComponent.Factory

    @CheckResult
    fun plusChartComponent(): ChartComponent.Factory

    @Component.Factory
    interface Factory {

        @CheckResult
        fun create(
            @BindsInstance application: Application,
            @Named("debug") @BindsInstance debug: Boolean
        ): MomentComponent
    }

    @Module
    abstract class MomentProvider {

        @Module
        companion object {

            @Provides
            @JvmStatic
            internal fun provideContext(application: Application): Context {
                return application
            }

            @Provides
            @JvmStatic
            @Named("app_name")
            internal fun provideAppNameRes(): Int {
                return R.string.app_name
            }
        }
    }
}
