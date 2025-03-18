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

import org.minutetask.casecore.ActionContext;
import org.minutetask.casecore.jpa.entity.UseCaseActionEntity;
import org.minutetask.casecore.service.api.UseCaseActionService;
import org.minutetask.casecore.service.api.UseCaseDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class UseCaseDispatcherImpl implements UseCaseDispatcher {
    @Lazy
    @Autowired
    private UseCaseDispatcherImpl self;

    @Autowired
    private UseCaseActionService useCaseActionService;

    @Autowired
    private UseCaseToolkit useCaseToolkit;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    //

    public Object invokeImpl(UseCaseActionEntity action) throws Exception {
        Class<?> serviceClass = action.getSource().getServiceClass();
        Method contractMethod = action.getSource().getMethod();
        Method serviceMethod = useCaseToolkit.getImplementationMethod(serviceClass, contractMethod);
        //
        Object[] args = action.getSource().getParameters();
        useCaseToolkit.setUseCaseId(serviceMethod, args, action.getUseCase().getId());
        //
        ActionContext actionContext = useCaseToolkit.newActionContext(action);
        useCaseToolkit.setActionContext(serviceMethod, args, actionContext);
        //
        Object result;
        try {
            result = useCaseToolkit.executeService(serviceClass, serviceMethod, args);
        } catch (Exception exception) {
            action = useCaseToolkit.interruptAction(action, actionContext, exception);
            if (action.getScheduledDate() == null) {
                return useCaseToolkit.rethrowException(exception);
            } else {
                return null;
            }
        }
        //
        action = useCaseToolkit.finishAction(action);
        return result;
    }

    @Override
    public Object invoke(UseCaseActionEntity action) throws Throwable {
        Method contractMethod = action.getSource().getMethod();
        //
        if (action.getSource().isAsync()) {
            String taskExecutor = action.getSource().getTaskExecutor();
            return useCaseToolkit.executeAsync(taskExecutor, contractMethod.getReturnType(), () -> {
                return self.invokeImpl(action);
            });
        } else {
            return self.invokeImpl(action);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCustom(UseCaseActionReadyEvent actionReadyEvent) throws Throwable {
        UseCaseActionEntity action = useCaseActionService.getAction(actionReadyEvent.getActionId());
        Method contractMethod = action.getSource().getMethod();
        //
        String taskExecutor = action.getSource().getTaskExecutor();
        useCaseToolkit.executeAsync(taskExecutor, contractMethod.getReturnType(), () -> {
            return self.invokeImpl(action);
        });
    }

    @Override
    public Object invoke(Method method, Object[] args) throws Throwable {
        UseCaseActionEntity action = useCaseToolkit.newAction(method, args);
        boolean persistent = action.getSource().isPersistent();
        boolean async = action.getSource().isAsync();
        boolean inTransaction = TransactionSynchronizationManager.isActualTransactionActive();
        //
        if (persistent && async && inTransaction) {
            UseCaseActionReadyEvent actionReadyEvent = new UseCaseActionReadyEvent(this, action.getId());
            applicationEventPublisher.publishEvent(actionReadyEvent);
            return null;
        } else {
            return self.invoke(action);
        }
    }
}
