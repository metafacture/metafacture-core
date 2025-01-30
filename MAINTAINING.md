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
We may publish `master-SNAPSHOT` builds more frequently.

Releasing involves to first make and test the release candidate before actually making the release. Note that we provide a *GitHub release for manual download* as well as a *release on Maven central* to be consumed as a library.

It's good habit to use semantic versioning in release numbers `A`.`B`.`C`, i.e. increase `A` when it's a major release breaking backward compatibility; increase `B` when it got new features; increase `C` indicating bug-fixes. A suffix like `rcN` (where `N` is a number) indicates a release candidate (`rc`).

To upload to Sonatype you need (as well for the release candidate as for the release) a `gradle.properties` in the root directory that looks like this:

```
signing.gnupg.executable=gpg
signing.gnupg.useLegacyGpg=true
signing.gnupg.homeDir=$e.g."~/.gnupg"
signing.gnupg.keyName=$yourKeyName
signing.password=$keysPassphrase
# depending on gradle plugin versions etc. you may need to use:
# signing.keyId=$yourKeyName
# signing.secretKeyRingFile=$e.g."~/.gnupg/secring.gpg"
#  Go to https://s01.oss.sonatype.org/
#  Go to profile
#  Change the pulldown from “Summary” to “User Token”
#  Click on “Access User Token”
sonatypeUsername=$usernameOfAccessUserToken
sonatypePassword=$token
```

## Publish `master-SNAPSHOT` builds

These are done more often, in irregular intervals. They are not considered stable and may break your application, so be cautious when using them.

The process is equal to the making of a release candidate, but without making any tags:

1. build and upload the `master-SNAPSHOT`:
    ```
    git pull; git checkout master;
    ```
