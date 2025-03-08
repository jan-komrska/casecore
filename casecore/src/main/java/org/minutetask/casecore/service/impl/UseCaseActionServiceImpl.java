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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.minutetask.casecore.exception.UnexpectedException;
import org.minutetask.casecore.jpa.entity.UseCaseActionEntity;
import org.minutetask.casecore.jpa.entity.UseCaseEntity;
import org.minutetask.casecore.jpa.repository.UseCaseActionRepository;
import org.minutetask.casecore.service.api.LiteralService;
import org.minutetask.casecore.service.api.UseCaseActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class UseCaseActionServiceImpl implements UseCaseActionService {
    @Autowired
    private LiteralService literalService;

    @Autowired
    private UseCaseActionRepository useCaseActionRepository;

    //

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public UseCaseActionEntity newAction(UseCaseEntity useCase) {
        UseCaseActionEntity action = new UseCaseActionEntity();
        action.setUseCase(useCase);
        action.setActive(false);
        action.setClosed(false);
        action.setCreatedDate(LocalDateTime.now());
        return action;
    }

    @Override
    public UseCaseActionEntity getAction(Long id) {
        return useCaseActionRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public UseCaseActionEntity persistAction(UseCaseActionEntity action) {
        if (action.getId() == null) {
            action.applyChanges();
            return useCaseActionRepository.save(action);
        } else {
            return action;
        }
    }

    @Override
    @Transactional
    public UseCaseActionEntity saveAction(UseCaseActionEntity action) {
        if (action.getId() != null) {
            action.applyChanges();
            return useCaseActionRepository.save(action);
        } else {
            return action;
        }
    }

    @Override
    @Transactional
    public UseCaseActionEntity deleteAction(UseCaseActionEntity action) {
        if (action.getId() != null) {
            useCaseActionRepository.delete(action);
            action.setId(null);
        }
        return action;
    }

    public List<UseCaseActionEntity> findScheduledActions(LocalDateTime targetDate) {
        return ListUtils.emptyIfNull(useCaseActionRepository.findScheduledActions(targetDate));
    }

    //

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Class<?> getActionServiceClass(UseCaseActionEntity action) {
        Long serviceClassId = action.getUseCaseActionData().getServiceClassId();
        return literalService.getClassFromId(serviceClassId);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public void setActionServiceClass(UseCaseActionEntity action, Class<?> serviceClass) {
        Long serviceClassId = literalService.getIdFromClass(serviceClass);
        action.getUseCaseActionData().setServiceClassId(serviceClassId);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Method getActionMethod(UseCaseActionEntity action) {
        String methodName = action.getUseCaseActionData().getMethodName();
        if (StringUtils.isNotEmpty(methodName)) {
            Long methodClassId = action.getUseCaseActionData().getMethodClassId();
            Class<?> methodClass = literalService.getClassFromId(methodClassId);
            //
            Class<?>[] parameterClasses = action.getUseCaseActionData().getParameterClassIds(). //
                    stream().map(literalService::getClassFromId).toArray(Class<?>[]::new);
            //
            try {
                return methodClass.getDeclaredMethod(methodName, parameterClasses);
            } catch (NoSuchMethodException ex) {
                throw new UnexpectedException(ex);
            }
        } else {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public void setActionMethod(UseCaseActionEntity action, Method method) {
        if (method != null) {
            Long methodClassId = literalService.getIdFromClass(method.getDeclaringClass());
            //
            List<Long> parameterClassIds = Arrays.stream(ArrayUtils.nullToEmpty(method.getParameterTypes())) //
                    .map(literalService::getIdFromClass).toList();
            parameterClassIds = new ArrayList<Long>(parameterClassIds);
            //
            action.getUseCaseActionData().setMethodClassId(methodClassId);
            action.getUseCaseActionData().setMethodName(method.getName());
            action.getUseCaseActionData().setParameterClassIds(parameterClassIds);
        } else {
            action.getUseCaseActionData().setMethodClassId(null);
            action.getUseCaseActionData().setMethodName(null);
            action.getUseCaseActionData().setParameterClassIds(null);
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Object[] getActionArgs(UseCaseActionEntity action) {
        return action.getUseCaseActionData().getParameters().toArray();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public void setActionArgs(UseCaseActionEntity action, Object[] args) {
        List<Object> parameters = new ArrayList<Object>();
        CollectionUtils.addAll(parameters, ArrayUtils.nullToEmpty(args));
        //
        action.getUseCaseActionData().setParameters(parameters);
    }
}
