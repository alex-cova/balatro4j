import ss from "./autocomplete_options.js";
import { VoucherRenderer } from "../class/VoucherRenderer.js";
import { TarotAndPlanetRenderer } from "../class/TarotAndPlanetRenderer.js";
import { JokerRenderer } from "../class/JokerRenderer.js";
import { StandardCardRenderer } from "../class/StandardCardRenderer.js";
import { BlindRenderer } from "../class/BlindRenderer.js";
import { TagRenderer } from "../class/TagRenderer.js";
const autoComplete = window.autoComplete;
const addedTags = new Set();

const BUFFOON = "Buffoon";
const CELESTIAL = "Celestial";
const TAROT = "Arcana";
const STANDARD = "Standard";
const SPECTRAL = "Spectral";
const LEGENDARY = "Legendary";
const THE_SOUL = "The Soul";
const TAGS = "Tag";
const BOSS = "Boss";

const searchField = document.getElementById('autoComplete');
const tagsContainer = document.getElementById('tags');
const searchButton = document.getElementById('searchbutton');
const cardsContainer = document.getElementById('cards');
const runContainer = document.getElementById('run');

searchField.addEventListener('keyup', (event) => {
	if (event.key === 'Enter') {
		const words = searchField.value.trim().split(',');
		words.forEach(word => {
			if (word) {
				createTag(word.trim());
			}
		});
		searchField.value = '';
	}
});

searchButton.addEventListener('click', () => {
	//clean containers
	cardsContainer.innerHTML = '';
	runContainer.innerHTML = '';
	const words = searchField.value.trim().split(',')

	if (words.length > 0) {
		words.forEach(word => {
			if (word) {
				createTag(word.trim());
			}
		});
		searchField.value = '';
	}

	const tags = Array.from(tagsContainer.getElementsByClassName('tag')).map(tag => tag.innerText.replace('X', '').trim());

	if (tags.length > 0) {
		searchButton.innerText = 'Searching...';
		fetch('https://balatro.legendaryseeds.app/balatro/search', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(tags)
		})
			.then(response => response.json())
			.then(data => {
				renderCards(data);
			})
			.catch(error => console.error('Error:', error))
			.finally(() => {
				searchButton.innerText = 'Search';
			});
	}
});

const autoCompleteJS = new autoComplete({
	placeHolder: "Enter the item name",
	data: {
		src: ss
	},
	resultItem: {
		highlight: true,
		class: "result-item",
	}, events: {
		input: {
			selection: (event) => {
				const selection = event.detail.selection.value;
				autoCompleteJS.input.value = '';
				createTag(selection);
			}
		}
	}
});

function createTag(text) {
	if (!isValid(text) || text === '') {
		return;
	}
	if (addedTags.has(text.toLowerCase())) {
		return; // Prevent duplicate tags
	}
	addedTags.add(text.toLowerCase());

	const tag = document.createElement('div');
	tag.className = 'tag';
	tag.innerText = text.trim().replace(',', '');

	const close = document.createElement('span');
	close.className = 'close';
	close.innerText = 'X';
	close.addEventListener('click', () => {
		tagsContainer.removeChild(tag);
		addedTags.delete(text.toLowerCase());
	});

	tag.appendChild(close);
	tagsContainer.appendChild(tag);
}

function isValid(text) {
	for (let i = 0; i < ss.length; i++) {
		if (ss[i].toLowerCase() === text.toLowerCase()) {
			return true;
		}
	}

	return false
}

function renderCards(codes) {
	setTimeout(() => {
		cardsContainer.scrollIntoView({ behavior: 'smooth', block: "start" })
	}, 400);
	cardsContainer.innerHTML = '';
	codes.forEach(code => {
		const card = document.createElement('div');
		card.className = 'card';
		card.innerText = code;

		card.addEventListener('click', () => {
			analyzeSeed(code);
		});
		const copyButton = document.createElement('button');
		copyButton.className = 'copy-button';
		copyButton.innerText = 'Copy';
		copyButton.addEventListener('click', (e) => {
			e.stopPropagation();
			copyButton.innerText = 'Copied!';
			setTimeout(() => {
				copyButton.innerText = 'Copy';
			}, 1000);
			navigator.clipboard.writeText(code);
		});

		card.appendChild(copyButton);
		cardsContainer.appendChild(card);
	});
}

