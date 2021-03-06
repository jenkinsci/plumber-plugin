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
package org.jenkinsci.plugins.plumber;

import hudson.Extension;
import org.jenkinsci.plugins.plumber.model.MethodMissingWrapperWhiteList;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.ProxyWhitelist;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.StaticWhitelist;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;

import javax.annotation.Nonnull;
import java.io.IOException;

@Extension
public class PlumberStepDSL extends GlobalVariable {
    @Override
    @Nonnull
    public String getName() {
        return "plumber";
    }

    @Override
    @Nonnull
    public Object getValue(@Nonnull CpsScript script) throws Exception {
        // Make sure we've already loaded ClosureModelTranslator or load it now.
        script.getClass().getClassLoader().loadClass("org.jenkinsci.plugins.plumber.ClosureModelTranslator");

        return script.getClass()
                .getClassLoader()
                .loadClass("org.jenkinsci.plugins.plumber.PlumberInterpreter")
                .getConstructor(CpsScript.class)
                .newInstance(script);
    }


    @Extension
    public static class PlumberWhiteList extends ProxyWhitelist {
        public PlumberWhiteList() throws IOException {
            super(new MethodMissingWrapperWhiteList(), new StaticWhitelist(
                    "method java.util.Map$Entry getKey",
                    "method java.util.Map$Entry getValue",
                    "staticField java.lang.System err",
                    "method java.io.PrintStream println java.lang.String",
                    "method java.util.Map containsKey java.lang.Object",
                    "method java.util.Map isEmpty",
                    "staticField hudson.model.Result UNSTABLE",
                    "staticField hudson.model.Result FAILURE",
                    "method java.util.Map putAll java.util.Map",
                    "staticMethod org.codehaus.groovy.runtime.ScriptBytecodeAdapter compareGreaterThan java.lang.Object java.lang.Object",
                    "staticMethod org.codehaus.groovy.runtime.DefaultGroovyMethods leftShift java.util.Collection java.lang.Object",
                    "staticMethod org.codehaus.groovy.runtime.DefaultGroovyMethods size java.lang.Object[]",
                    "staticMethod hudson.model.Result fromString java.lang.String",
                    "method hudson.model.Result isBetterThan hudson.model.Result",
                    "method java.util.Collection addAll java.util.Collection"
            ));
        }
    }

}
