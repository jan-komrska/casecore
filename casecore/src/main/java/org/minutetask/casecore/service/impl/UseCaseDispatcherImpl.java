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
import java.time.LocalDateTime;

import org.apache.commons.lang3.ArrayUtils;
import org.minutetask.casecore.jpa.entity.UseCaseActionEntity;
import org.minutetask.casecore.service.api.UseCaseActionService;
import org.minutetask.casecore.service.api.UseCaseDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.PostConstruct;

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
    private PlatformTransactionManager platformTransactionManager;

    private TransactionTemplate transactionTemplate;

    //

    @PostConstruct
    public void postConstruct() {
        transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.setPropagationBehavior(Propagation.REQUIRED.value());
    }

    public Object invokeImpl(UseCaseActionEntity useCaseAction) throws Exception {
        Long actionId = useCaseAction.getId();
        Class<?> serviceClass = useCaseActionService.getServiceClass(useCaseAction);
        Method contractMethod = useCaseActionService.getMethod(useCaseAction);
        Method serviceMethod = useCaseToolkit.getImplementationMethod(serviceClass, contractMethod);
        //
        Object[] newArgs = ArrayUtils.clone(ArrayUtils.nullToEmpty(useCaseActionService.getArgs(useCaseAction)));
        useCaseToolkit.setUseCaseId(serviceMethod, newArgs, useCaseAction.getUseCase().getId());
        //
        Object result;
        try {
            result = useCaseToolkit.executeService(serviceClass, serviceMethod, newArgs);
        } catch (Exception ex) {
            if (useCaseActionService.isPersistent(useCaseAction)) {
                transactionTemplate.execute((status) -> {
                    UseCaseActionEntity action = useCaseActionService.getAction(actionId);
                    useCaseActionService.setLastException(action, ex);
                    //
                    if (useCaseActionService.isAsync(action)) {
                        action.setActive(false);
                        action.setClosed(false);
                        action.setScheduledDate(LocalDateTime.now().plusSeconds(10));
                    } else {
                        action.setActive(false);
                        action.setClosed(true);
                    }
                    //
                    action = useCaseActionService.saveAction(action);
                    return action.getId();
                });
            }
            //
            return useCaseToolkit.rethrowException(ex);
        }
        //
        if (useCaseActionService.isPersistent(useCaseAction)) {
            transactionTemplate.execute((status) -> {
                UseCaseActionEntity action = useCaseActionService.getAction(actionId);
                action.setActive(false);
                action.setClosed(true);
                action.setScheduledDate(null);
                action = useCaseActionService.saveAction(action);
                //
                return action.getId();
            });
        }
        //
        return result;
    }

    @Override
    public Object invoke(Method method, Object[] args) throws Exception {
        UseCaseActionEntity useCaseAction = useCaseToolkit.newAction(method, args);
        //
        if (useCaseActionService.isAsync(useCaseAction)) {
            String taskExecutor = useCaseActionService.getTaskExecutor(useCaseAction);
            return useCaseToolkit.executeAsync(taskExecutor, method.getReturnType(), () -> {
                return self.invokeImpl(useCaseAction);
            });
        } else {
            return self.invokeImpl(useCaseAction);
        }
    }
}
