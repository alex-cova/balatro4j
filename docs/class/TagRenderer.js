import tagData from "../data/tagData.js";
import CardRenderer from "./CardRenderer.js";

export class TagRenderer extends CardRenderer {
	// const tagWidth = 204 / 6;
	// const tagHeight = 170 / 5;
	constructor() {
		super("../public/tags.png");
		this.gridWidth = 6;
		this.gridHeight = 5;
		this.imageWidth = 204;
		this.imageHeight = 170;
		this.itemWidth = this.imageWidth / this.gridWidth;
		this.itemHeight = this.imageHeight / this.gridHeight;
	}

	createCard(name) {
		const tag = tagData.get(name);
		return this.createCardWithCoordenates({
			x: tag.x,
			y: tag.y
		})
	}
}