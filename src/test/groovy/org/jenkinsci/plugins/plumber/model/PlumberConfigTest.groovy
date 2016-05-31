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

import org.junit.Test
import static org.junit.Assert.*;

class PlumberConfigTest {
    @Test
    public void testBasicConfig() {
        def config = new PlumberConfig()
        def c = {
            phase {
                name "foo"
                action {
                    script "echo hello"
                }
            }
        }

        config.fromClosure(c)
        def root = config.getConfig()

        assertTrue(root.phases.size() == 1)
    }

    @Test
    public void testBasicYamlConfig() {
        def config = new PlumberConfig()
        def y = """
phases:
  - name: 'foo'
    action:
      script: 'echo hello'
"""
        config.fromYaml(y)
        def root = config.getConfig()

        assertTrue(root.phases.size() == 1)
    }

    @Test
    public void testPhaseOverrides() {
        def plumberConfig = new PlumberConfig()

        def c = {
            env "foo": "bar", "boo": "far"

            archiveDirs "one/dir", "second/dir"

            stashDirs "stash/one", "stash/two"

            notifications {
                config {
                    name "email"
                    to "some@one.com"
                }
                onSuccess true
            }

            treatUnstableAsSuccess true

            phase {
                name "overridePhase"

                env "foo": "banana", "pants": "trousers"

                archiveDirs "third/dir"

                treatUnstableAsSuccess false

                notifications {
                    config {
                        name "email"
                        to "someone@else.com"
                    }
                    onSuccess false
                }

                action {
                    script "echo hello"
                }
            }
        }

        plumberConfig.fromClosure(c)

        def root = plumberConfig.getConfig()

        def phase = root.phases.first()

        def overrides = phase.getOverrides(root)

        assertEquals("third/dir", overrides.archiveDirs)
        assertEquals("stash/one,stash/two", overrides.stashDirs)
        assertEquals("banana", overrides.env?.foo)
        assertEquals("trousers", overrides.env?.pants)
        assertEquals("far", overrides.env?.boo)
        assertEquals(false, overrides.notifications?.onSuccess)
        assertEquals("someone@else.com", overrides.notifications?.configs?.find { it.name == "email" }?.to)
        assertFalse(overrides.treatUnstableAsSuccess)
    }

    @Test
    public void testYamlPhaseOverrides() {
        def plumberConfig = new PlumberConfig()

        def y = """
env:
  foo: bar
  boo: far

archiveDirs:
  - one/dir
  - second/dir

stashDirs:
  - stash/one
  - stash/two

notifications:
  configs:
    - name: email
      to: some@one.com
  onSuccess: true

treatUnstableAsSuccess: true

phases:
  - name: overridePhase
    env:
      foo: banana
      pants: trousers
    archiveDirs:
      - third/dir
    treatUnstableAsSuccess: false
    notifications:
      configs:
        - name: email
          to: someone@else.com
      onSuccess: false
    action:
      script: 'echo hello'
"""

        plumberConfig.fromYaml(y)

        def root = plumberConfig.getConfig()

        def phase = root.phases.first()

        def overrides = phase.getOverrides(root)

        assertEquals("third/dir", overrides.archiveDirs)
        assertEquals("stash/one,stash/two", overrides.stashDirs)
        assertEquals("banana", overrides.env?.foo)
        assertEquals("trousers", overrides.env?.pants)
        assertEquals("far", overrides.env?.boo)
        assertEquals(false, overrides.notifications?.onSuccess)
        assertEquals("someone@else.com", overrides.notifications?.configs?.find { it.name == "email" }?.to)
        assertFalse(overrides.treatUnstableAsSuccess)
    }

