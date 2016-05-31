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

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted

import static org.jenkinsci.plugins.plumber.Utils.getTabs
import static org.jenkinsci.plugins.plumber.Utils.toArgForm

@ToString
@EqualsAndHashCode
@SuppressFBWarnings(value="SE_NO_SERIALVERSIONID")
public class Unstash extends AbstractPlumberModel {
    @Whitelisted
    String fromPhase

    @Whitelisted
    String dir

    public Unstash() {

    }

    public Unstash(Map<String,Object> args) {
        if (args != null) {
            validateMapFields(args.keySet())

            this.fromPhase = args.fromPhase
            this.dir = args.dir
        }
    }

    @Whitelisted
    Unstash fromPhase(String val) {
        fieldVal("fromPhase", val)
    }

    @Whitelisted
    Unstash dir(String val) {
        fieldVal("dir", val)
    }

    public List<String> toPipelineScript(int tabsDepth) {
        def tabs = getTabs(tabsDepth)

        def lines = []

        def unstashDir
        if (dir == null || dir == "") {
            unstashDir = "pwd()"
        } else {
            unstashDir = "'${dir}'"
        }

        lines << "unstash {\n"
        lines << "\tfromPhase ${toArgForm(fromPhase)}\n"
        lines << "\tdir ${unstashDir}\n"
        lines << "}\n"
        return lines.collect { "${tabs}${it}" }
    }

    static final int serialVersionUID = 1L

}
