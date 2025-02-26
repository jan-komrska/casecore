package org.minutetask.casecore.service.impl;

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
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.minutetask.casecore.annotation.MethodRef;
import org.minutetask.casecore.service.api.UseCaseDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

@Service
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class UseCaseDispatcherImpl implements UseCaseDispatcher {
    @Autowired
    private ApplicationContext applicationContext;

    @Lazy
    @Autowired
    private UseCaseDispatcherImpl self;

    public Object invokeImpl(Method method, Object[] args) throws Throwable {
        return null;
    }

    @Override
    public Object invoke(Method method, Object[] args) throws Throwable {
        MethodRef methodRef = method.getAnnotation(MethodRef.class);
        String executorName = (methodRef != null) ? methodRef.async() : null;
        if (StringUtils.isNotEmpty(executorName)) {
            AsyncTaskExecutor asyncTaskExecutor = applicationContext.getBean(executorName, AsyncTaskExecutor.class);
            return asyncTaskExecutor.submitCompletable(() -> {
                try {
                    Object result = self.invokeImpl(method, args);
                    if (result instanceof Future<?> future) {
                        return future.get();
                    } else {
                        return result;
                    }
                } catch (Exception ex) {
                    throw ex;
                } catch (Throwable ex) {
                    throw new IllegalStateException(ex);
                }
            });
        } else {
            return self.invokeImpl(method, args);
        }
    }
}
