# cljbox2d

<!-- badges -->
[![cljdoc badge](https://cljdoc.org/badge/com.lambdaisland/cljbox2d)](https://cljdoc.org/d/com.lambdaisland/cljbox2d) [![Clojars Project](https://img.shields.io/clojars/v/com.lambdaisland/cljbox2d.svg)](https://clojars.org/com.lambdaisland/cljbox2d)
<!-- /badges -->

Idiomatic and cross-platform Clojure version of the Box2D physics engine API. Wraps jBox2D (Clojure) and Planck.js (ClojureScript).

Created for use with Quil, but can be used independently or with other rendering engines. We do include some helpers to integrate cljbox2d with Quil, see `lambdaisland.cljbox2d.quil`

Work in progress. See the demo directory for examples.

## Demos

Run these commands to see cljbox2d in action:

```
clojure -Sdeps '{:deps {com.lambdaisland/cljbox2d {:mvn/version "0.2.12"} quil/quil {:mvn/version "4.0.0-SNAPSHOT"}}}' -M -m lambdaisland.cljbox2d.demo.simple-shapes
clojure -Sdeps '{:deps {com.lambdaisland/cljbox2d {:mvn/version "0.2.12"} quil/quil {:mvn/version "4.0.0-SNAPSHOT"}}}' -M -m lambdaisland.cljbox2d.demo.pyramid
clojure -Sdeps '{:deps {com.lambdaisland/cljbox2d {:mvn/version "0.2.12"} quil/quil {:mvn/version "4.0.0-SNAPSHOT"}}}' -M -m lambdaisland.cljbox2d.demo.platformer
```

<!-- installation -->
## Installation

To use the latest release, add the following to your `deps.edn` ([Clojure CLI](https://clojure.org/guides/deps_and_cli))

```
com.lambdaisland/cljbox2d {:mvn/version "0.2.12"}
```

or add the following to your `project.clj` ([Leiningen](https://leiningen.org/))

```
[com.lambdaisland/cljbox2d "0.2.12"]
```
<!-- /installation -->

You will also need a library to deal with graphics and user interaction. If unsure you can start with [Quil](http://quil.info/).

## Rationale

The Box2D API is highly imperative, to create a body or a fixture you first
create a BodyDef or FixtureDef object, call a bunch of setters to set the right
parameters, then use that to construct the actual object. Yuck.

For us it's all just data.

## Getting started

There's a
[template](https://github.com/lambdaisland/cljbox2d/blob/main/src/lambdaisland/cljbox2d/demo/template.cljc)
file that you can use to set up your first project with cljbox2d and Quil.

## Hello cljbox2d

Our version of the [Hello Box2D](https://box2d.org/documentation/md__d_1__git_hub_box2d_docs_hello.html) tutorial.



## Usage



## Writing portable code

The following jBox2D features are not supported by planck.js

- ConstantVolumeJoin
- Particles (and thus particle raycast)

<!-- opencollective -->
## Lambda Island Open Source

<img align="left" src="https://github.com/lambdaisland/open-source/raw/master/artwork/lighthouse_readme.png">

&nbsp;

cljbox2d is part of a growing collection of quality Clojure libraries created and maintained
by the fine folks at [Gaiwan](https://gaiwan.co).

Pay it forward by [becoming a backer on our Open Collective](http://opencollective.com/lambda-island),
so that we may continue to enjoy a thriving Clojure ecosystem.

You can find an overview of our projects at [lambdaisland/open-source](https://github.com/lambdaisland/open-source).

&nbsp;

&nbsp;
<!-- /opencollective -->

<!-- contributing -->
## Contributing

Everyone has a right to submit patches to cljbox2d, and thus become a contributor.

Contributors MUST

- adhere to the [LambdaIsland Clojure Style Guide](https://nextjournal.com/lambdaisland/clojure-style-guide)
- write patches that solve a problem. Start by stating the problem, then supply a minimal solution. `*`
- agree to license their contributions as MPL 2.0.
- not break the contract with downstream consumers. `**`
- not break the tests.

Contributors SHOULD

- update the CHANGELOG and README.
- add tests for new functionality.

If you submit a pull request that adheres to these rules, then it will almost
certainly be merged immediately. However some things may require more
consideration. If you add new dependencies, or significantly increase the API
surface, then we need to decide if these changes are in line with the project's
goals. In this case you can start by [writing a pitch](https://nextjournal.com/lambdaisland/pitch-template),
and collecting feedback on it.

`*` This goes for features too, a feature needs to solve a problem. State the problem it solves, then supply a minimal solution.

`**` As long as this project has not seen a public release (i.e. is not on Clojars)
we may still consider making breaking changes, if there is consensus that the
changes are justified.
<!-- /contributing -->

<!-- license -->
## License

Copyright &copy; 2021-2022 Arne Brasseur and Contributors

Licensed under the term of the Mozilla Public License 2.0, see LICENSE.
<!-- /license -->
