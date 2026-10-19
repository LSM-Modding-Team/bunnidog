"""Minimal deterministic native structure writer. All block layout remains in the owning art script."""
import gzip
import struct


def string(value):
    return 8, value


def integer(value):
    return 3, value


def compound(value):
    return 10, value


def listing(kind, values):
    return 9, (kind, values)


def utf(value):
    raw = value.encode('utf-8')
    return struct.pack('>H', len(raw))+raw


def payload(kind, value):
    if kind == 3:
        return struct.pack('>i', value)
    if kind == 8:
        return utf(value)
    if kind == 9:
        element, values = value
        return bytes([element])+struct.pack('>i', len(values))+b''.join(payload(element, v) for v in values)
    if kind == 10:
        return b''.join(bytes([t])+utf(name)+payload(t, v) for name, (t, v) in value.items())+b'\0'
    raise ValueError(f'Unsupported template NBT tag {kind}')


def save(path, cells, size, version=4903):
    """cells maps (x,y,z) to (native block id, string properties, optional typed block-entity compound)."""
    palette, indices, blocks = [], {}, []
    for pos, (name, properties, data) in sorted(cells.items(), key=lambda cell: (cell[0][1], cell[0][2], cell[0][0])):
        key = name, tuple(sorted(properties.items()))
        if key not in indices:
            indices[key] = len(palette)
            state = {'Name': string(name)}
            if properties:
                state['Properties'] = compound({k: string(v) for k, v in sorted(properties.items())})
            palette.append(state)
        block = {'pos': listing(3, list(pos)), 'state': integer(indices[key])}
        if data:
            block['nbt'] = compound(data)
        blocks.append(block)
    root = {'DataVersion': integer(version), 'size': listing(3, list(size)),
            'palette': listing(10, palette), 'blocks': listing(10, blocks), 'entities': listing(10, [])}
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(gzip.compress(b'\x0a\0\0'+payload(10, root), mtime=0))
