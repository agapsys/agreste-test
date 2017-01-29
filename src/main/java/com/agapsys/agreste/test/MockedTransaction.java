/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
 *
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
 */

package com.agapsys.agreste.test;

import com.agapsys.agreste.JpaTransaction;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class MockedTransaction implements JpaTransaction {
    private final List<Runnable> commitQueue = new LinkedList<>();
    private final List<Runnable> rollbackQueue = new LinkedList<>();

    private EntityManager em;
    private EntityTransaction et;

    public MockedTransaction(EntityManager em) {
        this.em = em;
        this.et = em.getTransaction();

        if (!this.et.isActive())
            this.et.begin();
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void invokeAfterCommit(Runnable runnable) {
        if (runnable == null) throw new IllegalArgumentException("Null runnable");
        commitQueue.add(runnable);
    }

    @Override
    public void invokeAfterRollback(Runnable runnable) {
        if (runnable == null) throw new IllegalArgumentException("Null runnable");
        rollbackQueue.add(runnable);
    }

    private void close(boolean commit) {
        if (commit) {
            et.commit();
        } else {
            et.rollback();
        }

        et = em.getTransaction();
    }

    private void processQueue(List<Runnable> queue) {
        for (Runnable runnable : queue) {
            runnable.run();
        }

        queue.clear();
    }

    public void begin() {
        if (!et.isActive())
            et.begin();
    }

    public void rollback() {
        close(false);
        processQueue(rollbackQueue);
    }

    public void commit() {
        close(true);
        processQueue(commitQueue);
    }
}
