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
import io.jenkins.plugins.pipelineaction.PipelineAction
import org.jenkinsci.plugins.plumber.Utils
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted
import org.jenkinsci.plugins.workflow.cps.CpsGroovyShell
import org.jenkinsci.plugins.workflow.cps.CpsScript

import static org.jenkinsci.plugins.plumber.Utils.getTabs
import static org.jenkinsci.plugins.plumber.Utils.toArgForm

@ToString
@EqualsAndHashCode
@SuppressFBWarnings(value="SE_NO_SERIALVERSIONID")
public class Phase extends AbstractPlumberModel {
    @Whitelisted
    String name

    @Whitelisted
    List<String> before = []

    @Whitelisted
    List<String> after = []

    @Whitelisted
    Action action

    @Whitelisted
    String label

    @Whitelisted
    String dockerImage

    @Whitelisted
    List<String> archiveDirs = []

    @Whitelisted
    List<String> stashDirs = []

    @Whitelisted
    List<Unstash> unstash = []

    @Whitelisted
    Notifications notifications

    @Whitelisted
    Map<String,String> env = [:]

    @Whitelisted
    Boolean treatUnstableAsSuccess

    @Whitelisted
    Matrix matrix

    @Whitelisted
    List<SCM> scms = []

    @Whitelisted
    List<Reporter> reporters = []

    @Whitelisted
    Boolean skipSCM

    @Whitelisted
    PipelineClosureWrapper pipeline

    // TODO: Decide whether to default to clean workspaces. Currently *not*.
    @Whitelisted
    Boolean clean

    public Phase() {

    }

