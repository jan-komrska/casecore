package org.minutetask.casecore.factory;

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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.minutetask.casecore.annotation.MethodRef;
import org.minutetask.casecore.service.api.UseCaseDispatcher;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class ContractFactory<Contract> extends AbstractFactoryBean<Contract> {
    private Class<Contract> contractClass = null;
    private UseCaseDispatcher useCaseDispatcher = null;

    //

    public ContractFactory(Class<Contract> contractClass) {
        this.contractClass = contractClass;
    }

    //

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public Class<?> getObjectType() {
        return contractClass;
    }

    @Override
    protected Contract createInstance() throws Exception {
        return contractClass.cast(Proxy.newProxyInstance( //
                Thread.currentThread().getContextClassLoader(), //
                new Class<?>[] { contractClass }, //
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        MethodRef methodRef = method.getAnnotation(MethodRef.class);
                        boolean inlineMethod = (methodRef != null) ? methodRef.inline() : false;
                        if (inlineMethod && method.isDefault()) {
                            return InvocationHandler.invokeDefault(proxy, method, args);
                        }
                        //
                        return useCaseDispatcher.invoke(method, args);
                    }
                }));
    }

    //

    public UseCaseDispatcher getUseCaseDispatcher() {
        return useCaseDispatcher;
    }

    public void setUseCaseDispatcher(UseCaseDispatcher useCaseDispatcher) {
        this.useCaseDispatcher = useCaseDispatcher;
    }
}
