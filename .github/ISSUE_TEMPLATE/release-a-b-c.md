---
name: Release A.B.C
about: Steps for each release
title: 'Release A.B.C'
labels: ''
assignees: '@TobiasNx'

---

The maven central release will be the first semi-automatical, see <https://github.com/metafacture/metafacture-core/issues/709>

This release follows  A.B.C + LINK TO RELEASE ISSUE

Following [maintaining guidelines](https://github.com/metafacture/metafacture-core/blob/master/MAINTAINING.md).

- [ ] update dependencies if these are promising to not break much
- [ ] test with locally build `master-SNAPSHOT`
  - [ ] lobid-organisations (by @TobiasNx)
  - [ ] lobid-resources (by @TobiasNx)
  - [ ] lobid-extra-holdings  (by @TobiasNx)
  - [ ] oersi (by @tobiasnx)
  - [ ] limetrans (by @blackwinter)
- [ ] [release on maven central](https://central.sonatype.com/search?q=metafacture)
- [ ] [release on github](https://github.com/metafacture/metafacture-core/releases/)
- [ ]  update  [metafacture-playground](https://github.com/metafacture/metafacture-playground/issues/221)
- [ ] update [flux-commands](https://github.com/metafacture/metafacture-documentation/blob/master/docs/Documentation-Maintainer-Guide.md)
- [ ] update [metafacture-fix' functions](https://github.com/metafacture/metafacture-documentation/blob/master/docs/fix/Fix-functions.md)
- [ ] write [blog post](https://github.com/metafacture/metafacture-blog/issues/39)
- [ ] [toot](https://openbiblio.social/@metafacture/)
- [ ] announce at [metadaten.community](https://metadaten.community/c/software-und-tools/metafacture/8)
