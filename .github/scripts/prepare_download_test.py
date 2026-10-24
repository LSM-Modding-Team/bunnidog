#!/usr/bin/env python3
"""Copy an identified successful CI JAR into an unpublished diagnostic release."""
from io import BytesIO
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import sys
from zipfile import ZipFile


def gh(*args):
    return subprocess.check_output(['gh', *args], text=True).strip()


def api(path):
    return json.loads(gh('api', path))


def require(condition, message):
    if not condition:
        raise SystemExit(message)


def metadata():
    repo = os.environ['GITHUB_REPOSITORY']
    run_id, release_id = os.environ['BUILD_RUN_ID'], os.environ['DRAFT_RELEASE_ID']
    expected = os.environ['EXPECTED_JAR_SHA256'].lower()
    require(re.fullmatch(r'[1-9][0-9]*', run_id) and re.fullmatch(r'[1-9][0-9]*', release_id), 'Run/release IDs must be positive integers')
    require(re.fullmatch(r'[0-9a-f]{64}', expected), 'Expected JAR SHA-256 must contain 64 hexadecimal characters')
    run = api(f'repos/{repo}/actions/runs/{run_id}')
    require(run['status'] == 'completed' and run['conclusion'] == 'success', 'Source build must have succeeded')
    require(run['event'] == 'push' and run['head_branch'] == 'main' and run['head_repository']['full_name'] == repo,
            'Source must be this repository\'s main-branch push build')
    require(run['path'] == '.github/workflows/build.yml', 'Source must be the Build workflow')
    sha = run['head_sha']
    require(re.fullmatch(r'[0-9a-f]{40}', sha), 'Invalid source commit')
    release = api(f'repos/{repo}/releases/{release_id}')
    require(release['draft'] is True, 'Only unpublished draft releases may be changed')
    require(release['target_commitish'] == sha, 'Draft must target the exact source build commit')
    require(re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9._-]*', release['tag_name']), 'Unsupported draft tag')
    return repo, run_id, release_id, expected, sha, release


def main():
    repo, run_id, release_id, expected, sha, release = metadata()
    if sys.argv[1:] == ['validate']:
        artifacts = api(f'repos/{repo}/actions/runs/{run_id}/artifacts?per_page=100')['artifacts']
        matching = [a for a in artifacts if a['name'] in {f'bunnidogs-{sha}', f'ci-transfer-bunnidogs-{sha}'} and not a['expired']]
        require(len(matching) == 1, 'Expected one unexpired artifact for the exact commit')
        with open(os.environ['GITHUB_OUTPUT'], 'a') as out:
            out.write(f"artifact={matching[0]['name']}\n")
        return
    require(sys.argv[1:] == ['upload'], 'Expected validate or upload')
    candidates = list(Path('download-test-input').rglob('*'))
    files = [p for p in candidates if p.is_file()]
    require(len(files) == 1 and files[0].suffix == '.jar', 'Source artifact must contain exactly one JAR')
    source = files[0]
    require(re.fullmatch(r'bunnidogs-[A-Za-z0-9._-]+', source.stem), 'Unexpected JAR name')
    content = source.read_bytes()
    require(hashlib.sha256(content).hexdigest() == expected, 'JAR does not match the expected original SHA-256')
    output = Path('download-test-output')
    output.mkdir()
    jar = output / f'{source.stem}-mc26.2-neoforge-{sha[:7]}.jar'
    jar.write_bytes(content)
    checksums = output / 'SHA256SUMS.txt'
    control = None
    archive_digest = None
    checksum_text = f'{expected}  {jar.name}\n'
    if os.environ.get('INCLUDE_ORIGINAL_ZIP', 'false').lower() == 'true':
        artifacts = api(f'repos/{repo}/actions/runs/{run_id}/artifacts?per_page=100')['artifacts']
        original = [a for a in artifacts if a['name'] in {f'bunnidogs-{sha}', f'ci-transfer-bunnidogs-{sha}'} and not a['expired']]
        require(len(original) == 1, 'Original artifact is missing or expired')
        try:
            archive = subprocess.check_output(['gh', 'api', f"repos/{repo}/actions/artifacts/{original[0]['id']}/zip"], stderr=subprocess.PIPE)
        except subprocess.CalledProcessError:
            raise SystemExit('Could not retrieve the original artifact ZIP')
        archive_digest = hashlib.sha256(archive).hexdigest()
        require(original[0].get('digest') == f'sha256:{archive_digest}', 'Original archive digest differs from GitHub metadata')
        with ZipFile(BytesIO(archive)) as zipped:
            require(len(zipped.infolist()) == 1 and zipped.namelist()[0] == source.name, 'Unexpected original ZIP contents')
            require(zipped.read(source.name) == content, 'ZIP and extracted JAR differ')
        control = output / f'bunnidogs-{sha}.zip'
        control.write_bytes(archive)
        checksum_text += f'{archive_digest}  {control.name}\n'
    checksums.write_text(checksum_text)
    # Check again immediately before uploading; this workflow never publishes a release.
    fresh = api(f'repos/{repo}/releases/{release_id}')
    require(fresh['draft'] is True, 'Release was published during preparation')
    existing = {a['name']: a for a in fresh['assets']}
    upload = [str(checksums)]
    binaries = [(jar, expected)] + ([(control, archive_digest)] if control else [])
    for path, digest in binaries:
        if path.name in existing:
            require(existing[path.name].get('digest') == f'sha256:{digest}', 'Existing asset differs; refusing to replace it')
        else:
            upload.append(str(path))
    # Keep existing binary asset IDs stable while a maintainer is testing their downloads.
    subprocess.run(['gh', 'release', 'upload', release['tag_name'], *upload, '--clobber', '--repo', repo], check=True)
    current = api(f'repos/{repo}/releases/{release_id}')
    require(current['draft'] is True, 'Release is no longer a draft')
    assets = {a['name']: a for a in current['assets']}
    require(jar.name in assets and assets[jar.name]['size'] == len(content), 'Uploaded JAR is missing or has the wrong size')
    require(assets[jar.name].get('digest') == f'sha256:{expected}', 'Uploaded JAR digest differs')
    if control:
        require(assets.get(control.name, {}).get('digest') == f'sha256:{archive_digest}', 'Uploaded ZIP digest differs')
    with open(os.environ['GITHUB_STEP_SUMMARY'], 'a') as out:
        out.write(f"Prepared [unpublished download test]({current['html_url']}) from `{sha}`.\n\nJAR SHA-256: `{expected}`\n")
    print('Draft assets uploaded; original JAR bytes preserved:', expected)


if __name__ == '__main__':
    main()
