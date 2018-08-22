package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.user.Executor;

/**
 * Logging swimlane assignment.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "4")
public class CurrentSwimlaneAssignLog extends CurrentProcessLog implements SwimlaneAssignLog {
    private static final long serialVersionUID = 1L;

    public CurrentSwimlaneAssignLog() {
    }

    public CurrentSwimlaneAssignLog(Swimlane swimlane, Executor newExecutor) {
        addAttribute(ATTR_MESSAGE, swimlane.getName());
        if (swimlane.getExecutor() != null) {
            addAttribute(ATTR_OLD_VALUE, swimlane.getExecutor().getName());
        }
        if (newExecutor != null) {
            addAttribute(ATTR_NEW_VALUE, newExecutor.getName());
        }
        setSeverity(Severity.INFO);
    }
}