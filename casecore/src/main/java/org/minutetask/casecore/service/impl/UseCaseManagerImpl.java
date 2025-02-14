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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.minutetask.casecore.annotation.Key;
import org.minutetask.casecore.annotation.ServiceRef;
import org.minutetask.casecore.exception.UnexpectedException;
import org.minutetask.casecore.jpa.entity.UseCaseEntity;
import org.minutetask.casecore.jpa.entity.UseCaseKeyEntity;
import org.minutetask.casecore.jpa.repository.UseCaseKeyRepository;
import org.minutetask.casecore.jpa.repository.UseCaseRepository;
import org.minutetask.casecore.service.api.KeyTypeService;
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
    private KeyTypeService keyTypeService;

    @Autowired
    private UseCaseRepository useCaseRepository;

    @Autowired
    private UseCaseKeyRepository useCaseKeyRepository;

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
        return useCaseRepository.save(useCase);
    }

    @Override
    public UseCaseEntity getUseCase(Long id) {
        return useCaseRepository.findById(id).orElse(null);
    }

    @Override
    public UseCaseEntity getUseCase(String keyType, String keyValue) {
        Long keyTypeId = keyTypeService.getKeyTypeId(keyType);
        UseCaseKeyEntity useCaseKeyEntity = useCaseKeyRepository.findByTypeAndValue(keyTypeId, keyValue);
        if (useCaseKeyEntity != null) {
            return useCaseKeyEntity.getUseCase();
        } else {
            return null;
        }
    }

    @Override
    public UseCaseEntity updateUseCase(UseCaseEntity useCase, Object data) {
        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            List<Field> parameterFields = FieldUtils.getAllFieldsList(data.getClass());
            for (Field parameterField : parameterFields) {
                String parameterName = parameterField.getName();
                Object parameterValue = FieldUtils.readField(parameterField, data, true);
                parameters.put(parameterName, parameterValue);
            }
            useCase.getParameters().putAll(parameters);
            //
            Map<String, String> keys = new HashMap<String, String>();
            List<Field> keyFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), Key.class);
            for (Field keyField : keyFields) {
                String keyType = keyField.getAnnotation(Key.class).type();
                Object keyValue = FieldUtils.readField(keyField, data, true);
                parameters.put(keyType, conversionService.convert(keyValue, String.class));
            }
            useCase.getKeys().putAll(keys);
            //
            Map<Class<?>, String> services = new HashMap<Class<?>, String>();
            List<Field> serviceFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), ServiceRef.class);
            for (Field serviceField : serviceFields) {
                Class<?> contractClass = serviceField.getAnnotation(ServiceRef.class).contract();
                String serviceName = (String) FieldUtils.readField(serviceField, data, true);
                services.put(contractClass, serviceName);
            }
            useCase.getServices().putAll(services);
        } catch (IllegalAccessException ex) {
            throw new UnexpectedException(ex);
        }
        //
        return useCaseRepository.save(useCase);
    }

    @Override
    public <Data> Data getUseCaseData(UseCaseEntity useCase, Class<Data> dataClass) {
        Object data;
        try {
            data = ConstructorUtils.invokeConstructor(dataClass);
        } catch (ReflectiveOperationException ex) {
            throw new UnexpectedException(ex);
        }
        //
        try {
            Map<String, Object> parameters = MapUtils.emptyIfNull(useCase.getParameters());
            List<Field> parameterFields = FieldUtils.getAllFieldsList(dataClass);
            for (Field parameterField : parameterFields) {
                String parameterName = parameterField.getName();
                Object parameterValue = parameters.get(parameterName);
                parameterValue = conversionService.convert(parameterValue, parameterField.getType());
                FieldUtils.writeField(parameterField, data, parameterValue);
            }
            //
            Map<String, String> keys = MapUtils.emptyIfNull(useCase.getKeys());
            List<Field> keyFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), Key.class);
            for (Field keyField : keyFields) {
                String keyType = keyField.getAnnotation(Key.class).type();
                Object keyValue = keys.get(keyType);
                keyValue = conversionService.convert(keyValue, keyField.getType());
                FieldUtils.writeField(keyField, data, keyValue);
            }
            //
            Map<Class<?>, String> services = MapUtils.emptyIfNull(useCase.getServices());
            List<Field> serviceFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), ServiceRef.class);
            for (Field serviceField : serviceFields) {
                Class<?> contractClass = serviceField.getAnnotation(ServiceRef.class).contract();
                String serviceName = services.get(contractClass);
                FieldUtils.writeField(serviceField, data, serviceName);
            }
        } catch (IllegalAccessException ex) {
            throw new UnexpectedException(ex);
        }
        //
        return dataClass.cast(data);
    }

    @Override
    public void deleteUseCase(Long id) {
        useCaseRepository.deleteById(id);
    }
}
