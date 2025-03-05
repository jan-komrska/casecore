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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.minutetask.casecore.CaseCoreScan;
import org.minutetask.casecore.annotation.ContractRef;
import org.minutetask.casecore.service.api.UseCaseDispatcher;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ContractPostProcessor implements BeanDefinitionRegistryPostProcessor {
    private ClassPathScanningCandidateComponentProvider createComponentProvider() {
        ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return super.isCandidateComponent(beanDefinition) || beanDefinition.getMetadata().isAbstract();
            }
        };
        componentProvider.addIncludeFilter(new AnnotationTypeFilter(ContractRef.class, true, true));
        return componentProvider;
    }

    private List<String> getContractPackages(String beanClassName) {
        if (StringUtils.isEmpty(beanClassName)) {
            return Collections.emptyList();
        }
        //
        try {
            Class<?> beanClass = Class.forName(beanClassName, true, Thread.currentThread().getContextClassLoader());
            CaseCoreScan contractScan = beanClass.getAnnotation(CaseCoreScan.class);
            //
            if (contractScan == null) {
                return Collections.emptyList();
            } else if (ArrayUtils.isNotEmpty(contractScan.value())) {
                return Arrays.asList(contractScan.value());
            } else {
                return Collections.singletonList(beanClass.getPackageName());
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void registerContractFactory(BeanDefinitionRegistry registry, String contractClassName) {
        Class<?> contractClass;
        try {
            contractClass = Class.forName(contractClassName, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
        //
        if (!contractClass.isInterface()) {
            return;
        }
        //
        String beanName = ContractFactory.class.getName() + "::" + contractClass.getName();
        //
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ContractFactory.class);
        beanDefinitionBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinitionBuilder.setPrimary(true);
        beanDefinitionBuilder.setLazyInit(false);
        beanDefinitionBuilder.addConstructorArgValue(contractClass);
        beanDefinitionBuilder.addPropertyValue("useCaseDispatcher", new RuntimeBeanReference(UseCaseDispatcher.class));
        //
        registry.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
        //
        log.info("Registered contract factory [" + beanName + "]");
    }

    //

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ClassPathScanningCandidateComponentProvider componentProvider = createComponentProvider();
        List<String> contractPackages = new ArrayList<String>();
        Set<String> contractClasses = new HashSet<String>();
        //
        for (String beanName : ArrayUtils.nullToEmpty(registry.getBeanDefinitionNames())) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            contractPackages.addAll(getContractPackages(beanDefinition.getBeanClassName()));
        }
        //
        for (String contractPackage : contractPackages) {
            Set<BeanDefinition> beanDefinitions = componentProvider.findCandidateComponents(contractPackage);
            for (BeanDefinition beanDefinition : beanDefinitions) {
                if (StringUtils.isNotEmpty(beanDefinition.getBeanClassName())) {
                    contractClasses.add(beanDefinition.getBeanClassName());
                }
            }
        }
        //
        for (String contractClass : contractClasses) {
            registerContractFactory(registry, contractClass);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // DO NOTHING
    }
}
