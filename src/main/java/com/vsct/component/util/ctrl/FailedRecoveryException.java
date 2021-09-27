/*
 *  Copyright (C) 2021 VSCT  
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.  
 * You may obtain a copy of the License at 
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software  
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.vsct.component.util.ctrl;

/**
 * Exception lors d'un appel à {@link Try#recover(Class, java.util.function.Function)} (ou
 * {@link Try#flatRecover(Class, java.util.function.Function)}), pour ne pas "perdre"
 * l'exception initiale.
 *
 * @since 1.0
 */
public class FailedRecoveryException extends RuntimeException {
    private final RuntimeException originalFailure;

    public FailedRecoveryException(Throwable cause, RuntimeException originalFailure) {
        super(cause);
        this.originalFailure = originalFailure;
    }

    public RuntimeException getOriginalFailure() {
        return originalFailure;
    }
}
