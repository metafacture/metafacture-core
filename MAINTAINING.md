# Merging pull requests

Pull requests should not be rebased (as this would rewrite Git's history) but merged with the the latest master and merge as a none fast-forward merge into master.
For example, to merge a pull request `fix-xy` from http://github.com/cboehme/metafacture-core the following Git commands should be used:
```
# Bring master up-to-date:
git checkout master
git fetch
git rebase
# Fetch pull request:
git fetch http://github.com/cboehme/metafacture-core.git +fix-xy:cboehme-fix-xy
git checkout cboehme-fix-xy
# Merge master:
git merge master
# Run test cases, check commit, add fixup commits ...
# Merge into master
git checkout master
git merge --no-ff cboehme-fix-xy
```
The commit message of the merge command should follow this format:
```
Merge pull-request #PULLREQUEST-ID from cboehme/fix-xy
```

# Releasing

We shall make releases quarterly. Approximate timetable is every Januar, April, July, October.

Releasing involves to first make and test the release candidate before actually making the release. Note that we provide a *GitHub release for manual download* as well as a *release on Maven central* to be consumed as a library.

It's good habit to use semantic versioning in release numbers `A`.`B`.`C`, i.e. increase `A` when it's a major release breaking backward compatibility; increase `B` when it got new features; increase `C` indicating bug-fixes.

## Signing
To upload to Sonatype we need to sign the artifacts. Adjust `gradle.properties` in the root directory that like this:

```
signing.gnupg.executable=gpg
signing.gnupg.useLegacyGpg=true
signing.gnupg.homeDir=$e.g."~/.gnupg"
signing.gnupg.keyName=$yourKeyName
signing.password=$keysPassphrase
# depending on gradle plugin versions etc. you may need to use:
signing.keyId=$yourKeyName
signing.secretKeyRingFile=$e.g."~/.gnupg/secring.gpg"
```

## Authorize at central sonatype

To be able to authorize at central.sonatype you first have to "Generate User
Token" at https://central.sonatype.com/usertoken (must be logged in). Copy/add the
snippet you will be provided when creating the token to `~/.m2/settings.xml`.

!This is also needed when testing the uploaded deployment bundle (see below).!

## Authorize the nexus publishing plugin

According to the [nexus
  plugin](https://github.com/gradle-nexus/publish-plugin?tab=readme-ov-file#publishing-to-maven-central-via-sonatype-central)
  you have to add the following to the `gradle.properties`:

```
sonatypeUsername=$usernameOfToken
sonatypePassword=$passwordOfToken
```

## Upload, test, publish a release

There are no more "Release candidates" as such, but the uploaded, validated
[deployment bundle can be
  tested](https://central.sonatype.org/publish/publish-portal-api/#manually-testing-a-deployment-bundle).
  I.e. you first upload a release and before publishing it you test it.

### Upload

a) It's going from your local Git repository to central.sonatype.com to Maven Central. Each station requires some manual actions so you can double check that everything is ok. b) A release should also be published to GitHub.

1. Make an annotated signed tag for the release:
    ```
    git tag -s metafacture-core-A.B.C
    ```
1. When prompted, add a sensible tag message. For instance, something like:
    ```
    Release A.B.C
    ```
1. Make sure to have a *clean* Git directory (otherwise the build will fail with the error message `Working copy has modifications`):
    ```
    git status
    ```
1. Now you can build and upload the release to Sonatype:
    ```
    ./gradlew publishToSonatype  -PpublishVersion=A.B.C  closeSonatypeStagingRepository
    ```
### Test

_As a fallback and for build systems where the below does not work:
git checkout the release tag resp. the branch, build locally and consume locally. You don't need
to have a login then, no special configs etc._

If you decide to test what is actually in the pipeline you need some
prerequisites;
You need to have a login at central.sonatype.com and be added as a
maintainer of the namespace `org.metafacture`.
Follow the  section "Authorize at central sonatype" to be able to test the
deployment bundle.

You have to add this into you `~/.m2/settings.xml`:
```
  <servers>
    <server>
      <id>central.manual.testing</id>
      <configuration>
        <httpHeaders>
          <property>
            <name>Authorization</name>
            <value>Bearer $basencodedUsernameAndPassword</value>
          </property>
        </httpHeaders>
      </configuration>
    </server>
  </servers>

  <profiles>
    <profile>
      <id>central.manual.testing</id>
      <repositories>
        <repository>
          <id>central.manual.testing</id>
          <name>Central Testing repository</name>
          <url>https://central.sonatype.com/api/v1/publisher/deployments/download</url>
        </repository>
      </repositories>
    </profile>
  </profiles>
```
where `basencodedUsernameAndPassword` is created like this:
```
printf "$usernameToken:$passwordToken" | base64
```
(note the semicolon `:`).

If you have a maven project you can now update the dependencies in the
`pom.xml` and download the like:

```
mvn -debug -Pcentral.manual.testing install
```

###  Publish

If the tests went well we can publish.
We publish the Metafacture libraries as modules to maven central and a
Metafactur standalone runner at GitHub releases.

#### Publish Metafacture Runner to Github Releases

This provides the standalone runner.

1. Push the annotated signed tag you have created in the "Upload" section to GitHub:
    ```
    git push origin metafacture-core-A.B.C
    ```
1. Now the tag is available on GitHub. You can manually choose to [draft a new release on GitHub](https://github.com/metafacture/metafacture-core/releases/new). The signed `*-dist.*` files must be uploaded manually. They are produced like this:
    ```
    ./gradlew metafacture-runner:signArchive -PpublishVersion=A.B.C
    ```
    and can be found in `metafacture-runner/build/distributions/` (don't mind the `Source code` for that is created by GitHub automatically).

#### Publish to Maven Central

1. Finally, go to [central.sonatype.com](https://central.sonatype.com/publishing), log in, check the namespace (if you maintain more than one repo). Attention, because once published it can't be removed. If sure, click on "Publish". The artifacts are uploaded to Maven Central (which may take some time; have a look at e.g. [metafacture-biblio](https://repo1.maven.org/maven2/org/metafacture/metafacture-biblio/)).i
