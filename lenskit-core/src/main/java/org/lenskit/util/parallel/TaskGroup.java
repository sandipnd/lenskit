/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.lenskit.util.parallel;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

/**
 * A group of tasks to be executed in a fork-join tree.
 */
public class TaskGroup extends RecursiveAction {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(TaskGroup.class);

    private boolean parallel;
    private Deque<ForkJoinTask<?>> tasks;

    /**
     * Create a new task group.
     * @param par `true` to execute the subtasks in parallel.
     */
    public TaskGroup(boolean par) {
        parallel = par;
        tasks = new LinkedList<>();
    }

    /**
     * Query whether the subtasks will be run in parallel.
     * @return `true` if the subtasks are run in parallel.
     */
    public boolean isParallel() {
        return parallel;
    }

    /**
     * Add a task to be executed.
     * @param task The task to execute.
     */
    public void addTask(ForkJoinTask<?> task) {
        Preconditions.checkState(!isDone(), "task already completed");
        tasks.add(task);
    }

    @Override
    protected void compute() {
        if (parallel) {
            logger.debug("running {} tasks in parallel", tasks.size());
            invokeAll(tasks);
        } else {
            logger.debug("running {} tasks in sequence", tasks.size());
            while (!tasks.isEmpty()) {
                ForkJoinTask<?> task = tasks.removeFirst();
                task.invoke();
            }
        }
    }
}
