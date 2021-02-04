# lambdaisland/cljbox2d

<!-- badges -->
<!-- [![CircleCI](https://circleci.com/gh/lambdaisland/cljbox2d.svg?style=svg)](https://circleci.com/gh/lambdaisland/cljbox2d) [![cljdoc badge](https://cljdoc.org/badge/lambdaisland/cljbox2d)](https://cljdoc.org/d/lambdaisland/cljbox2d) [![Clojars Project](https://img.shields.io/clojars/v/lambdaisland/cljbox2d.svg)](https://clojars.org/lambdaisland/cljbox2d) -->
<!-- /badges -->

Idiomatic and cross-platform Clojure version of the Box2D physics engine API. Wraps jBox2D (Clojure) and Planck.js (ClojureScript).

Created for use with Quil, but can be used independently.

Work in progress. See the demo directory for examples.

## Writing portable code

The following jBox2D features are not supported by planck.js

- ConstantVolumeJoin
- Particles (and thus particle raycast)


## License

Copyright &copy; 2021 Arne Brasseur and Contributors

Licensed under the term of the Mozilla Public License 2.0, see LICENSE.

