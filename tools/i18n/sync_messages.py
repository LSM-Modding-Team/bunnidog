#!/usr/bin/env python3
"""Collect literal English fallbacks used by Texts/TextComponents; --write adds missing entries.

Dynamic allele/effect/food labels also live in the catalog and are exercised by LocalizationCheck.
Spanish additions start with the English fallback and must be translated before delivery.
"""
import hashlib
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
LANG = ROOT / 'src/main/resources/assets/bunnidogs/lang'
TOKENS = re.compile(r'//[^\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"')

def key(message):
    return 'text.bunnidogs.' + hashlib.sha256(message.encode()).hexdigest()[:16]

def literals():
    messages = {}
    for path in (ROOT / 'src/main/java').rglob('*.java'):
        source = path.read_text(encoding='utf-8')
        # Mask comments without disturbing quoted strings or source offsets.
        source = TOKENS.sub(lambda m: m[0] if m[0].startswith('"') else ' ' * len(m[0]), source)
        for call in re.finditer(r'\b(?:Texts|TextComponents)\.t\(', source):
            start = pos = call.end()
            depth = 0
            while pos < len(source):
                if source[pos] == '"':
                    quoted = re.match(r'"(?:\\.|[^"\\])*"', source[pos:])
                    pos += len(quoted[0]); continue
                if source[pos] in '([': depth += 1
                if source[pos] in ')]':
                    if depth == 0: break
                    depth -= 1
                if source[pos] == ',' and depth == 0: break
                pos += 1
            for quoted in TOKENS.finditer(source[start:pos]):
                if quoted[0].startswith('"'):
                    message = json.loads(quoted[0])
                    if message: messages[key(message)] = message
    return messages

def main():
    messages = literals()
    english = json.loads((LANG / 'en_us.json').read_text(encoding='utf-8'))
    spanish = json.loads((LANG / 'es_pe.json').read_text(encoding='utf-8'))
    missing = {k:v for k,v in messages.items() if english.get(k) != v or k not in spanish}
    if '--write' in sys.argv:
        additions = {k:v for k,v in messages.items() if k not in english}
        for k,v in messages.items():
            if k in english and english[k] != v: raise SystemExit('English fallback/key mismatch: '+k)
        english.update(messages)
        for k,v in messages.items(): spanish.setdefault(k,v)
        if additions:
            path = LANG / 'en_us.json'
            original = path.read_text(encoding='utf-8').rstrip()
            body = json.dumps(additions,ensure_ascii=False,indent=2)
            path.write_text(original[:-1].rstrip()+',\n'+body[2:-2]+'\n}\n',encoding='utf-8')
        (LANG / 'es_pe.json').write_text(json.dumps(spanish,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
        print(f'Collected {len(messages)} literal fallback messages; {len(missing)} additions.')
    elif missing:
        for k,v in missing.items(): print(k,repr(v))
        raise SystemExit('Missing localization entries. Run tools/i18n/sync_messages.py --write and translate the additions.')
    else: print(f'All {len(messages)} literal fallback messages are cataloged.')

if __name__ == '__main__': main()
