import CardRenderer from "./CardRenderer.js";
import jokerData from "../data/jokerData.js";

//TODO add this to a contsants file
const LEGENDARIES_NAMES = [
	"Perkeo",
	"Triboulet",
	"Chicot",
	"Canio",
	"Yorick",
	"The Soul",
]

export class JokerRenderer extends CardRenderer {
	constructor() {
		super("public/Jokers_Sprite.png");
	}

	legendaryOverlay(name, ctx, canvas) {
				const legendaryData = jokerData.get(name);
				const legendaryImg = new Image();
				legendaryImg.src = this.srcImage;
		
				legendaryImg.onload = () => {
					ctx.drawImage(
						legendaryImg,
						legendaryData.x * this.itemWidth,
						(legendaryData.y + 1) * this.itemHeight,// adjusting y to get the overlay that is one row below the joker background
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

	//add behaviour of the createCard method to render jokers in the sprite sheet
	//but instead of getting the x and y coordinates of the sprite, it will get the name of the joker
	createCard(name) {
		const joker = jokerData.get(name);
		if (!joker) {
			throw new Error(`Joker with name ${name} not found`);
		}
		if( LEGENDARIES_NAMES.includes(name)) {
			const legendaryWrapper = document.createElement('div');
			legendaryWrapper.className = 'legendary-wrapper-revealed';
			const card = this.createCardWithCoordenates({ x: joker.x, y: joker.y });
			card.classList.add('legendary');
			const canvas = document.createElement('canvas');
			canvas.id = 'legendary-overlay';
			const ctx = canvas.getContext('2d');
			this.legendaryOverlay(name, ctx, canvas)
			card.appendChild(canvas);
			legendaryWrapper.appendChild(card);
			
			return legendaryWrapper;

		}

		return this.createCardWithCoordenates({ x: joker.x, y: joker.y });
	}
}