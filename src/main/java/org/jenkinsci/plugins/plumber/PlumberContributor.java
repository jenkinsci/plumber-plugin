/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.plumber;

import hudson.ExtensionList;
import hudson.ExtensionPoint;

import javax.annotation.Nonnull;

public abstract class PlumberContributor implements ExtensionPoint {

    /**
     * The name of the contributor. Should be unique.
     * TODO: Figure out how to enforce uniqueness?
     * @return The name of the contributor.
     */
    public abstract @Nonnull String getName();

    // TODO: How do we inject the contributor? GlobalVariables have CpsScript passed to them, but since we'll be running
    // as a Step, we won't have that...so how do we parse it as Pipeline DSL?

    /**
     * Returns all the registered {@link PlumberContributor}s.
     */
    public static ExtensionList<PlumberContributor> all() {
        return ExtensionList.lookup(PlumberContributor.class);
    }

}