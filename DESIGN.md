# Why Plumber?
Currently Pipeline script is imperative Groovy (ish) script. This has 2 primary problems: 

1. Cognitive load:  programming a pipeline is a cognitive load which users do not expect. It is marketed as a point of power, but the 
reality is it is too much of a barrier for most users. (user comments like "I tried it; too hard; other plugins didn't work; moved 
on." "The cognitive load of learning a programming language for the pipeline is too high for us", "How do I send an email on failure 
after recording test results" and so on). 
2. Separation of duties: many admins of Jenkins will build pipelines or pieces of pipelines that are then assembled by the development
teams. Global Libraries solve this to a degree but end users still need to provide the glue code around shared functions such as
'node', 'stage', 'parallel'. A declarative approach provides structure for setting up this glue easily and running shared libraries.
  1. Tool-friendliness
  2. it is very very difficult to make graphical editors for a general program.
  3. It is very difficult to visualise an arbitrary pipeline until after it has executed (if not technically impossible as per halting
problem). Even after execution the flow graph could be more complicated than people are used to today. 
  4. Desire for declarative specification of Pipelines - see https://news.ycombinator.com/item?id=11362376, e.g.

# Inspirations for Plumber
* Maven - convention over configuration makes what would otherwise be complicated functionality underneath the covers seem trivial and
consistent. 
  * Standard declarative format for specifying your build.
  * Repeatable and standard workflow within your build - `mvn clean install` will do basically the same thing on any Maven project.
  * Common behaviors are built into the lifecycle and run in a consistent manner. No need to configure All The Things just to run unit
  tests, get Checkstyle results, etc.
* Puppet
  * Most specifically for execution ordering - specifying what this given behavior needs to run before or after allows for 
  constructing a directed acyclic graph of execution order without having to specify everything in the exact lexical order in the 
  code.

# Requirements Plumber must meet
* Declarative
  * Simpler for users, simpler for implementation, able to support bidirectional relationship with UI editor, simpler parallelism.
* Execution on top of Pipeline
  * Plumber is not a new language or a new job type. It lives on top of Pipeline and takes advantage of all existing Pipeline support
  tooling (i.e., stage view, durability, etc)
* Migration path to pure Pipeline
  * Plumber cannot be a dead end - it is intended to serve as an on-ramp to Pipeline, and to cover a significant portion of Pipeline 
  use cases, but it's not intended to expose full Pipeline functionality. So it must have a way to move from defining your Pipeline 
  using Plumber alone to extending into pure Pipeline code for greater power and control.
* Escape hatch to pipeline
  * As plumber runs in pipeline, it needs to be clear how to escape out should you need to, to the full power of pipeline. This should
  be addressed by the inline Pipeline block option for phases.
* Opinionated
  * Default behaviors and configuration if you don't specify otherwise.

# Design
## High level description
* "plumber" step provided by plugin
* One argument to step - configuration in closure/Groovy-builder-style form for plumber
  * Note - other configuration formats are entirely possible. YAML, JSON, you name it.
  * Inline Pipeline code in phases does throw this off a bit since that won't translate easily to a non-Groovy format, but that's 
  solvable.
* Configuration allows specifying:
  * Global configuration that applies across all phases.
    * Currently implemented:
      * Notifications
      * archive/stash directories
      * Debug
      * phase failure handling (limited currently)
      * SCM
      * Environment variables
  * 1..n phases where each phase has either one declarative action or one Pipeline code block that it performs, currently each on a 
  separate node (except for phases with "input" actions, which are special-cased to not require a "node" block).
    * Phases can specify relationships with other phases via "before" and "after" fields.
    * Phases can override global configuration for most things.
    * Phases also can specify the label or docker image to run in.
  * Actions will be covered in more detail in https://github.com/abayer/pipeline-action-plugin
    * Actions are still particularly relevant for input (due to some special logic around phases that are taking input), SCM, 
    notifiers and recorder/reporters - more on that in a later doc.
  * Pipeline code blocks cannot contain "stage", "node", or "parallel" steps to avoid breaking the execution model.
  * Phases are mapped into a graph and then broken up into execution sets - groups of each phase that has no pending dependencies and 
  therefore can be executed at this point.
  * Each execution set corresponds to one stage (or whatever the long-term equivalent is)
    * This will change once stage (or equivalent) can be functionally used within parallel.
  * Each phase in an execution set with more than one phase is executed simultaneously within parallel.
* Actual execution is entirely Pipeline-native - either through an interpreter Pipeline script 
(https://github.com/jenkinsci/plumber-plugin/blob/master/src/main/resources/org/jenkinsci/plugins/plumber/PlumberInterpreter.groovy)
or optionally through generating a Pipeline script that represents the actual execution, printing it out to a file, loading that file 
and executing it.
  * Outputting Pipeline is largely here for "Export to Pipeline" option to allow users to easily move their build from Plumber to pure
  Pipeline. The running of generated code will probably be removed. For that matter, the export option may well be removed.
* Plumber step can either be the only thing invoked in a Jenkinsfile, or other Pipeline code can be invoked before and after the 
plumber invocation. It just can't be run within a node block itself and can't run arbitrary code as actions to keep declarative 
behavior and stability.

# Plumber model
* 1..n phases
* Each phase has exactly one action or Pipeline code block, which will be performed during the execution of that phase.
* A phase can override top-level configuration for notifications, label/docker image to run in, etc.
* Phase execution order is not determined by lexical order in the configuration, but instead by dependency order, using "before" and 
"after" configuration values that can be set on each phase.
* Phases without unsatisfied/unexecuted dependencies will all be invoked in the same parallel execution, followed by the next batch of
such phases after this set has been marked as executed, etc.
* In a linear pipeline, each phase corresponds directly to one stage (or "label" or whatever the eventual block-scoped way of 
logically grouping steps is).
  * In the case of parallel execution, with the current stage implementation, each "execution set" of phases (described above) will be
  within a single stage. Hopefully we'll eventually be able to use the planned/theoretical block-scoped logical grouping for every 
  phase regardless of parallelism.
* Each phase runs in a new node block (with the "input" caveat mentioned above).
  * Label or docker image can be specified.
  * Workspace contents (or specific directories) can be stashed and unstashed between phases.
* Notifications can be configured at top-level or per-phase.
  * Notifications have some general configuration (should every phase trigger a notification, should notifications happen before phase
  execution, should they be sent on failure and/or success, etc) and a notifier type with its own configuration.
  * Notifiers are a special case of `PipelineAction`s for the mailer, HipChat, Slack, whatever integration, also contributed by plugins 
  like other actions.
* SCM - by default, will `checkout scm` in each phase.
  * SCM can be overridden at top-level or per-phase.
  * Multiple repos can be checked out at once in different directories.
  * SCM checkout can be skipped as well.
* Reporters - a.k.a., "Recorder" plugins - can be specified via a `PipelineAction` for that plugin, i.e., JUnit, static analysis, etc, 
and will be run post-action/pipeline block for a phase.

# Some samples, example syntax, tests, etc
Tests in source repo: https://github.com/jenkinsci/plumber/tree/master/src/test/resources