1. proceed as described in [Release candidate - Upload to Sonatype](https://github.com/metafacture/metafacture-core/wiki/Maintainer-Guidelines#upload-to-sonatype)

## Release candidate

*Release candidates should be tested by different people before releasing!*

### Prepare your release candidate

1. Make an rc-branch (necessary for Gradle to pick up the proper name):
    ```
    git checkout -b A.B.C-rcN
    ```
    (leave out the ` metafacture-core-` to avoid later the git "error: src refspec ... matches more than one" when you push the annotated git tag for having a tag named the same as the branch is not possible)
1. Optionally, you can now test the build locally by invoking a Gradle target:
    ```
    ./gradlew assemble
    ```

### Upload to Sonatype

1. Now you can build and upload the release candidate to Sonatype (note that `./gradlew` should inform you to make a "snapshot build". If the version doesn't end with `-SNAPSHOT` the artifacts will not be uploaded to Sonatype's snapshot repository!):
    ```
    ./gradlew clean; ./gradlew publishToMavenLocal; ./gradlew publishToSonatype
    ```
1. Go to [Sonatype's snapshot repository](https://oss.sonatype.org/index.html#nexus-search;gav~org.metafacture) and type in the correct `Version` to see if it is already available there (can take some minutes). [Example for `5.5.1-rc1-SNAPSHOT`](https://oss.sonatype.org/index.html#nexus-search;gav~org.metafacture~~5.5.1*~~)(if you don't see a `5.5.1-rc1-SNAPSHOT.jar` there check it at https://oss.sonatype.org/content/repositories/snapshots/org/metafacture/metafacture-biblio/5.5.1-rc1-SNAPSHOT/).
1. Make an annotated signed tag (it's important to do that _after_ uploading to Sonatype's snapshot repository because otherwise the `-SNAPSHOT` will not be appended to the release candidate thus will not land in `snapshot repository`):
    ```
    git tag -s metafacture-core-A.B.C-rcN
    ```
1. Push the annotated signed tag to GitHub:
    ```
    git push origin tag metafacture-core-A.B.C-rcN
    ```

### Publish to [GitHub Packages](https://github.com/orgs/metafacture/packages?repo_name=metafacture-core)

1. Push your properly named branch to GitHub. Notice the `-rc` part of the branch's name:
    ```
    git push origin A.B.C-rcN
    ```
Because there is `fetch --no-tags` in `actions/checkout@v2` the `-SNAPSHOT` suffix will always be appended (in comparison to doing `./gradlew publishAllPublicationsToGitHubPackagesRepository` locally, which will find the `SCM tag`). The publishing to GitHub packages is triggered then.

If we don't want `-SNAPSHOT` we may want to remove the `-SNAPSHOT` in `build.gradle`:
```
if (grgit.branch.current().name.contains('-rc')) { ...
    return "${grgit.branch.current().name}-SNAPSHOT"
}
```

Note that `Packages` is not the same as [`Releases`](https://github.com/metafacture/metafacture-core/releases).

### Consume the SNAPSHOT

1. See e.g. [Example for 5.5.1-rc1-SNAPSHOT](https://oss.sonatype.org/index.html#nexus-search;gav~org.metafacture~~5.5.1*~~) how to configure the dependency.
1. Configure your build system to use Sonatype's Snapshot Repository to be able to load the dependencies of the release candidate (or master-SNAPSHOT).
    For Maven update your `pom.xml` (after `</dependencies>`):
    ```xml
    <repositories>
        <repository>
            <id>oss.sonatype.org-snapshot</id>
            <url>https://oss.sonatype.org/content/repositories/snapshots</url>
            <releases>
                <enabled>false</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
    ```
    For Gradle, add the snapshots repository:
    ```gradle
    repositories {
        maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
    }
    ```
    For Leiningen, add this to your `project.clj` (and be aware of the proper indentation!):
    ```clojure
        :repositories [["snapshots" "https://oss.sonatype.org/content/repositories/snapshots"]]
    ```
    For sbt, add this to your `build.sbt`:
    ```
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    ```

## Making a release

a) It's going from your local Git repository to Sonatype to Maven Central. Each station requires some manual actions so you can double check that everything is ok. b) A release should also be published to GitHub.

1. Switch to `master` branch. Merge the approved `rc` into master:
    ```
    git switch master; pull --no-ff origin A.B.C-rcN; git push origin master
    ```

1. Make sure you have a signed tag locally:
    ```
    git show metafacture-core-A.B.C
    ```
    If it doesn't exist yet, create it:
    ```
    git tag -s metafacture-core-A.B.C
    ```
1. When prompted, add a sensible commit message. For instance, something like:
    ```
    Release 5.7.0
    ```
1. Make sure you have that signed tag pushed to GitHub:
    ```
    git ls-remote --tags origin
    ```
    If it is missing, push it with:
    ```
    git push origin  metafacture-core-A.B.C
    ```
1. Now the tag is available at GitHub. You can manually choose to [draft a new release on GitHub](https://github.com/metafacture/metafacture-core/releases/new). The signed `*dist*` files must be uploaded manually. They are produced like this:
    ```
    ./gradlew metafacture-runner:signArchive
    ```
    and can be found in `metafacture-core/metafacture-runner/build/distributions/` (don't mind the `Source code` for that is created by GitHub automatically).
1. Make sure to have a *clean* Git directory (otherwise only a SNAPSHOT will be built):
    ```
    git status
    ```
1. Let the release be built and uploaded (the SCM tag will be detected and the release be built):
    ```
    ./gradlew clean; ./gradlew publishToMavenLocal; ./gradlew publishToSonatype
    ```
1. Finally, go to [oss.sonatype.org](https://oss.sonatype.org), log in, check the [Staging Repositories](https://oss.sonatype.org/#stagingRepositories) and when it's finished, click on `Close`. If everything is good publish with clicking on `Release` - attention, because once published it can't be removed. The artifacts are uploaded to Maven Central (which may take some time. Have a look e.g. [metafacture-biblio](https://repo1.maven.org/maven2/org/metafacture/metafacture-biblio/) ). You can check that it's actually in the publishing pipeline by clicking on `Views/Repositories->Releases`, then type in the `Path lookup` field `org/metafacture/` and click on version.

