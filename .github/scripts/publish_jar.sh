#!/usr/bin/env bash
set -euo pipefail
# Only a successful main push may turn its exact CI bytes into a public download.
[[ "$GITHUB_EVENT_NAME" == push && "$GITHUB_REF" == refs/heads/main ]]
[[ "$GITHUB_SHA" =~ ^[a-f0-9]{40}$ ]]
mapfile -t jars < <(find build/libs -maxdepth 1 -type f -name '*.jar')
[[ ${#jars[@]} == 1 ]]
short_sha=${GITHUB_SHA:0:12}
tag="build-$short_sha"
mkdir -p release-assets
jar_name="bunnidogs-mc26.2-neoforge-$short_sha.jar"
cp -- "${jars[0]}" "release-assets/$jar_name"
(cd release-assets && sha256sum "$jar_name" > SHA256SUMS.txt)
cat > release-assets/NOTES.md <<EOF_NOTES
Install **$jar_name** in your Minecraft 26.2 / NeoForge mods folder.

Use the JAR under **Assets** below. It is the exact successful CI build, with no ZIP extraction needed.

Source: $GITHUB_SHA
Checks: Java 25 build, model validation and native GameTests.
EOF_NOTES
if ! gh release view "$tag" --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1; then
  gh release create "$tag" --repo "$GITHUB_REPOSITORY" --target "$GITHUB_SHA" \
    --title "Development build $short_sha" --draft --prerelease --notes-file release-assets/NOTES.md
fi
release_target=$(gh release view "$tag" --repo "$GITHUB_REPOSITORY" --json targetCommitish --jq .targetCommitish)
[[ "$release_target" == "$GITHUB_SHA" ]]
# Reruns verify an existing binary; a failed upload can resume without overwriting it.
mkdir -p release-existing
if gh release download "$tag" --repo "$GITHUB_REPOSITORY" --dir release-existing --pattern "$jar_name"; then
  cmp "release-assets/$jar_name" "release-existing/$jar_name"
else
  gh release upload "$tag" --repo "$GITHUB_REPOSITORY" "release-assets/$jar_name"
fi
gh release upload "$tag" --repo "$GITHUB_REPOSITORY" release-assets/SHA256SUMS.txt --clobber
# Publication is the final operation, after both checked assets exist.
gh release edit "$tag" --repo "$GITHUB_REPOSITORY" --draft=false --prerelease

url="https://github.com/$GITHUB_REPOSITORY/releases/download/$tag/$jar_name"
{
  printf '### Download the mod\n\n[**Download JAR**](%s)\n\n' "$url"
  printf 'Minecraft **26.2**, NeoForge, source `%s`.\n\n' "$GITHUB_SHA"
  printf 'The Actions artifact is an internal ZIP archive; the link above downloads the JAR directly.\n'
} >> "$GITHUB_STEP_SUMMARY"
