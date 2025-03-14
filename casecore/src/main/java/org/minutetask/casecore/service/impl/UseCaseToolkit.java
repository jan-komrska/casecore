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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.hibernate.resource.beans.container.internal.NoSuchBeanException;
import org.minutetask.casecore.ActionContext;
import org.minutetask.casecore.annotation.IdRef;
import org.minutetask.casecore.annotation.KeyRef;
import org.minutetask.casecore.exception.BadRequestException;
import org.minutetask.casecore.exception.ConflictException;
import org.minutetask.casecore.exception.NotFoundException;
import org.minutetask.casecore.exception.UnexpectedException;
import org.minutetask.casecore.jpa.entity.UseCaseActionEntity;
import org.minutetask.casecore.jpa.entity.UseCaseEntity;
import org.minutetask.casecore.service.api.LiteralService;
import org.minutetask.casecore.service.api.UseCaseActionService;
import org.minutetask.casecore.service.api.UseCaseService;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.java.Log;

@Log
@Service
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class UseCaseToolkit {
    private static final String DEFAULT_TASK_EXECUTOR = "taskExecutor";

    private volatile String defaultExecutorName = null;
    private volatile SimpleAsyncTaskExecutor internalExecutor = null;

    @Qualifier("org.minutetask.casecore.CoreCaseConfiguration::objectMapper")
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private LiteralService literalService;

    @Autowired
    private UseCaseService useCaseService;

    @Autowired
    private UseCaseActionService useCaseActionService;

    //

    public <Result> Result rethrowException(Throwable throwable) throws Exception {
        if (throwable instanceof Exception exception) {
            throw exception;
        } else if (throwable instanceof Error error) {
            throw error;
        } else {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    public Method getImplementationMethod(Class<?> serviceClass, Method method) {
        Class<?>[] parameterTypes = ArrayUtils.nullToEmpty(method.getParameterTypes());
        return MethodUtils.getMatchingMethod(serviceClass, method.getName(), parameterTypes);
    }

    //

    @Getter
    @AllArgsConstructor
    @ToString
    public static class KeyDto {
        private String type;
        private String value;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public class ActionContextDto implements ActionContext {
        private int retryOnFailureDelay;
        private int retryCount;

        private Class<?> lastExceptionClass;
        private String lastExceptionMessage;
    }

    public Long getUseCaseId(Method method, Object[] args) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        parameterAnnotations = ArrayUtils.nullToEmpty(parameterAnnotations, Annotation[][].class);
        //
        for (int pindex = 0; pindex < parameterAnnotations.length; pindex++) {
            Annotation[] annotations = parameterAnnotations[pindex];
            annotations = ArrayUtils.nullToEmpty(annotations, Annotation[].class);
            //
            for (int aindex = 0; aindex < annotations.length; aindex++) {
                Annotation annotation = annotations[aindex];
                if (annotation instanceof IdRef) {
                    return objectMapper.convertValue(args[pindex], Long.class);
                }
            }
        }
        //
        return null;
    }

    public void setUseCaseId(Method method, Object[] args, Long id) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        parameterAnnotations = ArrayUtils.nullToEmpty(parameterAnnotations, Annotation[][].class);
        //
        for (int pindex = 0; pindex < parameterAnnotations.length; pindex++) {
            Annotation[] annotations = parameterAnnotations[pindex];
            annotations = ArrayUtils.nullToEmpty(annotations, Annotation[].class);
            //
            for (int aindex = 0; aindex < annotations.length; aindex++) {
                Annotation annotation = annotations[aindex];
                if (annotation instanceof IdRef) {
                    Class<?> parameterType = method.getParameterTypes()[pindex];
                    args[pindex] = objectMapper.convertValue(id, parameterType);
                }
            }
        }
    }

    public KeyDto getUseCaseKey(Method method, Object[] args) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        parameterAnnotations = ArrayUtils.nullToEmpty(parameterAnnotations, Annotation[][].class);
        //
        for (int pindex = 0; pindex < parameterAnnotations.length; pindex++) {
            Annotation[] annotations = parameterAnnotations[pindex];
            annotations = ArrayUtils.nullToEmpty(annotations, Annotation[].class);
            //
            for (int aindex = 0; aindex < annotations.length; aindex++) {
                Annotation annotation = annotations[aindex];
                if (annotation instanceof KeyRef keyRef) {
                    String type = keyRef.value();
                    String value = objectMapper.convertValue(args[pindex], String.class);
                    if (StringUtils.isNotEmpty(value)) {
                        return new KeyDto(type, value);
                    } else {
                        return null;
                    }
                }
            }
        }
        //
        return null;
    }

    public void setActionContext(Method method, Object[] args, ActionContext actionContext) {
        Class<?>[] parameterTypes = ArrayUtils.nullToEmpty(method.getParameterTypes());
        for (int index = 0; index < parameterTypes.length; index++) {
            Class<?> parameterType = parameterTypes[index];
            if (ActionContext.class.equals(parameterType)) {
                args[index] = actionContext;
            }
        }
    }

    private Object[] prepareActionArgs(Object[] args) {
        args = ArrayUtils.clone(ArrayUtils.nullToEmpty(args));
        for (int index = 0; index < args.length; index++) {
            if (args[index] instanceof ActionContext) {
                args[index] = null;
            }
        }
        return args;
    }

    @Transactional
    public UseCaseActionEntity newAction(Method method, Object[] args) {
        args = prepareActionArgs(args);
        //
        Long useCaseId = getUseCaseId(method, args);
        KeyDto useCaseKey = getUseCaseKey(method, args);
        //
        UseCaseEntity useCase;
        if (useCaseId != null) {
            useCase = useCaseService.getUseCase(useCaseId);
        } else if (useCaseKey != null) {
            useCase = useCaseService.getUseCase(useCaseKey.getType(), useCaseKey.getValue());
        } else {
            throw new BadRequestException();
        }
        //
        if (useCase.isClosed()) {
            throw new ConflictException();
        }
        //
        UseCaseActionEntity useCaseAction = useCaseActionService.newAction(useCase, true);
        //
        Long contractId = literalService.getIdFromClass(method.getDeclaringClass());
        Long serviceClassId = useCase.getUseCaseData().getServices().get(contractId);
        Class<?> serviceClass = literalService.getClassFromId(serviceClassId);
        //
        useCaseActionService.setServiceClass(useCaseAction, serviceClass);
        useCaseActionService.setMethod(useCaseAction, method);
        useCaseActionService.setArgs(useCaseAction, args);
        //
        if (useCaseActionService.isPersistent(useCaseAction)) {
            useCaseAction = useCaseActionService.persistAction(useCaseAction);
        }
        //
        return useCaseAction;
    }

    public UseCaseActionEntity finishAction(UseCaseActionEntity useCaseAction) {
        useCaseAction.setActive(false);
        useCaseAction.setClosed(true);
        useCaseAction.setScheduledDate(null);
        //
        useCaseActionService.setLastException(useCaseAction, null);
        //
        if (useCaseActionService.isPersistent(useCaseAction)) {
            useCaseAction = useCaseActionService.saveAction(useCaseAction);
        }
        //
        return useCaseAction;
    }

    public UseCaseActionEntity interruptAction(UseCaseActionEntity useCaseAction, ActionContext actionContext, Throwable throwable) {
        if (useCaseActionService.isAsync(useCaseAction) && (actionContext.getRetryOnFailureDelay() >= 0)) {
            useCaseAction.setActive(false);
            useCaseAction.setClosed(false);
            useCaseAction.setScheduledDate(LocalDateTime.now().plusSeconds(actionContext.getRetryOnFailureDelay()));
            //
            log.log(Level.WARNING, "Schedulled retry because of exception {0}: {1}", //
                    new Object[] { throwable.getClass().getSimpleName(), throwable.getMessage() });
        } else {
            useCaseAction.setActive(false);
            useCaseAction.setClosed(true);
        }
        //
        useCaseActionService.setLastException(useCaseAction, throwable);
        //
        if (useCaseActionService.isPersistent(useCaseAction)) {
            useCaseAction = useCaseActionService.saveAction(useCaseAction);
        }
        //
        return useCaseAction;
    }

    public ActionContext newActionContext(UseCaseActionEntity useCaseAction) {
        ActionContextDto actionContext = new ActionContextDto();
        //
        actionContext.setRetryOnFailureDelay(-1);
        actionContext.setRetryCount(useCaseActionService.getRetryCount(useCaseAction));
        actionContext.setLastExceptionClass(useCaseActionService.getLastExceptionClass(useCaseAction));
        actionContext.setLastExceptionMessage(useCaseActionService.getLastExceptionMessage(useCaseAction));
        //
        return actionContext;
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
            Object result = null;
            //
            try {
                result = callable.call();
            } catch (Exception ex) {
                if (resultClass.equals(Void.TYPE)) {
                    log.log(Level.SEVERE, "Unexpected exception:", ex);
                }
                return rethrowException(ex);
            }
            //
            if (result instanceof Future<?> future) {
                try {
                    return future.get();
                } catch (ExecutionException ex) {
                    if (resultClass.equals(Void.TYPE)) {
                        log.log(Level.SEVERE, "Unexpected exception:", ex.getCause());
                    }
                    return rethrowException(ex.getCause());
                }
            }
            //
            return result;
        };
        //
        if (resultClass.equals(CompletableFuture.class)) {
            Object proxyResult = proxyExecutor.submitCompletable(proxyCallable);
            return resultClass.cast(proxyResult);
        } else if (resultClass.isAssignableFrom(Future.class)) {
            Object proxyResult = proxyExecutor.submit(proxyCallable);
            return resultClass.cast(proxyResult);
        } else if (resultClass.equals(Void.TYPE)) {
            proxyExecutor.submit(proxyCallable);
            return null;
        } else {
            throw new BadRequestException();
        }
    }

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
