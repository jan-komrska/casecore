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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.minutetask.casecore.exception.BadRequestException;
import org.minutetask.casecore.exception.UnexpectedException;
import org.minutetask.casecore.jpa.entity.LiteralEntity;
import org.minutetask.casecore.jpa.repository.LiteralRepository;
import org.minutetask.casecore.service.api.LiteralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.PersistenceException;

@Service
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class LiteralServiceImpl implements LiteralService {
    @Lazy
    @Autowired
    private LiteralServiceImpl self;

    @Autowired
    private LiteralRepository literalRepository;

    @Value("${case-core.key-type.provisioning.repeat-count:3}")
    private int repeatCount;
    @Value("${case-core.key-type.provisioning.repeat-delay:100}")
    private int repeatDelay;

    private Map<Long, String> literalMap = new ConcurrentHashMap<Long, String>();
    private Map<String, Long> literalIdMap = new ConcurrentHashMap<String, Long>();

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Long findLiteralIdInDb(String value) {
        LiteralEntity entity = literalRepository.findByValue(value);
        if (entity != null) {
            literalMap.put(entity.getId(), entity.getValue());
            literalIdMap.put(entity.getValue(), entity.getId());
            return entity.getId();
        } else {
            return null;
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String findLiteralInDb(Long id) {
        LiteralEntity entity = literalRepository.findById(id).orElse(null);
        if (entity != null) {
            literalMap.put(entity.getId(), entity.getValue());
            literalIdMap.put(entity.getValue(), entity.getId());
            return entity.getValue();
        } else {
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long saveLiteralIdInDb(String value) {
        LiteralEntity entity = new LiteralEntity();
        entity.setValue(value);
        //
        entity = literalRepository.save(entity);
        literalMap.put(entity.getId(), entity.getValue());
        literalIdMap.put(entity.getValue(), entity.getId());
        return entity.getId();
    }

    @Override
    public Long getIdFromValue(String value) {
        if (StringUtils.isEmpty(value)) {
            throw new BadRequestException();
        }
        //
        PersistenceException exception = null;
        //
        for (int index = 0; index < repeatCount; index++) {
            try {
                Long cachedId = literalIdMap.get(value);
                if (cachedId != null) {
                    return cachedId;
                }
                //
                Long existingId = self.findLiteralIdInDb(value);
                if (existingId != null) {
                    return existingId;
                }
                //
                Long newId = self.saveLiteralIdInDb(value);
                if (newId != null) {
                    return newId;
                }
            } catch (PersistenceException ex) {
                exception = ex;
            }
            //
            try {
                Thread.sleep(repeatDelay);
            } catch (InterruptedException ex) {
                // DO NOTHING
            }
        }
        //
        throw new UnexpectedException(exception);
    }

    @Override
    public Long getIdFromValue(Class<?> value) {
        return getIdFromValue((value != null) ? value.getName() : null);
    }

    @Override
    public String getValueFromId(Long id) {
        if (id == null) {
            throw new BadRequestException();
        }
        //
        String cachedValue = literalMap.get(id);
        if (StringUtils.isNotEmpty(cachedValue)) {
            return cachedValue;
        }
        //
        String existingValue = self.findLiteralInDb(id);
        if (StringUtils.isNotEmpty(existingValue)) {
            return existingValue;
        }
        //
        throw new UnexpectedException();
    }

    @Override
    public Class<?> getClassFromId(Long id) {
        try {
            String value = getValueFromId(id);
            return Class.forName(value, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException ex) {
            throw new UnexpectedException(ex);
        }
    }
}
