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

package com.pyamsoft.moment.finance.model

import androidx.annotation.CheckResult

data class Symbol internal constructor(internal val symbol: String?) {

    @CheckResult
    fun isValid(): Boolean {
        return symbol != null
    }

    @CheckResult
    fun symbol(): String {
        return symbol.orEmpty()
    }

    companion object {

        @JvmStatic
        @CheckResult
        fun empty(): Symbol {
            return Symbol(null)
        }
    }
}

@CheckResult
fun String?.toSymbol(): Symbol {
    return Symbol(this)
}