<!--
  SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md

  SPDX-License-Identifier: CC-BY-4.0
  -->

[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/tudo-aqua/rereso/ci.yml?logo=githubactions&logoColor=white)](https://github.com/tudo-aqua/rereso/actions)
[![JavaDoc](https://javadoc.io/badge2/tools.aqua/rereso/javadoc.svg)](https://javadoc.io/doc/tools.aqua/rereso)
[![Maven Central](https://img.shields.io/maven-central/v/tools.aqua/rereso?logo=apache-maven)](https://search.maven.org/artifact/tools.aqua/rereso)

# ReReSo Schemas and JVM Tooling

The ReReSo Project seeks to improve the *re*usability of *re*search *so*ftware by standardizing
benchmark and tool formats as well as metadata to describe them. While the metadata formats are
expected to be community-agnostic, the benchmark formats utilize _layered_ combinations of
community-specific schemata.

This repository contains:

1. metadata schemata for tools and benchmarks,
2. benchmark schemata for log files (i.e., sequences of events that may additionally contain, e.g.,
   temporal information), and
3. Kotlin-based tools to work with these formats on the JVM (see the badge for Maven Central
   releases).

### License

All code files in this repository are licensed under the Apache License, Version 2.0. All non-code
files are licensed under the Creative Commons 4.0 Attribution License. License information is
compliant with the REUSE 3.3 specification.

### Acknowledgements

The authors would like to thank the Federal Government and the Heads of Government of the Länder, as
well as the Joint Science Conference (GWK), for their funding and support within the framework of
the NFDI4Ing consortium. Funded by the German Research Foundation (DFG) -- project number
[442146713 (NFDI4Ing)](https://gepris.dfg.de/gepris/projekt/442146713).
