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

import org.minutetask.casecore.annotation.MethodRef;
import org.minutetask.casecore.exception.BadRequestException;
import org.minutetask.casecore.jpa.entity.UseCaseActionEntity;
import org.minutetask.casecore.jpa.entity.UseCaseEntity;
import org.minutetask.casecore.service.api.LiteralService;
import org.minutetask.casecore.service.api.UseCaseActionService;
import org.minutetask.casecore.service.api.UseCaseDispatcher;
import org.minutetask.casecore.service.api.UseCaseService;
import org.minutetask.casecore.service.impl.UseCaseToolkit.KeyDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

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
    private LiteralService literalService;

    //

    public Object invokeImpl(Method method, Object[] args) throws Exception {
        MethodRef methodRef = method.getAnnotation(MethodRef.class);
        boolean persistentMethod = (methodRef != null) ? methodRef.persistent() : false;
        Long useCaseId = useCaseToolkit.getUseCaseId(method, args);
        KeyDto useCaseKey = useCaseToolkit.getUseCaseKey(method, args);
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
        Long contractId = literalService.getIdFromClass(method.getDeclaringClass());
        Long serviceClassId = useCase.getUseCaseData().getServices().get(contractId);
        Class<?> serviceClass = literalService.getClassFromId(serviceClassId);
        //
        UseCaseActionEntity useCaseAction = useCaseActionService.newAction(useCase);
        useCaseActionService.setActionServiceClass(useCaseAction, serviceClass);
        useCaseActionService.setActionMethod(useCaseAction, method);
        useCaseActionService.setActionArgs(useCaseAction, args);
        if (persistentMethod) {
            useCaseAction = useCaseActionService.persistAction(useCaseAction);
        }
        //
        Object result = useCaseToolkit.executeService(serviceClass, method, args);
        //
        if (persistentMethod) {
            useCaseAction.setClosed(true);
            useCaseAction = useCaseActionService.saveAction(useCaseAction);
        }
        return result;
    }

    @Override
    public Object invoke(Method method, Object[] args) throws Exception {
        MethodRef methodRef = method.getAnnotation(MethodRef.class);
        boolean asyncMethod = (methodRef != null) ? methodRef.async() : false;
        String taskExecutor = (methodRef != null) ? methodRef.taskExecutor() : "";
        //
        if (asyncMethod) {
            return useCaseToolkit.executeAsync(taskExecutor, method.getReturnType(), () -> {
                return self.invokeImpl(method, args);
            });
        } else {
            return self.invokeImpl(method, args);
        }
    }
}
