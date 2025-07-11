import { LuaRandom } from '../src/LuaRandom.js';

describe('LuaRandom', () => {
	// Known expected values for specific seeds
	const KNOWN_VALUES = [
		{ seed: 0.0, expected: 0.794206292431241 },
		{ seed: 1.0, expected: 0.3238105623786367 },
		{ seed: 42.0, expected: 0.9560792879182105 },
		{ seed: 12345.0, expected: 0.3579737466187569 }
	];

	describe('random method', () => {
		test('should produce deterministic output for known seeds', () => {
			KNOWN_VALUES.forEach(({ seed, expected }) => {
				const result = LuaRandom.random(seed);
				expect(result).toBeCloseTo(expected, 10);
			});
		});

		test('should return a value between 0 and 1', () => {
			for (let i = 0; i < 100; i++) {
				const result = LuaRandom.random(i);
				expect(result).toBeGreaterThanOrEqual(0);
				expect(result).toBeLessThan(1);
			}
		});

		test('should be deterministic for the same seed', () => {
			const seed = 12345.67;
			const firstResult = LuaRandom.random(seed);
			const secondResult = LuaRandom.random(seed);
			expect(firstResult).toBe(secondResult);
		});
	});

	describe('randint method', () => {
		test('should return integers within the specified range', () => {
			const max = 10;
			for (let seed = 0; seed < 100; seed++) {
				const result = LuaRandom.randint(seed, max);
				expect(result).toBeGreaterThanOrEqual(0);
				expect(result).toBeLessThanOrEqual(max);
				expect(Number.isInteger(result)).toBe(true);
			}
		});

		test('should be deterministic for the same seed and max', () => {
			const seed = 123.45;
			const max = 1000;
			const firstResult = LuaRandom.randint(seed, max);
			const secondResult = LuaRandom.randint(seed, max);
			expect(firstResult).toBe(secondResult);
		});

		test('should handle max=0 case', () => {
			const result = LuaRandom.randint(42, 0);
			expect(result).toBe(0);
		});
	});

	describe('_unsignedRightShift method', () => {
		test('should perform unsigned right shift correctly', () => {
			expect(LuaRandom._unsignedRightShift(8n, 1n)).toBe(4n);
			expect(LuaRandom._unsignedRightShift(9n, 1n)).toBe(4n);

			// Test with negative values (in signed representation)
			const negative = -1n & ((1n << 64n) - 1n); // -1 as unsigned 64-bit
			expect(LuaRandom._unsignedRightShift(negative, 4n)).toBe(1152921504606846975n);
		});
	});

	describe('_doubleToLongBits and _longBitsToDouble methods', () => {
		test('should round-trip values correctly', () => {
			const testValues = [0, 1, -1, Math.PI, 1.23456789, Number.MAX_SAFE_INTEGER];

			testValues.forEach(value => {
				const bits = LuaRandom._doubleToLongBits(value);
				const roundTrip = LuaRandom._longBitsToDouble(bits);
				expect(roundTrip).toBe(value);
			});
		});

		test('_doubleToLongBits should produce expected bit patterns', () => {
			// IEEE 754 representation of 1.0 is 0x3ff0000000000000
			expect(LuaRandom._doubleToLongBits(1.0)).toBe(4607182418800017408n);

			// IEEE 754 representation of 0.0 is 0x0000000000000000
			expect(LuaRandom._doubleToLongBits(0.0)).toBe(0n);
		});
	});

	describe('_randInt method', () => {
		test('should produce deterministic output for known seeds', () => {
			// These expected values would need to be obtained from a reference implementation
			// For now, we're just testing that the values are consistent
			const seeds = [0.0, 1.0, 42.0, 12345.0];
			const results = seeds.map(seed => LuaRandom._randInt(seed));

			// Ensure we get the same results on repeated calls
			seeds.forEach((seed, index) => {
				expect(LuaRandom._randInt(seed)).toBe(results[index]);
			});
		});

		test('should return a BigInt', () => {
			const result = LuaRandom._randInt(0.0);
			expect(typeof result).toBe('bigint');
		});
	});

	describe('randdblmem method', () => {
		test('should return a value with the correct bit pattern', () => {
			// randdblmem should return a value with the exponent bits set to 1023
			// (which is 0x3ff in the exponent position)
			const result = LuaRandom.randdblmem(0.0);

			// Check if the exponent bits are set correctly (bits 52-62 should be 0x3ff)
			const exponentMask = 0x7FF0000000000000n;
			const exponentValue = (result & exponentMask);
			expect(exponentValue).toBe(4607182418800017408n); // 0x3ff0000000000000

			// Verify that only the mantissa and exponent are modified
			const mantissaMask = 0x000FFFFFFFFFFFFFn;
			expect(result & ~mantissaMask).toBe(4607182418800017408n);
		});
	});
});