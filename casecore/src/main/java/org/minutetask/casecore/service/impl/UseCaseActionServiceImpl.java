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
            return useCaseActionRepository.save(action);
        } else {
            return action;
        }
    }

    @Override
    @Transactional
    public UseCaseActionEntity saveAction(UseCaseActionEntity action) {
        if (action.getId() != null) {
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

    //

    @Override
    public Class<?> getActionServiceClass(UseCaseActionEntity action) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setActionServiceClass(UseCaseActionEntity action, Class<?> serviceClass) {
        // TODO Auto-generated method stub
    }

    @Override
    public Method getActionMethod(UseCaseActionEntity action) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setActionMethod(UseCaseActionEntity action, Method method) {
        // TODO Auto-generated method stub
    }

    @Override
    public Object[] getActionArgs(UseCaseActionEntity action) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setActionArgs(UseCaseActionEntity action, Object[] args) {
        // TODO Auto-generated method stub
    }
}
