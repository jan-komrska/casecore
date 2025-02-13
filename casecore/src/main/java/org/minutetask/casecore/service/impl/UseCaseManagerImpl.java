package org.minutetask.casecore.service.impl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.minutetask.casecore.annotation.Key;
import org.minutetask.casecore.annotation.ServiceRef;
import org.minutetask.casecore.exception.UnexpectedException;

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

import org.minutetask.casecore.jpa.entity.UseCaseEntity;
import org.minutetask.casecore.jpa.repository.KeyTypeRepository;
import org.minutetask.casecore.jpa.repository.UseCaseRepository;
import org.minutetask.casecore.service.api.UseCaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//TODO
@Transactional
@Service
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class UseCaseManagerImpl implements UseCaseManager {
    @Autowired
    private KeyTypeRepository keyTypeRepository;

    @Autowired
    private UseCaseRepository useCaseRepository;

    @Autowired
    private ConversionService conversionService;

    @Override
    public UseCaseEntity createUseCase(Object data) {
        UseCaseEntity useCase = new UseCaseEntity();
        //
        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            List<Field> parameterFields = FieldUtils.getAllFieldsList(data.getClass());
            for (Field parameterField : parameterFields) {
                String parameterName = parameterField.getName();
                Object parameterValue = FieldUtils.readField(parameterField, data, true);
                parameters.put(parameterName, parameterValue);
            }
            useCase.setParameters(parameters);
            //
            Map<String, String> keys = new HashMap<String, String>();
            List<Field> keyFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), Key.class);
            for (Field keyField : keyFields) {
                String keyType = keyField.getAnnotation(Key.class).type();
                Object keyValue = FieldUtils.readField(keyField, data, true);
                parameters.put(keyType, conversionService.convert(keyValue, String.class));
            }
            useCase.setKeys(keys);
            //
            Map<Class<?>, String> services = new HashMap<Class<?>, String>();
            List<Field> serviceFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), ServiceRef.class);
            for (Field serviceField : serviceFields) {
                Class<?> contractClass = serviceField.getAnnotation(ServiceRef.class).contract();
                String serviceName = (String) FieldUtils.readField(serviceField, data, true);
                services.put(contractClass, serviceName);
            }
            useCase.setServices(services);
        } catch (IllegalAccessException ex) {
            throw new UnexpectedException(ex);
        }
        //
        useCase = useCaseRepository.save(useCase);
        return useCase;
    }

    @Override
    public UseCaseEntity getUseCase(Long id) {
        return useCaseRepository.findById(id).orElse(null);
    }

    @Override
    public UseCaseEntity getUseCase(String keyType, String keyValue) {
        // TODO
        return null;
    }

    @Override
    public void updateUseCase(UseCaseEntity useCase, Object data) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteUseCase(Long id) {
        useCaseRepository.deleteById(id);
    }
}
