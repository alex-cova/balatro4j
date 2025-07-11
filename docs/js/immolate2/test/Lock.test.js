import { Lock } from '../src/utils/Lock.js';
import { describe, expect, it } from 'vitest';

describe('Lock', () => {
	let lock;

	beforeEach(() => {
		lock = new Lock();
	});

	describe('lock method', () => {
		it('should lock a string item', () => {
			lock.lock('Test Item');
			expect(lock.isLocked('Test Item')).toBe(true);
		});

		it('should lock an array of items', () => {
			const items = ['Item 1', 'Item 2', 'Item 3'];
			lock.lock(items);

			items.forEach(item => {
				expect(lock.isLocked(item)).toBe(true);
			});
		});
		//TODO implement ItemImpl and JokerImpl classes
		// Uncomment the following tests once ItemImpl and JokerImpl are implemented
		// it('should lock an ItemImpl instance', () => {
		// 	const item = new ItemImpl('Test Item');
		// 	lock.lock(item);
		// 	expect(lock.isLocked('Test Item')).toBe(true);
		// });

		// it('should lock a JokerImpl instance', () => {
		// 	const joker = new JokerImpl('Test Joker');
		// 	lock.lock(joker);
		// 	expect(lock.isLocked('Test Joker')).toBe(true);
		// });

		it('should throw error for invalid input type', () => {
			expect(() => lock.lock(123)).toThrow('Invalid argument type');
		});
	});

	describe('unlock method', () => {
		beforeEach(() => {
			lock.lock(['Item 1', 'Item 2', 'Item 3']);
		});

		it('should unlock a string item', () => {
			lock.unlock('Item 1');
			expect(lock.isLocked('Item 1')).toBe(false);
		});

		it('should unlock an array of items', () => {
			lock.unlock(['Item 1', 'Item 2']);

			expect(lock.isLocked('Item 1')).toBe(false);
			expect(lock.isLocked('Item 2')).toBe(false);
			expect(lock.isLocked('Item 3')).toBe(true);
		});

		// it('should unlock an ItemImpl instance', () => {
		// 	const item = new ItemImpl('Item 3');
		// 	lock.unlock(item);
		// 	expect(lock.isLocked('Item 3')).toBe(false);
		// });
	});

	describe('isLocked method', () => {
		beforeEach(() => {
			lock.lock(['Item 1', 'Item 2']);
		});

		it('should return true for locked items', () => {
			expect(lock.isLocked('Item 1')).toBe(true);
		});

		it('should return false for unlocked items', () => {
			expect(lock.isLocked('Unknown Item')).toBe(false);
		});

		// it('should work with object instances', () => {
		// 	const item = new ItemImpl('Item 1');
		// 	expect(lock.isLocked(item)).toBe(true);
		// });
	});

	describe('firstLock method', () => {
		it('should lock all items in static firstLock set', () => {
			lock.firstLock();

			Array.from(Lock.firstLock).forEach(item => {
				expect(lock.isLocked(item)).toBe(true);
			});
		});
	});

	describe('initLocks method', () => {
		it('should lock ante2Lock items when ante is 1', () => {
			lock.initLocks(1, false, false);

			Array.from(Lock.ante2Lock).forEach(item => {
				expect(lock.isLocked(item)).toBe(true);
			});
		});

		it('should not lock ante2Lock items when ante is 2', () => {
			lock.initLocks(2, false, false);

			Array.from(Lock.ante2Lock).forEach(item => {
				expect(lock.isLocked(item)).toBe(false);
			});
		});

		it('should lock appropriate items for fresh profile', () => {
			lock.initLocks(1, true, false);

			expect(lock.isLocked('Negative Tag')).toBe(true);
			expect(lock.isLocked('Foil Tag')).toBe(true);
			expect(lock.isLocked('Palette')).toBe(true);
		});

		it('should lock appropriate items for fresh run', () => {
			lock.initLocks(1, false, true);

			expect(lock.isLocked('Planet X')).toBe(true);
			expect(lock.isLocked('Ceres')).toBe(true);
			expect(lock.isLocked('Palette')).toBe(true);
		});
	});

	describe('initUnlocks method', () => {
		beforeEach(() => {
			// Lock everything first
			lock.lock([
				"The Mouth", "The Fish", "The Wall", "The House", "The Mark",
				"The Wheel", "The Arm", "The Water", "The Needle", "The Flint",
				"Standard Tag", "Meteor Tag", "Buffoon Tag", "Handy Tag",
				"Garbage Tag", "Ethereal Tag", "Top-up Tag", "Orbital Tag",
				"Negative Tag", "The Tooth", "The Eye", "The Plant",
				"The Serpent", "The Ox"
			]);
		});

		it('should unlock ante 2 items when ante is 2', () => {
			lock.initUnlocks(2, true);

			expect(lock.isLocked('The Mouth')).toBe(false);
			expect(lock.isLocked('Standard Tag')).toBe(false);
			expect(lock.isLocked('Negative Tag')).toBe(true); // Still locked for fresh profile
		});

		it('should unlock Negative Tag when not a fresh profile', () => {
			lock.initUnlocks(2, false);
			expect(lock.isLocked('Negative Tag')).toBe(false);
		});

		it('should unlock ante 3 items when ante is 3', () => {
			lock.initUnlocks(3, false);

			expect(lock.isLocked('The Tooth')).toBe(false);
			expect(lock.isLocked('The Eye')).toBe(false);
		});

		it('should unlock The Plant when ante is 4', () => {
			lock.initUnlocks(4, false);
			expect(lock.isLocked('The Plant')).toBe(false);
		});

		it('should unlock The Serpent when ante is 5', () => {
			lock.initUnlocks(5, false);
			expect(lock.isLocked('The Serpent')).toBe(false);
		});

		it('should unlock The Ox when ante is 6', () => {
			lock.initUnlocks(6, false);
			expect(lock.isLocked('The Ox')).toBe(false);
		});
	});
});