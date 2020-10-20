const { base64_encode, base64_decode } = require('../src/libs/utils/base64');

it('can encode/decode to base64', () => {
    let res = base64_encode('hello');
    expect(res).not.toBeNull();
    expect(res).toBe('aGVsbG8=');
    
    res = base64_decode('aGVsbG8=');
    expect(res).toBe('hello');
});
