/*
 * This file is part of the ETECTURE Open Source Community Projects.
 *
 * Copyright (c) 2013 by:
 *
 * ETECTURE GmbH
 * Darmstädter Landstraße 112
 * 60598 Frankfurt
 * Germany
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the author nor the names of its contributors may be
 *    used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicrepositories.api.Generator;
import de.etecture.opensource.dynamicrepositories.api.Param;
import de.etecture.opensource.dynamicrepositories.api.Query;
import de.etecture.opensource.dynamicrepositories.api.ResultConverter;
import de.etecture.opensource.dynamicrepositories.spi.QueryMetaData;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author rhk
 */
public class DefaultQueryMetaData<T> implements QueryMetaData<T> {

    private final Map<String, Object> parameters = new HashMap<>();
    private final String queryName;
    private final String query;
    private final Type type;
    private final Class<T> queryType;
    private final Kind queryKind;
    private final String technology;
    private final String connection;
    private ResultConverter resultConverter;
    private Class<?> repositoryClass;

    public DefaultQueryMetaData(QueryMetaData<T> metaData) {
        this(metaData.getType(), metaData.getQueryType(), metaData
                .getQueryKind(), metaData
                .getQuery(), metaData.getQueryName(), metaData
                .getQueryTechnology(), metaData.getConnection(), metaData
                .getParameterMap());
    }

    public DefaultQueryMetaData(Type type, Class<T> queryType, Kind queryKind,
            String query,
            String queryName, String technology, String connection,
            Map<String, Object> parameters) {
        this.type = type;
        this.queryType = queryType;
        this.queryKind = queryKind;
        this.query = query;
        this.queryName = queryName;
        this.parameters.putAll(parameters);
        this.technology = technology;
        this.connection = connection;
        this.resultConverter = null;
        this.repositoryClass = queryType;
    }

    public DefaultQueryMetaData(Query query, String methodName,
            Class<T> queryType, Type type, Kind queryKind,
            Map<String, Object> parameters) {
        this.queryType = queryType;
        this.type = type;
        this.repositoryClass = queryType;
        this.queryKind = queryKind;
        this.technology = query.technology();
        if (StringUtils.isEmpty(query.name()) && StringUtils.isEmpty(query
                .value())) {
            this.queryName = methodName;
            this.query = "";
        } else {
            this.query = query.value();
            this.queryName = query.name();
        }

        this.connection = query.connection();
        this.parameters.putAll(parameters);
        if (query.converter().getName().equals(ResultConverter.class.getName())) {
            this.resultConverter = null;
        } else {
            try {
                this.resultConverter = query.converter().newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new IllegalArgumentException(
                        "cannot instantiate converter.",
                        ex);
            }
        }
    }

    @Override
    public Set<String> getParameterNames() {
        return parameters.keySet();
    }

    @Override
    public Map<String, Object> getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public Object getParameterValue(String parameterName) {
        return parameters.get(parameterName);
    }

    @Override
    public int getOffset() {
        return -1;
    }

    @Override
    public int getCount() {
        return -1;
    }

    @Override
    public String getQueryName() {
        return queryName;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public Class<T> getQueryType() {
        return queryType;
    }

    @Override
    public Kind getQueryKind() {
        return queryKind;
    }

    @Override
    public Exception createException(
            Class<? extends Annotation> qualifier, String message,
            Exception cause) {
        return cause;
    }

    @Override
    public ResultConverter<T> getConverter() {
        return (ResultConverter<T>) this.resultConverter;
    }

    public void setConverter(ResultConverter resultConverter) {
        this.resultConverter = resultConverter;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Annotation[] getAnnotations() {
        return new Annotation[0];
    }

    @Override
    public Class<?> getRepositoryClass() {
        return repositoryClass;
    }

    public void setRepositoryClass(Class<?> repositoryClass) {
        this.repositoryClass = repositoryClass;
    }

    public void addParameter(String name, Object value) {
        parameters.put(name, value);
    }

    public void addParameter(Param param) {
        if (param.generator().getName().equals(Generator.class.getName())) {
            final String value = param.value();
            if ("$$$generated$$$".equals(value)) {
                throw new IllegalArgumentException(String.format(
                        "Either generator or value must be specified for parameter defintion '%s'!",
                        param.name()));
            }
            parameters.put(param.name(), ConvertUtils.convert(value, param
                    .type()));
        } else {
            try {
                final Generator generator = param.generator().newInstance();
                parameters.put(param.name(), ConvertUtils.convert(generator
                        .generateValue(param), param.type()));
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new IllegalArgumentException(
                        "The generator cannot be instantiated. ", ex);
            }
        }
    }

    @Override
    public String getQueryTechnology() {
        return this.technology;
    }

    @Override
    public String getConnection() {
        return this.connection;
    }
}
