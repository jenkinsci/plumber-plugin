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
import org.jenkinsci.plugins.plumber.PlumberDependencyGraph
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted

@ToString
@EqualsAndHashCode
@SuppressFBWarnings(value="SE_NO_SERIALVERSIONID")
public class Root extends AbstractPlumberModel {

    @Whitelisted
    List<Phase> phases = []

    @Whitelisted
    Map<String,String> env = [:]

    @Whitelisted
    Notifications notifications

    @Whitelisted
    List<String> archiveDirs = []

    @Whitelisted
    List<String> stashDirs = []

    @Whitelisted
    Boolean treatUnstableAsSuccess = false

    @Whitelisted
    Boolean debug = false

    @Whitelisted
    List<SCM> scms = []

    @Whitelisted
    Boolean skipSCM = false

    @Whitelisted
    Integer parallelism = 0

    public Root() {

    }

    public Root(Map<String, Object> args) {
        if (args != null) {
            validateMapFields(args.keySet())

            if (args.containsKey("archiveDirs")) {
                if (args.archiveDirs instanceof String) {
                    this.archiveDirs = [(String)args.archiveDirs]
                } else if (args.archiveDirs instanceof List) {
                    this.archiveDirs = (List<String>)args.archiveDirs
                }
            }
            if (args.containsKey("stashDirs")) {
                if (args.stashDirs instanceof String) {
                    this.stashDirs = [(String)args.stashDirs]
                } else if (args.stashDirs instanceof List) {
                    this.stashDirs = (List<String>)args.stashDirs
                }
            }
            if (args.containsKey("phases") && args.phases instanceof List) {
                args.phases?.each { Map<String,Object> phaseMap ->
                    this.phases.add(new Phase(phaseMap))
                }
            }
            if (args.containsKey("scm") && args.scm instanceof List) {
                args.scm?.each { Map<String,Object> scmMap ->
                    this.scms.add(new SCM(scmMap))
                }
            }
            if (args.containsKey("notifications") && args.notifications instanceof Map) {
                this.notifications = new Notifications((Map<String,Object>) args.notifications)
            }
            if (args.containsKey("env") && args.env instanceof Map) {
                args.env?.each { String k, String v ->
                    this.env.put(k, v)
                }
            }
            if (args.containsKey("treatUnstableAsSuccess")) {
                this.treatUnstableAsSuccess = args.treatUnstableAsSuccess
            }
            if (args.containsKey("debug")) {
                this.debug = args.debug
            }
            if (args.containsKey("skipSCM")) {
                this.skipSCM = args.skipSCM
            }
            if (args.containsKey("parallelism") && args.parallelism instanceof Integer) {
                this.parallelism = (Integer) args.parallelism
            }
        }
    }

    @Whitelisted
    Root phase(Closure<?> closure) {
        addClosureValToList("phases", Phase.class, closure)
    }

    @Whitelisted
    Root phase(Phase p) {
        addValToList("phases", p)
    }

    @Whitelisted
    Root phases(List<Phase> p) {
        fieldVal("phases", p)
    }

    @Whitelisted
    Root scm(Closure<?> closure) {
        addClosureValToList("scms", SCM.class, closure)
    }

    @Whitelisted
    Root scm(SCM s) {
        addValToList("scms", s)
    }

    @Whitelisted
    Root scms(List<SCM> s) {
        fieldVal("scms", s)
    }

    @Whitelisted
    Root env(Map<String,String> val) {
        fieldVal("env", val)
    }

    @Whitelisted
    Root notifications(Closure<?> closure) {
        closureVal("notifications", Notifications.class, closure)
    }

    @Whitelisted
    Root notifications(Notifications n) {
        fieldVal("notifications", n)
    }

    @Whitelisted
    Root archiveDir(String val) {
        addValToList("archiveDirs", val)
    }

    @Whitelisted
    Root archiveDirs(List<String> val) {
        fieldVal("archiveDirs", val)
    }

    @Whitelisted
    Root archiveDirs(String... val) {
        fieldVal("archiveDirs", val)
    }

    @Whitelisted
    Root stashDir(String val) {
        addValToList("stashDirs", val)
    }

