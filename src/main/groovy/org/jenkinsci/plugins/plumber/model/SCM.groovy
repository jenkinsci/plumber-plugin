/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.plumber.model

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted

import static org.jenkinsci.plugins.plumber.Utils.getTabs
import static org.jenkinsci.plugins.plumber.Utils.toArgForm

@ToString
@EqualsAndHashCode
@SuppressFBWarnings(value="SE_NO_SERIALVERSIONID")
public class SCM extends AbstractPlumberModel {
    @Whitelisted
    String name

    @Whitelisted
    MappedClosure config

    @Whitelisted
    String directory

    public SCM() {

    }

    public SCM(Map<String,Object> args) {
        if (args != null) {
            validateMapFields(args.keySet())

            this.name = args.name
            if (args.containsKey("dir") && args.dir != null) {
                this.directory = args.dir
            }
            if (args.containsKey("config") && args.config instanceof Map) {
                this.config = new MappedClosure((Map<String, Object>) args.config)
            }

        }
    }

    @Whitelisted
    SCM name(String val) {
        fieldVal("name", val)
    }

    @Whitelisted
    SCM dir(String val) {
        fieldVal("directory", val)
    }

    @Whitelisted
    SCM config(Closure<?> closure) {
        closureVal("config", MappedClosure.class, closure)
    }

    @Whitelisted
    SCM config(MappedClosure m) {
        fieldVal("config", m)
    }

    public List<String> toPipelineScript(int tabsDepth) {
        def tabs = getTabs(tabsDepth)

        def lines = []

        if (config.getMap() != null && !config.getMap().isEmpty()) {
            def argMap = [:]
            argMap.putAll(config.getMap())

            argMap.put("name", name)
            def indent = ""
            if (directory != null) {
                lines << "dir('${directory}') {"
                indent = "\t"
            }
            lines << "${indent}runPipelineAction(PipelineActionType.SCM, [${toArgForm(argMap)}])"
            if (directory != null) {
                lines << "}"
            }
        }

        return lines.collect { "${tabs}${it}" }
    }

}
