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

import org.apache.commons.lang3.ArrayUtils;
import org.minutetask.casecore.jpa.entity.UseCaseActionEntity;
import org.minutetask.casecore.jpa.entity.UseCaseEntity;
import org.minutetask.casecore.service.api.UseCaseActionService;
import org.minutetask.casecore.service.api.UseCaseDispatcher;
import org.minutetask.casecore.service.api.UseCaseService;
import org.minutetask.casecore.service.impl.UseCaseToolkit.Invocation;
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
    private UseCaseService useCaseService;

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

    public Object invokeImpl(Invocation invocation, Method method, Object[] args) throws Exception {
        final Long actionId;
        //
        if (invocation.isPersistent()) {
            actionId = transactionTemplate.execute((status) -> {
                UseCaseEntity parentUseCase = useCaseService.getUseCase(invocation.getUseCaseId());
                //
                UseCaseActionEntity useCaseAction = useCaseActionService.newAction(parentUseCase);
                useCaseActionService.setActionServiceClass(useCaseAction, invocation.getServiceClass());
                useCaseActionService.setActionMethod(useCaseAction, method);
                useCaseActionService.setActionArgs(useCaseAction, args);
                //
                parentUseCase.getUseCaseActions().add(useCaseAction);
                useCaseAction = useCaseActionService.persistAction(useCaseAction);
                useCaseService.saveUseCase(parentUseCase);
                //
                return useCaseAction.getId();
            });
        } else {
            actionId = null;
        }
        //
        Method serviceMethod = useCaseToolkit.getImplementationMethod(invocation.getServiceClass(), method);
        Object[] newArgs = ArrayUtils.clone(ArrayUtils.nullToEmpty(args));
        useCaseToolkit.setUseCaseId(serviceMethod, newArgs, invocation.getUseCaseId());
        //
        Object result = useCaseToolkit.executeService(invocation.getServiceClass(), method, newArgs);
        //
        if (invocation.isPersistent()) {
            transactionTemplate.execute((status) -> {
                UseCaseActionEntity useCaseAction = useCaseActionService.getAction(actionId);
                useCaseAction.setClosed(true);
                useCaseAction = useCaseActionService.saveAction(useCaseAction);
                //
                return useCaseAction.getId();
            });
        }
        //
        return result;
    }

    @Override
    public Object invoke(Method method, Object[] args) throws Exception {
        Invocation invocation = useCaseToolkit.getInvocation(method, args);
        //
        if (invocation.isAsync()) {
            return useCaseToolkit.executeAsync(invocation.getTaskExecutor(), method.getReturnType(), () -> {
                return self.invokeImpl(invocation, method, args);
            });
        } else {
            return self.invokeImpl(invocation, method, args);
        }
    }
}