    @Whitelisted
    Root stashDirs(List<String> val) {
        fieldVal("stashDirs", val)
    }

    @Whitelisted
    Root stashDirs(String... val) {
        fieldVal("stashDirs", val)
    }

    @Whitelisted
    Root treatUnstableAsSuccess(Boolean val) {
        fieldVal("treatUnstableAsSuccess", val)
    }

    @Whitelisted
    Root debug(Boolean val) {
        fieldVal("debug", val)
    }

    @Whitelisted
    Root skipSCM(Boolean val) {
        fieldVal("skipSCM", val)
    }

    @Whitelisted
    Root parallelism(Integer val) {
        fieldVal("parallelism", val)
    }

    @Whitelisted
    public List<String> toPipelineScript(Boolean forExport = false) {
        def lines = []

        lines << "import io.jenkins.plugins.pipelineaction.PipelineActionType"
        lines << "import org.jenkinsci.plugins.plumber.model.*"
        lines << "import hudson.model.Result"
        lines << ""

        if (!forExport) {
            lines << "def call() {"
        }
        executionSets().eachWithIndex { exSet, idx ->
            lines << "stage '${exSet.stageName}'"
            lines.addAll(parallelizePhases(idx, exSet.phases))
        }
        if (!forExport) {
            lines << "}"
            lines << "return this"
        }

        lines << ""
        lines << Notifications.toPipelineScriptFunction()

        return lines
    }

    private List<String> parallelizePhases(int exSetIndex, List<Phase> phases) {
        def lines = []
        String parallelVar = "parallelSet${exSetIndex}"
        if (phases.size() > 1) {
            lines << "def ${parallelVar} = [:]"
            phases.each { p ->
                lines << "${parallelVar}['${p.name}'] = {"
                lines.addAll(p.toPipelineScript(this, 1))
                lines << "}"

            }
            lines << "parallel ${parallelVar}"
        } else if (!phases.isEmpty()) {
            lines.addAll(phases[0].toPipelineScript(this, 0))
        }
        return lines
    }

    /**
     * Finds a phase with the given name and returns it.
     *
     * @param name
     * @return the Phase matching the name, or null if not found.
     */
    public Phase phaseFromName(String name) {
        if (name != null && name != "") {
            return phases.find { it.name == name }
        } else {
            return null
        }
    }

    /**
     * Returns a list of "execution sets" - groups of 1..n phases to be executed concurrently, with a stage name
     * constructed from the phase names as well.
     *
     * @return A list of maps with "stageName" and "phases" keys.
     */
    @Whitelisted
    public List executionSets() {
        def graph = PlumberDependencyGraph.fromPhaseList(phases)

        def exSets = []

        while (graph.hasMorePhases()) {
            def exSetDetails = [:]
            def exSetPhaseNames = graph.getNextPhases(parallelism)
            exSetDetails.stageName = exSetPhaseNames.join("+")

            exSetDetails.phases = []

            // Look for phases with matrix axes and transform them - add everything else automatically
            exSetPhaseNames.collect { phaseFromName(it) }.each { Phase p ->
                // If no matrix, just add it.
                if (p.matrix == null || p.matrix.matrixCombinations().isEmpty()) {
                    exSetDetails.phases << p
                } else if (p.matrix.matrixCombinations().size() == 1) {
                    // If there's only one set of combinations, add it, with the axes added to the environment.
                    Phase newPhase = p.clone()
                    newPhase.addToEnv(p.matrix.matrixCombinations().first())

                    exSetDetails.phases << newPhase
                } else {
                    p.matrix.matrixCombinations().each { Map<String,String> thisCombo ->
                        Phase newPhase = p.clone()
                        newPhase.addToEnv(thisCombo)
                        newPhase.name = p.name + "+" + thisCombo.collect { k, v ->
                            "${k}=${v}"
                        }.join(",")

                        exSetDetails.phases << newPhase
                    }
                }
            }

            exSets << exSetDetails

            graph.postPhaseProcessing(exSetPhaseNames)
        }

        return exSets
    }

    static final int serialVersionUID = 1L

}
