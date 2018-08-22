package ru.runa.wfe.audit;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.Index;

@Entity
@Table(name = "ARCHIVED_LOG")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue(value = "0")
public abstract class ArchivedProcessLog extends BaseProcessLog {

    private Long id;
    private Long processId;

    @Override
    @Transient
    public boolean isArchive() {
        return true;
    }

    /**
     * NOT generated, id values are preserved when moving row to archive.
     */
    @Override
    @Id
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    @Column(name = "PROCESS_ID", nullable = false)
    @Index(name = "IX_ARCH_LOG_PROCESS")
    public Long getProcessId() {
        return processId;
    }

    @Override
    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    @Transient
    @Override
    public Long getTokenId() {
        return null;
    }

    @Override
    public void setTokenId(Long tokenId) {
        throw new IllegalAccessError();
    }
}