# Plumber

"Tell the plumber what your pipeline should look like but not how to build it."

Plumber aims to be a complimentary DSL you can use in your pipeline scripts that is declarative in nature. 

## Core principles

* Declarative as much as possible (state what, not how)
* Build on pipeline script
* Allow Jenkins/plumber to work out how to parallelise phases (like stages)
* Work with pipeline script (you can embed pipeline snippets)
* Work with Jenkinsfile and pipeline-as-code (remember, it is DSL)
* Convention over configuration
* Pipelines defined as data 
* Global configuration, over-ride per phase as needed
* Sensible default behavior 

## Design

See [the design doc here](DESIGN.md)

## Status 

Experimental and very much a work in progress. Keep an eye on this space. 

## Hello, world

```
plumber {
    phase {
        name "obtain pants"
        action {
            script "echo hello"
        }
    }
    phase {
        name "left leg"
        action {
            script "echo left"
        }
        after "obtain pants"
    }
    phase {
        name "right leg"
        action {
            script "echo right"
        }
        after "obtain pants"
    }
    
}
```

In this example, the final two "phases" can be executed in parallel, as they have a common previous phase they depend on: `after "obtain pants"`. `script` is `sh` on unix, and `bat` on windows (plumber takes care of that as well). 

You can embed pipeline script by using `pipeline` instead of action (there are many actions built in covering high level tasks).

## More examples and docs

Take a look in `src/test/resources` for a whole chunk of samples of things you can do. 
