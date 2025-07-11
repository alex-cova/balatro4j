import CardRenderer from './CardRenderer.js';
import tarotsAndPlanetsData from '../data/tarotsAndPlanetsData.js';
import enhancersData from '../data/enhancersData.js';
import { JokerRenderer } from './JokerRenderer.js';

const LEGENDARIES_NAMES = [
	"Perkeo",
	"Triboulet",
	"Chicot",
	"Canio",
	"Yorick",
	"The Soul",
]

export class TarotAndPlanetRenderer extends CardRenderer {
	constructor() {
		super("public/Tarots_Sprite.png");
		this.imageHeight = 570;
		this.gridWidth = 10;
		this.gridHeight = 6;
		this.itemWidth = this.imageWidth / this.gridWidth;
		this.itemHeight = this.imageHeight / this.gridHeight;
	}

	overlayTheSoulStone(ctx, canvas) {
		const soulStone = enhancersData.get("Soul");
		const enhancersImg = new Image();
		enhancersImg.src = "public/Enhancers.png";

		enhancersImg.onload = () => {
			ctx.drawImage(
				enhancersImg,
				soulStone.x * this.itemWidth,
				soulStone.y * this.itemHeight,
				this.itemWidth,
				this.itemHeight,
				0,
				0,
				this.itemWidth,
				this.itemHeight
			);

			return canvas;
		}

	}

	createCard(name) {
		const tarotOrPlanet = tarotsAndPlanetsData.get(name);
		if (!tarotOrPlanet) {
			throw new Error(`Tarot or Planet with name ${name} not found`);
		}

		if (LEGENDARIES_NAMES.includes(name)) {
			const legendaryWrapper = document.createElement('div');
			legendaryWrapper.className = 'legendary-wrapper';
			const card = this.createCardWithCoordenates({ x: tarotOrPlanet.x, y: tarotOrPlanet.y });
			card.classList.add('legendary')
			const canvas = document.createElement('canvas');
			canvas.id = 'soul-stone-overlay';
			const ctx = canvas.getContext('2d');

			this.overlayTheSoulStone(ctx, canvas);

			card.appendChild(canvas);
			//show legendary button
			const showLegendaryButton = document.createElement('button');
			showLegendaryButton.className = 'show-legendary-button copy-button';
			showLegendaryButton.innerText = 'USE';
			showLegendaryButton.addEventListener('click', () => {
				const jokerRenderer = new JokerRenderer();
				const legendaryCard = jokerRenderer.createCard(name);
				legendaryCard.classList.add('legendary-revealed');
				legendaryWrapper.removeChild(card);
				legendaryWrapper.appendChild(legendaryCard);
				// const legendaryCard = this.createCardWithCoordenates({ x: tarotOrPlanet.x, y: tarotOrPlanet.y });
				// legendaryCard.classList.add('legendary');
				// card.appendChild(legendaryCard);
				showLegendaryButton.remove();
			});

			legendaryWrapper.appendChild(card);
			legendaryWrapper.appendChild(showLegendaryButton);

			// Variables to track current rotation (for smooth interpolation)
			let currentRotateX = 0;
			let currentRotateY = 0;
			let currentTranslateX = 0;
			let currentTranslateY = 0;
			let animationFrameId = null;

			// Smooth animation function
			const updateCardTransform = () => {
				card.style.transform = `perspective(800px) rotateX(${currentRotateX}deg) rotateY(${currentRotateY}deg)`;
				canvas.style.transform = `translateZ(20px) translate(${currentTranslateX}px, ${currentTranslateY}px)`;
				animationFrameId = requestAnimationFrame(updateCardTransform);
			};

			// Add parallax effect on hover/mouse movement
			card.addEventListener('mousemove', (e) => {
				// Calculate mouse position relative to card center
				const rect = card.getBoundingClientRect();
				const centerX = rect.left + rect.width / 2;
				const centerY = rect.top + rect.height / 2;

				// Calculate normalized mouse position (-1 to 1 range)
				const mouseX = (e.clientX - centerX) / (rect.width / 2);
				const mouseY = (e.clientY - centerY) / (rect.height / 2);

				// Apply easing function to make edges smoother
				// This creates a cubic curve that's gentler at the extremes
				const easeMouseX = mouseX * (1 - 0.2 * Math.abs(mouseX));
				const easeMouseY = mouseY * (1 - 0.2 * Math.abs(mouseY));

				// Calculate target rotation with easing
				const targetRotateY = easeMouseX * 15; // Max 15 degrees
				const targetRotateX = -easeMouseY * 15; // Max 15 degrees

				// Smoothly interpolate current rotation toward target (damping effect)
				currentRotateX += (targetRotateX - currentRotateX) * 0.15;
				currentRotateY += (targetRotateY - currentRotateY) * 0.15;

				// Calculate smoother translation for overlay
				const targetTranslateX = easeMouseX * 5;
				const targetTranslateY = easeMouseY * 5;
				currentTranslateX += (targetTranslateX - currentTranslateX) * 0.2;
				currentTranslateY += (targetTranslateY - currentTranslateY) * 0.2;

				// Start animation if not already running
				if (!animationFrameId) {
					animationFrameId = requestAnimationFrame(updateCardTransform);
				}
			});

			// Reset on mouse leave with smooth transition
			card.addEventListener('mouseleave', () => {
				// Smoothly return to center
				const resetAnimation = () => {
					// Interpolate back to zero
					currentRotateX *= 0.85;
					currentRotateY *= 0.85;
					currentTranslateX *= 0.85;
					currentTranslateY *= 0.85;

					// Update the transform
					card.style.transform = `perspective(800px) rotateX(${currentRotateX}deg) rotateY(${currentRotateY}deg)`;
					canvas.style.transform = `translateZ(20px) translate(${currentTranslateX}px, ${currentTranslateY}px)`;

					// Stop animation when close enough to zero
					if (Math.abs(currentRotateX) < 0.1 && Math.abs(currentRotateY) < 0.1) {
						cancelAnimationFrame(animationFrameId);
						animationFrameId = null;
						card.style.transform = 'perspective(800px) rotateX(0) rotateY(0)';
						canvas.style.transform = 'translateZ(20px)';
					} else {
						requestAnimationFrame(resetAnimation);
					}
				};

				resetAnimation();
			});

			card.addEventListener('click', () => {
				showLegendaryButton.classList.toggle('active');
				card.classList.toggle('active');
			})

			return legendaryWrapper;
		}

		return this.createCardWithCoordenates({ x: tarotOrPlanet.x, y: tarotOrPlanet.y });
	}
}
