# Chrome download block — 2026-10-24

The user reports that Chrome blocks the Actions ZIP for `c39c82e` as dangerous,
while the previous version downloads normally with the same protection enabled.
Treat this as an unresolved distribution issue. Do not recommend disabling
browser protection or claim that a successful build proves a false positive.

## Comparison of the actual CI artifacts

Both archives were retrieved through the authenticated GitHub connector and their
SHA-256 hashes match the artifact metadata. Both contain exactly one
`bunnidogs-1.0.0.jar`, with identical DEFLATE compression, ZIP flags and Unix mode
`100644`. Neither archive/JAR is corrupt. No build configuration, dependencies or
upload workflow changed between these commits. The changed classes/resources
correspond to the Apex implementation; no native executables, scripts, embedded
JARs or unexpected bundles were added. The latest CI JAR is byte-for-byte identical
to the locally built JAR. These observations do not establish Google's reason.

| File | SHA-256 |
|---|---|
| Previous Actions ZIP (`8ea9b4b`) | `5b3367650c1469859b083246ea8314554fd970cc78c9835a89356c242e00f13c` |
| Blocked Actions ZIP (`c39c82e`) | `44b65c37bae7bdcfde3ca3f9e97814709579e744123786617e391a9313d972a3` |
| Previous JAR | `6c20086e78d135bd3813fa0700e464c32d655a007df69396ddf925915a20a5c4` |
| Latest JAR | `f3ac62bbdf6fc3b467d9ea878c8776dc022940d523a604a3ffde0e93faae8ce3` |

No game code was changed to generate a new hash or try to evade the warning.

## Direct-JAR diagnostic download

Draft release `apex-preview-c39c82e` targets the exact original commit. The manual
**Prepare download test** workflow copies the original JAR from successful Build
run `40118608846`, checks its expected SHA-256, and attaches the identical bytes
under a descriptive filename, the unchanged original ZIP under its original
filename, and `SHA256SUMS.txt`. Existing binary asset IDs are retained on reruns. It never publishes a release.
This is a testable distribution route, not a confirmed fix for Chrome.

Dispatch inputs for this incident:

- `build_run_id`: `40118608846`
- `draft_release_id`: `422901680`
- `expected_jar_sha256`: `f3ac62bbdf6fc3b467d9ea878c8776dc022940d523a604a3ffde0e93faae8ce3`

The workflow rejects non-main/non-Build sources, failed or expired builds,
mismatched commits/hashes, extra files and published release targets. It uses
GitHub's normal Actions token to upload; it does not change the current cloud
session's credentials. Do not upload private binaries to public scanning services
or publish this draft merely to investigate the warning.

The original artifact API redirects also differed: the previous run used
`productionresultssa2.blob.core.windows.net`, while the flagged run used
`productionresultssa6.blob.core.windows.net`. This is an observed delivery
variable, not evidence that either host is the cause.

With Chrome's normal protection enabled, compare the Actions ZIP, the exact same
ZIP from the draft, and the direct JAR from that draft. An unblocked identical
ZIP on the release route isolates a download-URL/context difference without any
change to the archive contents, filename or hash. A successful direct download narrows the issue to the
container/download route or associated reputation; it does not identify Google's
exact rule. If both are blocked, obtain Chrome's specific download-protection
verdict before changing packaging or gameplay. Google's download warning cannot
be inspected from the build server. Redact temporary/private URLs from any
browser diagnostic logs before sharing them.

The public sharing route should use an approved release or a mod distribution
platform, with a versioned installable JAR and checksum. A draft is visible only
to repository maintainers. Publish only when requested; recheck the resulting
public download with protection enabled. No route is guaranteed unblocked until
that browser test is performed.
