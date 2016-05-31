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

import groovy.json.JsonOutput
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted
import org.yaml.snakeyaml.Yaml

@SuppressFBWarnings(value="SE_NO_SERIALVERSIONID")
public class PlumberConfig {
    private Root root

    @Whitelisted
    public PlumberConfig() {
        // Empty constructor basically just for whitelisting.
    }

    @Whitelisted
    public PlumberConfig(Root root) {
        this.root = root
    }

    @Whitelisted
    public void fromString(String str) {
        fromClosure((Closure)new GroovyShell().evaluate(str))
    }

    @Whitelisted
    public void fromClosure(Closure<?> closure) {
        root = new Root()
        closure.delegate = root
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()
    }

    @Whitelisted
    public void fromMap(Map<String,Object> map) {
        root = new Root(map)
    }

    @Whitelisted
    public void fromYaml(String str) {
        Yaml yaml = new Yaml()

        Map<String, Object> plumberYaml = (Map<String, Object>) yaml.load(str)

        root = new Root(plumberYaml)
    }

    @Whitelisted
    public Root getConfig() {
        return root
    }

    public String toJson() {
        return JsonOutput.prettyPrint(JsonOutput.toJson(root))
    }

}
