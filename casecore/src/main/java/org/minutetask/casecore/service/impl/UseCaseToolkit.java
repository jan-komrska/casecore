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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.resource.beans.container.internal.NoSuchBeanException;
import org.minutetask.casecore.exception.BadRequestException;
import org.minutetask.casecore.exception.ConflictException;
import org.minutetask.casecore.exception.NotFoundException;
import org.minutetask.casecore.exception.UnexpectedException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Service;

import lombok.extern.java.Log;

@Log
@Service
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class UseCaseToolkit {
    private static final String DEFAULT_TASK_EXECUTOR = "taskExecutor";

    private volatile String defaultExecutorName = null;
    private volatile SimpleAsyncTaskExecutor internalExecutor = null;

    @Autowired
    private ApplicationContext applicationContext;

    //

    public static <Result> Result rethrowException(Throwable throwable) throws Exception {
        if (throwable instanceof Exception exception) {
            throw exception;
        } else if (throwable instanceof Error error) {
            throw error;
        } else {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    //

    private AsyncTaskExecutor getAsyncTaskExecutor(String executorName) {
        if (StringUtils.isNotEmpty(executorName)) {
            return applicationContext.getBean(executorName, AsyncTaskExecutor.class);
        } else if (StringUtils.isNotEmpty(defaultExecutorName)) {
            return applicationContext.getBean(defaultExecutorName, AsyncTaskExecutor.class);
        } else if (internalExecutor != null) {
            return internalExecutor;
        } else {
            synchronized (this) {
                if (StringUtils.isEmpty(defaultExecutorName) && (internalExecutor == null)) {
                    String[] executorNames = applicationContext.getBeanNamesForType(AsyncTaskExecutor.class);
                    executorNames = ArrayUtils.nullToEmpty(executorNames);
                    //
                    if (executorNames.length == 1) {
                        defaultExecutorName = executorNames[0];
                    } else if (ArrayUtils.contains(executorNames, DEFAULT_TASK_EXECUTOR)) {
                        defaultExecutorName = DEFAULT_TASK_EXECUTOR;
                    } else {
                        internalExecutor = new SimpleAsyncTaskExecutor();
                    }
                }
            }
            //
            return getAsyncTaskExecutor(executorName);
        }
    }

    public Object executeAsync(String executorName, Class<?> resultClass, Callable<Object> callable) {
        AsyncTaskExecutor proxyExecutor = getAsyncTaskExecutor(executorName);
        Callable<Object> proxyCallable = () -> {
            Object result = callable.call();
            //
            if (result instanceof Future<?> future) {
                try {
                    return future.get();
                } catch (ExecutionException ex) {
                    if (resultClass.isAssignableFrom(Void.class)) {
                        log.log(Level.SEVERE, "Unexpected exception:", ex.getCause());
                    }
                    return rethrowException(ex.getCause());
                }
            }
            //
            return null;
        };
        //
        if (resultClass.isAssignableFrom(CompletableFuture.class)) {
            Object proxyResult = proxyExecutor.submitCompletable(proxyCallable);
            return resultClass.cast(proxyResult);
        } else if (resultClass.isAssignableFrom(Future.class)) {
            Object proxyResult = proxyExecutor.submit(proxyCallable);
            return resultClass.cast(proxyResult);
        } else if (resultClass.isAssignableFrom(Void.class)) {
            proxyExecutor.submit(proxyCallable);
            return null;
        } else {
            throw new BadRequestException();
        }
    }

    //

    public Object executeService(Class<?> serviceClass, Method method, Object[] args) throws Exception {
        Object service;
        try {
            service = applicationContext.getBean(serviceClass);
        } catch (NoSuchBeanException ex) {
            throw new NotFoundException(ex);
        } catch (NoUniqueBeanDefinitionException ex) {
            throw new ConflictException(ex);
        }
        //
        try {
            return method.invoke(service, args);
        } catch (IllegalAccessException ex) {
            throw new UnexpectedException(ex);
        } catch (InvocationTargetException ex) {
            return rethrowException(ex.getCause());
        }
    }
}