async function analyzeSeed(seed) {
	const res = await fetch(`https://balatro.legendaryseeds.app/balatro/${seed}?maxAnte=8`);
	const data = await res.json();
	cardsContainer.classList.toggle('hidden');
	setTimeout(() => {
		runContainer.scrollIntoView({ behavior: 'smooth', block: "start" });
	}, 400);
	const voucherRenderer = new VoucherRenderer();
	const tarotAndPlanetRenderer = new TarotAndPlanetRenderer();
	const renderers = {
		[BUFFOON]: new JokerRenderer(),
		[CELESTIAL]: tarotAndPlanetRenderer,
		[TAROT]: tarotAndPlanetRenderer,
		[SPECTRAL]: tarotAndPlanetRenderer,
		[LEGENDARY]: tarotAndPlanetRenderer,
		[STANDARD]: new StandardCardRenderer(),
		[TAGS]: new TagRenderer(),
		[BOSS]: new BlindRenderer(),
	}

	runContainer.innerHTML = 'Seed: ' + seed + '<br>';

	data.antes.forEach((ante, i) => {
		const anteElement = document.createElement('div');
		anteElement.className = 'ante';

		const firstRow = document.createElement('div');
		firstRow.className = 'first-row';
		//Render Tags
		const tags = ante.tags;
		const tagsElement = document.createElement('div');
		tagsElement.className = 'run-tags';
		tags.forEach(tag => {
			const tagCard = renderers.Tag.createCard(tag);
			tagsElement.appendChild(tagCard);
		});
		firstRow.appendChild(tagsElement);

		//Render Boss Blinds
		const boss = ante.boss;
		const bossElement = document.createElement('div');
		bossElement.className = 'boss';
		const bossCard = renderers[BOSS].createCard(boss);
		bossElement.appendChild(bossCard);
		firstRow.appendChild(bossElement);

		//Render Shop Queue
		const shopItems = ante.shopQueue;
		const shopQueue = document.createElement('div');

		shopQueue.className = 'shop-queue';
		shopItems.forEach(shopItem => {
			const renderer = renderers[shopItem.kind];
			const card = renderer.createCard(shopItem.item);
			shopQueue.appendChild(card);
		});
		firstRow.appendChild(shopQueue);

		anteElement.appendChild(firstRow);

		const secondRow = document.createElement('div');
		secondRow.className = "second-row"
		//Render Vouchers
		const voucherElement = document.createElement('div');
		voucherElement.className = 'voucher';
		const voucherCard = voucherRenderer.createCard(ante.voucher);
		voucherElement.appendChild(voucherCard);
		secondRow.appendChild(voucherElement);

		//Render Packs
		const packs = ante.packs;
		const packsElement = document.createElement('div');
		packsElement.className = 'packs';
		packs.forEach(pack => {
			const packNameContainer = document.createElement('div');
			packNameContainer.className = 'pack-name-container';
			const packNameElement = document.createElement('span');
			packNameElement.className = 'pack-name';
			packNameElement.appendChild(document.createTextNode(`${pack.type}`));
			packNameContainer.appendChild(packNameElement);
			packsElement.appendChild(packNameContainer);

			pack.options.forEach(choice => {
				if (choice.item === THE_SOUL) {
					const soulCard = renderers.Arcana.createCard(choice.item);
					packsElement.appendChild(soulCard);
				} else {
					const packCard = renderers[choice.kind].createCard(choice.item);
					packsElement.appendChild(packCard);
				}
			});
			// packsElement.appendChild(packElement);
		});
		secondRow.appendChild(packsElement);

		anteElement.appendChild(secondRow);

		// anteElement.appendChild(packsElement);
		runContainer.appendChild(anteElement);
	});

}