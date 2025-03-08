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

import org.minutetask.casecore.jpa.entity.UseCaseActionEntity;
import org.minutetask.casecore.service.api.UseCaseActionService;
import org.minutetask.casecore.service.api.UseCaseDispatcher;
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
    private UseCaseActionService useCaseActionService;

    @Autowired
    private UseCaseToolkit useCaseToolkit;

    //

    public Object invokeImpl(UseCaseActionEntity action) throws Exception {
        Class<?> serviceClass = useCaseActionService.getServiceClass(action);
        Method contractMethod = useCaseActionService.getMethod(action);
        Method serviceMethod = useCaseToolkit.getImplementationMethod(serviceClass, contractMethod);
        //
        Object[] args = useCaseActionService.getArgs(action);
        useCaseToolkit.setUseCaseId(serviceMethod, args, action.getUseCase().getId());
        //
        Object result;
        try {
            result = useCaseToolkit.executeService(serviceClass, serviceMethod, args);
        } catch (Exception ex) {
            action = useCaseToolkit.interruptAction(action, ex);
            return useCaseToolkit.rethrowException(ex);
        }
        //
        action = useCaseToolkit.finishAction(action);
        return result;
    }

    @Override
    public Object invoke(UseCaseActionEntity action) throws Throwable {
        Method contractMethod = useCaseActionService.getMethod(action);
        //
        if (useCaseActionService.isAsync(action)) {
            String taskExecutor = useCaseActionService.getTaskExecutor(action);
            return useCaseToolkit.executeAsync(taskExecutor, contractMethod.getReturnType(), () -> {
                return self.invokeImpl(action);
            });
        } else {
            return self.invokeImpl(action);
        }
    }

    @Override
    public Object invoke(Method method, Object[] args) throws Throwable {
        return invoke(useCaseToolkit.newAction(method, args));
    }
}
