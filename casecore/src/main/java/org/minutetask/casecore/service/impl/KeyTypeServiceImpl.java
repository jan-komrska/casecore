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

import org.minutetask.casecore.exception.NotFoundException;
import org.minutetask.casecore.jpa.entity.KeyTypeEntity;
import org.minutetask.casecore.jpa.repository.KeyTypeRepository;
import org.minutetask.casecore.service.api.KeyTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.PersistenceException;

@Service
public class KeyTypeServiceImpl implements KeyTypeService {
    @Autowired
    private KeyTypeServiceImpl self;

    @Autowired
    private KeyTypeRepository keyTypeRepository;

    @Value("case-core.key-type.provisioning.repeat-count:2")
    private int repeatCount;
    @Value("case-core.key-type.provisioning.repeat-delay:100")
    private int repeatDelay;

    private Map<String, Long> keyTypeMap = new ConcurrentHashMap<String, Long>();

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Long findKeyTypeIdInDb(String type) {
        KeyTypeEntity entity = keyTypeRepository.findByName(type);
        if (entity != null) {
            keyTypeMap.put(entity.getName(), entity.getId());
            return entity.getId();
        } else {
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long saveKeyTypeIdInDb(String type) {
        KeyTypeEntity entity = new KeyTypeEntity();
        entity.setName(type);
        //
        entity = keyTypeRepository.save(entity);
        keyTypeMap.put(entity.getName(), entity.getId());
        return entity.getId();
    }

    @Override
    public Long getKeyTypeId(String type) {
        // TODO
        for (int index = 0; index < repeatCount; index++) {
            try {
                Long cachedId = keyTypeMap.get(type);
                if (cachedId != null) {
                    return cachedId;
                }
                //
                Long existingId = self.findKeyTypeIdInDb(type);
                if (existingId != null) {
                    return existingId;
                }
                //
                Long newId = self.saveKeyTypeIdInDb(type);
                if (newId != null) {
                    return newId;
                }
            } catch (PersistenceException ex) {
                // DO NOTHING
            }
            //
            try {
                Thread.sleep(repeatDelay);
            } catch (InterruptedException e) {
                // DO NOTHING
            }
        }
        //
        throw new NotFoundException();
    }
}
