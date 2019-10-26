package com.legooframework.model.autotask.entity;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TaskProcessAgg {

    private final TaskSourceEntity taskSource;
    private final List<TaskProcessTemp> taskProcessTemps;

    public TaskProcessAgg(TaskSourceEntity taskSource, List<TaskProcessTemp> taskProcessTemps) {
        this.taskSource = taskSource;
        if (this.taskSource.isError()) {
            this.taskProcessTemps = null;
        } else {
            this.taskProcessTemps = taskProcessTemps;
            if (CollectionUtils.isNotEmpty(taskProcessTemps)) {
                List<String> uuids = taskProcessTemps.stream().map(TaskProcessTemp::getUuid).collect(Collectors.toList());
                this.taskSource.finshed(Joiner.on(',').join(uuids));
            } else {
                this.taskSource.finshed("finshed");
            }
        }
    }

    public TaskSourceEntity getTaskSource() {
        return taskSource;
    }

    public Optional<List<TaskExecuteEntity>> getExecTasks() {
        if (this.taskSource.isError() || CollectionUtils.isEmpty(taskProcessTemps)) return Optional.empty();
        List<TaskExecuteEntity> taskExecutes = Lists.newArrayListWithCapacity(taskProcessTemps.size());
        if (CollectionUtils.isNotEmpty(this.taskProcessTemps)) {
            this.taskProcessTemps.forEach(task -> {
                TaskExecuteEntity execute = new TaskExecuteEntity(taskSource, task);
                taskExecutes.add(execute);
            });
        }
        return Optional.ofNullable(CollectionUtils.isNotEmpty(taskExecutes) ? taskExecutes : null);
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("taskSource", taskSource)
                .add("taskProcessTemps", taskProcessTemps == null ? null : taskProcessTemps.size())
                .toString();
    }
}