    public Phase(Map<String, Object> args) {
        if (args != null) {
            validateMapFields(args.keySet())
            this.name = args.name
            if (args.containsKey("before")) {
                if (args.before instanceof String) {
                    this.before = [(String)args.before]
                } else if (args.before instanceof List) {
                    this.before = (List<String>)args.before
                }
            }
            if (args.containsKey("after")) {
                if (args.after instanceof String) {
                    this.after = [(String)args.after]
                } else if (args.after instanceof List) {
                    this.after = (List<String>)args.after
                }
            }
            if (args.containsKey("action") && args.action instanceof Map) {
                this.action = new Action((Map<String,Object>)args.action)
            }
            this.label = args.label
            this.dockerImage = args.dockerImage
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
            if (args.containsKey("scm") && args.scm instanceof List) {
                args.scm?.each { Map<String,Object> scmMap ->
                    this.scms.add(new SCM(scmMap))
                }
            }
            if (args.containsKey("reporters") && args.reporters instanceof List) {
                args.reporters?.each { Map<String,Object> reporterMap ->
                    this.reporters.add(new Reporter(reporterMap))
                }
            }
            if (args.containsKey("unstash")) {
                if (args.unstash instanceof List) {
                    args.unstash?.each { Map<String, Object> unstashMap ->
                        this.unstash.add(new Unstash(unstashMap))
                    }
                } else if (args.unstash instanceof Map) {
                    this.unstash.add(new Unstash((Map<String,Object>)args.unstash))
                } else if (args.unstash instanceof String) {
                    this.unstash.add(new Unstash([fromPhase: args.unstash]))
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
            if (args.containsKey("matrix") && args.matrix instanceof Map) {
                this.matrix = new Matrix((Map<String,Object>)args.matrix)
            }
            if (args.containsKey("treatUnstableAsSuccess")) {
                this.treatUnstableAsSuccess = args.treatUnstableAsSuccess
            }
            if (args.containsKey("skipSCM")) {
                this.skipSCM = args.skipSCM
            }
            if (args.containsKey("clean")) {
                this.clean = args.clean
            }
            if (args.containsKey("pipeline")) {
                this.pipeline = new PipelineClosureWrapper((String)args.pipeline)
            }
        }
    }

    @Whitelisted
    Phase name(String val) {
        fieldVal("name", val)
    }

    @Whitelisted
    Phase label(String val) {
        fieldVal("label", val)
    }

    @Whitelisted
    Phase dockerImage(String val) {
        fieldVal("dockerImage", val)
    }

    @Whitelisted
    Phase before(String val) {
        addValToList("before", val)
    }

    @Whitelisted
    Phase before(List<String> val) {
        fieldVal("before", val)
    }

    @Whitelisted
    Phase before(String... val) {
        fieldVal("before", val)
    }

    @Whitelisted
    Phase after(String val) {
        addValToList("after", val)
    }

    @Whitelisted
    Phase after(List<String> val) {
        fieldVal("after", val)
    }

    @Whitelisted
    Phase after(String... val) {
        fieldVal("after", val)
    }

    @Whitelisted
    Phase archiveDir(String val) {
        addValToList("archiveDirs", val)
    }

    @Whitelisted
    Phase archiveDirs(List<String> val) {
        fieldVal("archiveDirs", val)
    }

    @Whitelisted
    Phase archiveDirs(String... val) {
        fieldVal("archiveDirs", val)
    }

    @Whitelisted
    Phase stashDir(String val) {
        addValToList("stashDirs", val)
    }

    @Whitelisted
    Phase stashDirs(List<String> val) {
        fieldVal("stashDirs", val)
    }

    @Whitelisted
    Phase stashDirs(String... val) {
        fieldVal("stashDirs", val)
    }

    @Whitelisted
    Phase action(Closure<?> closure) {
        closureVal("action", Action.class, closure)
    }

    @Whitelisted
    Phase action(Action a) {
        fieldVal("action", a)
    }

    @Whitelisted
    Phase matrix(Closure<?> closure) {
        closureVal("matrix", Matrix.class, closure)
    }

    @Whitelisted
    Phase matrix(Matrix m) {
        fieldVal("matrix", m)
    }

    @Whitelisted
    Phase notifications(Closure<?> closure) {
        closureVal("notifications", Notifications.class, closure)
    }

    @Whitelisted
    Phase notifications(Notifications n) {
        fieldVal("notifications", n)
    }

    @Whitelisted
    Phase unstash(Closure<?> closure) {
        addClosureValToList("unstash", Unstash.class, closure)
    }

    @Whitelisted
    Phase unstash(Unstash u) {
        addValToList("unstash", u)
    }

    @Whitelisted
    Phase unstash(List<Unstash> u) {
        fieldVal("unstash", u)
    }

    @Whitelisted
    Phase unstash(String from, String dir) {
        addValToList("unstash", new Unstash().fromPhase(from).dir(dir))
    }

    @Whitelisted
    Phase scm(Closure<?> closure) {
        addClosureValToList("scms", SCM.class, closure)
    }

    @Whitelisted
    Phase scm(SCM s) {
        addValToList("scms", s)
    }

    @Whitelisted
    Phase scms(List<SCM> s) {
        fieldVal("scms", s)
    }

    @Whitelisted
    Phase reporter(Closure<?> closure) {
        addClosureValToList("reporters", Reporter.class, closure)
    }

    @Whitelisted
    Phase reporter(Reporter r) {
        addValToList("reporters", r)
    }

    @Whitelisted
    Phase reporters(List<Reporter> r) {
        fieldVal("reporters", r)
    }

    @Whitelisted
    Phase env(Map<String,String> val) {
        fieldVal("env", val)
    }

    @Whitelisted
    Phase treatUnstableAsSuccess(Boolean val) {
        fieldVal("treatUnstableAsSuccess", val)
    }

    @Whitelisted
    Phase skipSCM(Boolean val) {
        fieldVal("skipSCM", val)
    }

    @Whitelisted
    Phase clean(Boolean val) {
        fieldVal("clean", val)
    }

    @Whitelisted
    Phase pipeline(PipelineClosureWrapper val) {
        fieldVal("pipeline", val)
    }

    @Whitelisted
    Phase pipeline(Closure val) {
        fieldVal("pipeline", new PipelineClosureWrapper(val))
    }

    /**
     * Adds an individual key/value pair to the environment.
     *
     * @param key
     * @param value
     */
    public void addToEnv(String key, String value) {
        this.env."${key}" = value
    }

    /**
     * Adds the contents of a map to the environment
     *
     * @param inputMap
     */
    public void addToEnv(Map<String, String> inputMap) {
        this.env.putAll(inputMap)
    }

    /**
     * Generates Pipeline source for this phase and given {@link Root}
     *
     * @param root
     * @param tabsDepth
     * @return Pipeline source code as a list of strings
     */
    public List<String> toPipelineScript(Root root, int tabsDepth) {
        def tabs = getTabs(tabsDepth)
        def overrides = getOverrides(root)
        def overridesFlagsBase = overrides.flags()
        def notifierFlagsBase = overrides.notifications.flags()
        def overridesFlagsString = toArgForm(overridesFlagsBase)

        def lines = []

        lines.addAll(nodeLabelOrDocker(overridesFlagsString, notifierFlagsBase, overrides, 0))

        return lines.collect { "${tabs}${it}" }
    }

    /**
     * Generates the Pipeline source for the actual phase execution.
     *
     * @param overridesFlagsString
     * @param notifierFlagsBase
     * @param overrides
     * @param tabsDepth
     * @return Pipeline source code as a list of strings
     */
    private List<String> phaseExecutionCode(String overridesFlagsString, Map<String,Boolean> notifierFlagsBase,
                                            PhaseOverrides overrides, int tabsDepth) {
        def lines = []
        def tabs = getTabs(tabsDepth)

        def actionConfig = action?.actionConfig?.getMap()

        PipelineAction actionClass
        if (actionConfig != null && !actionConfig.isEmpty() && actionConfig.name != null) {
            actionClass = PipelineAction.getPipelineAction(actionConfig.name)
        }

        lines << "generalNotifier(${toArgForm(name)}, [${overridesFlagsString}], [${toArgForm([before: true] + notifierFlagsBase)}])"
        if (pipeline != null || (actionClass != null && actionClass.usesNode())) {
            if (!overrides.skipSCM) {
                if (!overrides.scms.isEmpty()) {
                    overrides.scms.each { SCM s ->
                        if (overrides.scms.size() > 1 && (s.directory == null || s.directory == "")) {
                            lines << "error('More than one SCM specified, and SCM specified without a directory, so failing.')"
                        } else {
                            lines.addAll(s.toPipelineScript(0))
                        }
                    }
                } else {
                    lines << "checkout scm"
                }
            }

            if (!unstash.isEmpty()) {
                lines.addAll(unstash.collect { it.toPipelineScript(0) })
            }
        }

        lines << "catchError {"
        if (action != null && !action.isEmpty()) {
            lines.addAll(action.toPipelineScript(1))
        } else {
            // TODO: Maybe figure out how to handle Pipeline closures here
        }

        if (pipeline != null || (actionClass != null && actionClass.usesNode())) {
            // Archiving and stashing.
            if (overrides.archiveDirs != null && overrides.archiveDirs != "") {
                lines << "try {"
                lines << "\tarchive ${toArgForm(overrides.archiveDirs)}"
                lines << "} catch (Exception e) {"
                lines << "\techo('Error archiving ${overrides.archiveDirs}, but continuing: \${e}')\n"
                lines << "}"
            }

            if (overrides.stashDirs != null && overrides.stashDirs != "") {
                lines << "try {"
                lines << "\tstash name: ${toArgForm(name)}, includes: ${toArgForm(overrides.stashDirs)}"
                lines << "} catch (Exception e) {"
                lines << "\techo('Error stashing ${overrides.stashDirs}, but continuing: \${e}')\n"
                lines << "}"
            }
            if (!reporters.isEmpty()) {
                reporters.each { r ->
                    lines << "try {"
                    lines.addAll(r.toPipelineScript(1))
                    lines << "} catch (Exception e) {"
                    lines << '\techo("Error running reporter ' + r.name + ' with config ' +
                        r.config.getMap() + ', but continuing: ${e}"'
                    lines << '}'
                }
            }
        }
        lines << "}"

        lines << "generalNotifier(${toArgForm(name)}, [${overridesFlagsString}], [${toArgForm([before: false] + notifierFlagsBase)}])"

        return lines.collect { "${tabs}${it}" }
    }

    /**
     * Generates Pipeline source for environment wrapping and passes onward to phase execution.
     *
     * @param overridesFlagsString
     * @param notifierFlagsBase
     * @param overrides
     * @param tabsDepth
     * @return Pipeline source code as a list of strings
     */
    private List<String> envWrapper(String overridesFlagsString, Map<String,Boolean> notifierFlagsBase,
                                    PhaseOverrides overrides, int tabsDepth) {
        def tabs = getTabs(tabsDepth)
        def lines = []

        def envList = [ "PLUMBER_PHASE=${this.name}".toString() ]

        if (overrides.envList != null && !overrides.envList.isEmpty()) {
            envList.addAll(overrides.envList)
        }

        lines << "withEnv([${toArgForm(envList)}]) {"
        lines.addAll(phaseExecutionCode(overridesFlagsString, notifierFlagsBase, overrides, 1))
        lines << "}"

        return lines.collect { "${tabs}${it}" }
    }

    /**
     * Generates Pipeline source for the node specification section and calls onwards to the environment wrapper.
     *
     * @param overridesFlagsString
     * @param notifierFlagsBase
     * @param overrides
     * @param tabsDepth
     * @return Pipeline source code as a list of Strings.
     */
    private List<String> nodeLabelOrDocker(String overridesFlagsString, Map<String,Boolean> notifierFlagsBase,
                                           PhaseOverrides overrides, int tabsDepth) {
        def lines = []
        def tabs = getTabs(tabsDepth)

        def actionConfig = action?.actionConfig?.getMap()
        PipelineAction actionClass
        if (actionConfig != null && !actionConfig.isEmpty() && actionConfig.name != null) {
            actionClass = PipelineAction.getPipelineAction(actionConfig.name)
        }
        if (actionClass != null && !actionClass.usesNode()) {
            // If we're running an action that doesn't have usesNode()==true, don't wrap in a node.
            lines.addAll(envWrapper(overridesFlagsString, notifierFlagsBase, overrides, 0))
        } else if (label != null) {
            lines << "node('${label}') {"
            if (clean) {
                lines << "\tdeleteDir()"
            }
            lines.addAll(envWrapper(overridesFlagsString, notifierFlagsBase, overrides, 1))
            lines << "}"
        } else if (dockerImage != null) {
            lines << "node('docker') { // TODO: Figure out how we specify the Docker node label"
            lines << "\tdocker.image('${dockerImage}').inside() {"
            lines.addAll(envWrapper(overridesFlagsString, notifierFlagsBase, overrides, 2))
            lines << "\t}"
            lines << "}"
        } else {
            lines << "node {"
            if (clean) {
                lines << "\tdeleteDir()"
            }
            lines.addAll(envWrapper(overridesFlagsString, notifierFlagsBase, overrides, 1))
            lines << "}"
        }

        return lines.collect { "${tabs}${it}" }
    }

    /**
     * Get the archiveDirs, stashDirs, env, scm and notifications for this phase, defaulting to the root versions if
     * not specified here.
     *
     * @param root The Root this phase is in.
     * @return An instance of PhaseOverrides for this root and phase objects.
     */
    @Whitelisted
    public PhaseOverrides getOverrides(Root root) {
        return new PhaseOverrides(root, this)
    }

    /**
     * Wrapper class for overrides of {@link Root} configuration at the {@link Phase} level.
     */
    public static final class PhaseOverrides implements Serializable {
        @Whitelisted
        String archiveDirs
        @Whitelisted
        String stashDirs
        @Whitelisted
        Map<String,String> env = [:]
        @Whitelisted
        Notifications notifications
        @Whitelisted
        Boolean treatUnstableAsSuccess
        @Whitelisted
        Boolean skipSCM
        @Whitelisted
        List<SCM> scms = []
        @Whitelisted
        List<String> envList

        PhaseOverrides(Root root, Phase phase) {
            this.archiveDirs = phase.archiveDirs?.isEmpty() ? root.archiveDirs?.join(',') : phase.archiveDirs?.join(',')

            this.stashDirs = phase.stashDirs?.isEmpty() ? root.stashDirs?.join(',') : phase.stashDirs?.join(',')

            this.env.putAll(root.env)
            this.env.putAll(phase.env)

            if (phase.notifications == null) {
                if (root.notifications != null) {
                    this.notifications = root.notifications
                } else {
                    this.notifications = new Notifications()
                }
            } else {
                this.notifications = phase.notifications

                // Get rid of need to specify configs in overrides.
                if (phase.notifications.configs.isEmpty()) {
                    this.notifications.configs.addAll(root.notifications.configs)
                }
            }

            this.treatUnstableAsSuccess = phase.treatUnstableAsSuccess == null ? root.treatUnstableAsSuccess : phase.treatUnstableAsSuccess

            this.skipSCM = phase.skipSCM == null ? root.skipSCM : phase.skipSCM

            if (phase.scms.isEmpty()) {
                this.scms.addAll(root.scms)
            } else {
                this.scms.addAll(phase.scms)
            }

            // Shortcut to avoid having to do collect in Pipeline script.
            this.envList = this.env.collect { k, v -> "${k}=${v}" }
        }

        /**
         * Gets any boolean fields from this class in a Map form - field name to boolean value.
         *
         * @return Map of string->booleans for flags.
         */
        public Map<String,Boolean> flags() {
            return this.class.declaredFields.findAll { !it.synthetic && it.type == Boolean.class }.collectEntries { t ->
                [(t.name): this."${t.name}"]
            }
        }

    }

    private PipelineClosureWrapper validatedInlinePipeline(String inlinePipeline) {
        Closure argClosure = (Closure) new GroovyShell().evaluate("{ -> ${inlinePipeline} }")
        def validator = new PipelineScriptValidator()
        argClosure.delegate = validator
        argClosure.resolveStrategy = Closure.DELEGATE_ONLY
        argClosure.call()

        if (!validator.invalidStepsUsed.isEmpty()) {
            throw new IllegalArgumentException("Illegal Pipeline steps used in inline Pipeline - ${validator.invalidStepsUsed.join(', ')}")
        } else {
            return new PipelineClosureWrapper((Closure) new GroovyShell().evaluate("{ -> ${inlinePipeline} }"))
        }

    }

    static final int serialVersionUID = 1L

}