    @Test
    public void testAction() {
        def config = new PlumberConfig()
        def c = {
            phase {
                name "foo"
                action {
                    name "simpleEcho"
                    pants 'trousers'
                    shirts 'polos'
                }
            }
        }

        config.fromClosure(c)
        def root = config.getConfig()

        assertTrue(root.phases.size() == 1)
        def actionConfig = root.phases[0].action
        assertNotNull(actionConfig)
        assertNotNull(actionConfig.getMap())

        assertNull(root.phases[0].pipeline)
        assertEquals("trousers", actionConfig.getMap().pants)
        assertEquals("polos", actionConfig.getMap().shirts)
        assertEquals("simpleEcho", actionConfig.getMap().name)
    }

    @Test
    public void testYamlAction() {
        def config = new PlumberConfig()
        def y = """
phases:
  - name: foo
    action:
      name: simpleEcho
      pants: trousers
      shirts: polos
"""
        config.fromYaml(y)
        def root = config.getConfig()

        assertTrue(root.phases.size() == 1)
        def actionConfig = root.phases[0].action
        assertNotNull(actionConfig)
        assertNotNull(actionConfig.getMap())

        assertNull(root.phases[0].pipeline)
        assertEquals("trousers", actionConfig.getMap().pants)
        assertEquals("polos", actionConfig.getMap().shirts)
        assertEquals("simpleEcho", actionConfig.getMap().name)
    }

    @Test
    public void testMatrix() {
        def config = new PlumberConfig()
        def c = {
            phase {
                name "foo"
                matrix {
                    axes {
                        'FOO' "bar", "baz"
                    }
                }
                action {
                    script "echo hello"
                }
            }
        }

        config.fromClosure(c)
        def root = config.getConfig()

        assertTrue(root.phases.size() == 1)

        def executionSets = root.executionSets()

        assertTrue(executionSets.size() == 1)

        def phasesFromExSet = executionSets.first().phases

        assertTrue(phasesFromExSet.size() == 2)

        assertTrue(phasesFromExSet.any { Phase p ->
            p.name == "foo+FOO=bar" && p.env != null && p.env.get("FOO") != null && p.env.get("FOO") == "bar"
        })
        assertTrue(phasesFromExSet.any { Phase p ->
            p.name == "foo+FOO=baz" && p.env != null && p.env.get("FOO") != null && p.env.get("FOO") == "baz"
        })
    }

    @Test
    public void testYamlMatrix() {
        def config = new PlumberConfig()
        def y = """
phases:
  - name: foo
    matrix:
      axes:
        FOO:
          - bar
          - baz
    action:
      script "echo hello"
"""
        config.fromYaml(y)

        def root = config.getConfig()

        assertTrue(root.phases.size() == 1)

        def executionSets = root.executionSets()

        assertTrue(executionSets.size() == 1)

        def phasesFromExSet = executionSets.first().phases

        assertTrue(phasesFromExSet.size() == 2)

        assertTrue(phasesFromExSet.any { Phase p ->
            p.name == "foo+FOO=bar" && p.env != null && p.env.get("FOO") != null && p.env.get("FOO") == "bar"
        })
        assertTrue(phasesFromExSet.any { Phase p ->
            p.name == "foo+FOO=baz" && p.env != null && p.env.get("FOO") != null && p.env.get("FOO") == "baz"
        })
    }

    @Test
    public void testInlinePipeline() {
        def config = new PlumberConfig()
        def c = {
            phase {
                name "foo"
                pipeline {
                    sh "Tada"
                }
            }
        }

        config.fromClosure(c)
        def root = config.getConfig()

        assertTrue(root.phases.size() == 1)
        Phase p = root.phases[0]

        assertNull(p.action)
        assertNotNull(p.pipeline)
        assertTrue(p.pipeline.closure instanceof Closure)
    }

    @Test
    public void testYamlInlinePipeline() {
        def config = new PlumberConfig()
        def y = """
phases:
  - name: foo
    pipeline: sh "Tada"
"""
        config.fromYaml(y)

        def root = config.getConfig()

        assertTrue(root.phases.size() == 1)
        Phase p = root.phases[0]

        assertNull(p.action)
        assertNotNull(p.pipeline)
        assertTrue(p.pipeline.closure instanceof Closure)
    }
}
