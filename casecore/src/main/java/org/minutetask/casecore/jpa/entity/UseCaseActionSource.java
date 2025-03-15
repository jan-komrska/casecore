package org.minutetask.casecore.jpa.entity;

/*-
 * ========================LICENSE_START=================================
 * org.minutetask.casecore:casecore
 * %%
 * Copyright (C) 2025 Jan Komrska
 * %%
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
 * =========================LICENSE_END==================================
 */

import java.lang.reflect.Method;

import org.apache.commons.lang3.ArrayUtils;
import org.minutetask.casecore.annotation.MethodRef;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class UseCaseActionSource {
    private Class<?> serviceClass = null;

    private Method method = null;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Object[] parameters = null;

    private Class<?> lastExceptionClass = null;

    private String lastExceptionMessage = null;

    private int retryCount = 0;

    //

    public Object[] getParameters() {
        return ArrayUtils.clone(ArrayUtils.nullToEmpty(parameters));
    }

    public void setParameters(Object[] parameters) {
        this.parameters = (ArrayUtils.isNotEmpty(parameters)) ? ArrayUtils.clone(parameters) : null;
    }

    public void setLastException(Throwable throwable) {
        if (throwable != null) {
            lastExceptionClass = throwable.getClass();
            lastExceptionMessage = throwable.getMessage();
        } else {
            lastExceptionClass = null;
            lastExceptionMessage = null;
        }
    }

    public void incRetryCount() {
        retryCount++;
    }

    //

    public boolean isPersistent() {
        MethodRef methodRef = (method != null) ? method.getAnnotation(MethodRef.class) : null;
        return (methodRef != null) ? methodRef.persistent() : false;
    }

    public boolean isAsync() {
        MethodRef methodRef = (method != null) ? method.getAnnotation(MethodRef.class) : null;
        return (methodRef != null) ? methodRef.async() : false;
    }

    public String getTaskExecutor() {
        MethodRef methodRef = (method != null) ? method.getAnnotation(MethodRef.class) : null;
        return (methodRef != null) ? methodRef.taskExecutor() : "";
    }

    //

    public boolean isEmpty() {
        return (serviceClass == null) && (method == null) && (parameters == null) //
                && (lastExceptionClass == null) && (lastExceptionMessage == null) //
                && (retryCount == 0);
    }
}
