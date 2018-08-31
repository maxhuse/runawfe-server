/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.wfe.var;

import java.util.Arrays;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.CurrentVariableDeleteLog;
import ru.runa.wfe.audit.CurrentVariableLog;
import ru.runa.wfe.audit.CurrentVariableCreateLog;
import ru.runa.wfe.audit.CurrentVariableUpdateLog;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.converter.SerializableToByteArrayConverter;

/**
 * Base class for classes that store variable values in the database.
 */
@Entity
@Table(name = "BPM_VARIABLE", uniqueConstraints = { @UniqueConstraint(name = "UK_VARIABLE_PROCESS", columnNames = { "PROCESS_ID", "NAME" }) })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue(value = "V")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class CurrentVariable<V> extends BaseVariable<CurrentProcess, V> {

    protected Long id;
    private String name;
    private CurrentProcess process;

    public CurrentVariable() {
    }

    @Override
    @Transient
    public boolean isArchive() {
        return false;
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_VARIABLE", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    @Override
    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    @Override
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    @Column(name = "NAME", length = 1024)
    @Index(name = "IX_VARIABLE_NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(targetEntity = CurrentProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", nullable = false)
    @ForeignKey(name = "FK_VARIABLE_PROCESS")
    @Index(name = "IX_VARIABLE_PROCESS")
    public CurrentProcess getProcess() {
        return process;
    }

    public void setProcess(CurrentProcess process) {
        this.process = process;
    }

    private CurrentVariableLog getLog(Object oldValue, Object newValue, VariableDefinition variableDefinition) {
        if (oldValue == null) {
            return new CurrentVariableCreateLog(this, newValue, variableDefinition);
        } else if (newValue == null) {
            return new CurrentVariableDeleteLog(this);
        } else {
            return new CurrentVariableUpdateLog(this, oldValue, newValue, variableDefinition);
        }
    }

    public boolean supports(Object value) {
        if (value == null) {
            return false;
        }
        return converter != null && converter.supports(value);
    }

    public CurrentVariableLog setValue(ExecutionContext executionContext, Object newValue, VariableDefinition variableDefinition) {
        Object newStorableValue;
        if (supports(newValue)) {
            if (converter != null && converter.supports(newValue)) {
                newStorableValue = converter.convert(executionContext, this, newValue);
            } else {
                converter = null;
                newStorableValue = newValue;
            }
        } else {
            throw new InternalApplicationException(this + " does not support new value '" + newValue + "' of '" + newValue.getClass() + "'");
        }
        Object oldValue = getStorableValue();
        if (newValue == null || converter instanceof SerializableToByteArrayConverter) {
            setStringValue(null);
        } else {
            setStringValue(toString(newValue, variableDefinition));
        }
        if (converter != null && oldValue != null) {
            oldValue = converter.revert(oldValue);
        }
        setStorableValue((V) newStorableValue);
        return getLog(oldValue, newValue, variableDefinition);
    }

    public String toString(Object value, VariableDefinition variableDefinition) {
        String string;
        if (SystemProperties.isV3CompatibilityMode() && value != null && String[].class == value.getClass()) {
            string = Arrays.toString((String[]) value);
        } else if (value instanceof Executor) {
            string = ((Executor) value).getLabel();
        } else {
            string = String.valueOf(value);
        }
        string = Utils.getCuttedString(string, getMaxStringSize());
        return string;
    }
}